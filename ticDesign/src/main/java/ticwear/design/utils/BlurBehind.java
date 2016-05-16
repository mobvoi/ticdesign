package ticwear.design.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

public class BlurBehind {

    private static final int CONSTANT_BLUR_RADIUS = 20;

    private BlurBehindTask mBlurBehindTask;

    public BlurBehind() {
    }

    public void prepare(@Nullable Window window, OnBlurFinishedCallback callback) {
        prepare(window != null ? window.getDecorView() : null, callback);
    }

    public void prepare(@Nullable View view, OnBlurFinishedCallback callback) {
        cancel();
        mBlurBehindTask = new BlurBehindTask(callback);
        mBlurBehindTask.execute(prepareSource(view));
    }

    public void cancel() {
        if (mBlurBehindTask != null) {
            mBlurBehindTask.cancel(true);
            mBlurBehindTask = null;
        }
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

    private class BlurBehindTask extends AsyncTask<Bitmap, Void, Bitmap> {

        private OnBlurFinishedCallback mOnBlurFinishedCallback;

        public BlurBehindTask(OnBlurFinishedCallback callback) {
            mOnBlurFinishedCallback = callback;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            if (params.length == 0) {
                return null;
            }

            Bitmap source = params[0];
            return FastBlur.doBlur(source, CONSTANT_BLUR_RADIUS, true);
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
