/*
 * Copyright (c) 2016 Mobvoi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.utils.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.Window;

public class BlurBehind {

    private final Context mContext;

    private final BlurFactor mBlurFactor = new BlurFactor();
    private int mAnimationDuration;

    public static BlurBehind from(Context context) {
        return new BlurBehind(context);
    }

    private BlurBehind(Context context) {
        mContext = context;
        mAnimationDuration = 0;
    }

    public BlurBehind radius(int radius) {
        mBlurFactor.radius = radius;
        return this;
    }

    public BlurBehind sampling(int sampling) {
        mBlurFactor.sampling = sampling;
        return this;
    }

    public BlurBehind color(int color) {
        mBlurFactor.color = color;
        return this;
    }

    public BlurBehind animate(int duration) {
        mAnimationDuration = duration;
        return this;
    }

    public BlurBehindExecutor capture(@Nullable Window window) {
        return capture(window != null ? window.getDecorView() : null);
    }

    public BlurBehindExecutor capture(@Nullable View view) {
        return new BlurBehindExecutor(mContext, prepareSource(view), mBlurFactor, mAnimationDuration);
    }

    private Bitmap prepareSource(View view) {
        if (view == null) {
            return null;
        }
        Bitmap source = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(source);
        view.draw(c);
        return source;
    }

    public static class BlurBehindExecutor {

        private final Context mContext;
        private final Bitmap mSource;
        private final BlurFactor mBlurFactor;
        private final int mAnimationDuration;

        private Runnable mPreSetBackgroundRunnable;
        private Runnable mPostSetBackgroundRunnable;

        private BlurBehindExecutor(Context mContext, Bitmap source,
                                   BlurFactor factor, int duration) {
            this.mContext = mContext;
            this.mSource = source;
            this.mBlurFactor = factor;
            this.mAnimationDuration = duration;
        }

        public BlurBehindExecutor preSetBackground(Runnable callback) {
            mPreSetBackgroundRunnable = callback;
            return this;
        }

        public BlurBehindExecutor postSetBackground(Runnable callback) {
            mPostSetBackgroundRunnable = callback;
            return this;
        }

        public BlurBehindFuture into(final Window window) {
            if (window == null) {
                return new BlurBehindFuture(null);
            }
            Display display = window.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mBlurFactor.width = size.x;
            mBlurFactor.height = size.y;
            return start(new ViewGetter() {
                @Override
                public View get() {
                    // Make sure there is no actions for the window before pre set background.
                    return window.getDecorView();
                }
            });
        }

        public BlurBehindFuture into(final View target) {
            if (target == null) {
                return new BlurBehindFuture(null);
            }
            mBlurFactor.width = target.getMeasuredWidth();
            mBlurFactor.height = target.getMeasuredHeight();
            return start(new ViewGetter() {
                @Override
                public View get() {
                    return target;
                }
            });
        }

        private BlurBehindFuture start(final ViewGetter getter) {
            if (getter == null) {
                return new BlurBehindFuture(null);
            }

            BlurBehindTask task = new BlurBehindTask(mBlurFactor, new OnBlurFinishedCallback() {
                @Override
                public void onBlurFinished(Bitmap blurredBitmap) {
                    if (mPreSetBackgroundRunnable != null) {
                        mPreSetBackgroundRunnable.run();
                    }

                    Drawable background = new BitmapDrawable(
                            mContext.getResources(), blurredBitmap);
                    changeBackground(getter.get(), background, mAnimationDuration);

                    if (mPostSetBackgroundRunnable != null) {
                        mPostSetBackgroundRunnable.run();
                    }
                }
            });

            return new BlurBehindFuture(task.execute(mSource));
        }

        private TransitionDrawable changeBackground(View view, Drawable drawable, int duration) {
            if (duration <= 0) {
                view.setBackground(drawable);
                return null;
            }

            Drawable oldDrawable = view.getBackground();
            if (oldDrawable instanceof TransitionDrawable) {
                TransitionDrawable transitionDrawable = (TransitionDrawable) oldDrawable;
                if (transitionDrawable.getNumberOfLayers() < 1) {
                    oldDrawable = null;
                } else {
                    oldDrawable = transitionDrawable.getDrawable(
                            transitionDrawable.getNumberOfLayers() - 1);
                }
            }
            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{oldDrawable, drawable});
            view.setBackground(transitionDrawable);
            transitionDrawable.startTransition(duration);
            return transitionDrawable;
        }

        private interface ViewGetter {
            View get();
        }

    }

    public static class BlurBehindFuture {

        private AsyncTask mBlurBehindTask;

        private BlurBehindFuture(AsyncTask task) {
            this.mBlurBehindTask = task;
        }

        public void cancel() {
            if (mBlurBehindTask != null) {
                mBlurBehindTask.cancel(true);
                mBlurBehindTask = null;
            }
        }
    }

    private static class BlurBehindTask extends AsyncTask<Bitmap, Void, Bitmap> {

        private OnBlurFinishedCallback mOnBlurFinishedCallback;
        private final BlurFactor mBlurFactor;

        public BlurBehindTask(BlurFactor factor, OnBlurFinishedCallback callback) {
            mBlurFactor = factor;
            mOnBlurFinishedCallback = callback;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            if (params.length == 0) {
                return null;
            }

            Bitmap source = params[0];

            int width = mBlurFactor.width / mBlurFactor.sampling;
            int height = mBlurFactor.height / mBlurFactor.sampling;

            if (width == 0 || height == 0) {
                return null;
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            canvas.scale(1 / (float) mBlurFactor.sampling, 1 / (float) mBlurFactor.sampling);
            Paint paint = new Paint();
            paint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
            PorterDuffColorFilter filter =
                    new PorterDuffColorFilter(mBlurFactor.color, PorterDuff.Mode.SRC_ATOP);
            paint.setColorFilter(filter);
            canvas.drawBitmap(source, 0, 0, paint);

            return FastBlur.doBlur(bitmap, mBlurFactor.radius, true);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mOnBlurFinishedCallback != null) {
                mOnBlurFinishedCallback.onBlurFinished(bitmap);
            }
        }
    }

    public interface OnBlurFinishedCallback {
        void onBlurFinished(Bitmap blurredBitmap);
    }
}
