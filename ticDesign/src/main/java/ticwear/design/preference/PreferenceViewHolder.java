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

package ticwear.design.preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ticwear.design.DesignConfig;
import ticwear.design.R;
import ticwear.design.widget.FocusableLinearLayoutManager;
import ticwear.design.widget.FocusableLinearLayoutManager.FocusState;
import ticwear.design.widget.FocusableLinearLayoutManager.ViewHolder;

public class PreferenceViewHolder extends ViewHolder {

    static final String TAG = "PrefVH";

    protected ViewGroup iconFrame;
    protected ImageView iconBackground;
    protected ImageView iconView;
    protected TextView titleView;
    protected TextView summaryView;
    protected ViewGroup widgetFrame;

    protected float iconScaleUp;
    protected float iconScaleDown;
    protected float itemAlphaDown;

    public PreferenceViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
        this(parent, layoutResId, 0);
    }

    public PreferenceViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId,
                                @LayoutRes int widgetLayoutResId) {
        this(inflateView(parent, layoutResId), inflateView(parent, widgetLayoutResId));
    }

    PreferenceViewHolder(@NonNull View itemView, @Nullable View widgetView) {
        super(itemView);

        iconFrame = findViewById(R.id.icon_frame);
        iconBackground = findViewById(R.id.icon_background);
        iconView = findViewById(android.R.id.icon);
        titleView = findViewById(android.R.id.title);
        summaryView = findViewById(android.R.id.summary);

        widgetFrame = findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (widgetView != null) {
                widgetFrame.addView(widgetView);
                widgetFrame.setVisibility(View.VISIBLE);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }

        initItemAnimationProperties(itemView.getContext());
    }

    private void initItemAnimationProperties(Context context) {
        float iconSizeUp = context.getResources().getDimensionPixelSize(R.dimen.tic_list_item_icon_frame_size_large);
        float iconSizeNormal = context.getResources().getDimensionPixelSize(R.dimen.tic_list_item_icon_frame_size_normal);
        float iconSizeDown = context.getResources().getDimensionPixelSize(R.dimen.tic_list_item_icon_frame_size_small);
        iconScaleUp = iconSizeUp / iconSizeNormal;
        iconScaleDown = iconSizeDown / iconSizeNormal;
        itemAlphaDown = 0.6f;   // TODO: define in attributes.
    }

    @CheckResult
    public static View inflateView(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
        Context context = parent.getContext();
        final LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return layoutResId == 0 ? null : layoutInflater.inflate(layoutResId, parent, false);
    }

    /**
     * Binds the created View to the data for this Preference.
     * <p>
     * This is a good place to grab references to custom Views in the layout and
     * set properties on them.
     * <p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param preferenceData The preference data to bind to this view.
     * @see #PreferenceViewHolder(View, View)
     */
    @CallSuper
    public void bind(@NonNull PreferenceData preferenceData) {
        if (DesignConfig.DEBUG_RECYCLER_VIEW) {
            Log.v(TAG, getLogPrefix() + "bind to " + preferenceData.title + getLogSuffix());
        }

        bindTextView(titleView, preferenceData.title);

        bindTextView(summaryView, preferenceData.summary);

        boolean removeIcon = preferenceData.removeIconIfEmpty && preferenceData.icon == null;

        if (iconView != null) {
            if (preferenceData.icon != null) {
                iconView.setImageDrawable(preferenceData.icon);
            }
            iconView.setVisibility(removeIcon ? View.GONE : View.VISIBLE);
        }

        if (iconFrame != null) {
            iconFrame.setVisibility(removeIcon ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onCentralProgressUpdated(float progress, long animateDuration) {
        float scaleMin = iconScaleDown;
        float scaleMax = iconScaleUp;
        float alphaMin = itemAlphaDown;
        float alphaMax = 1.0f;

        float scale = scaleMin + (scaleMax - scaleMin) * progress;
        float alphaProgress = getFocusInterpolator().getInterpolation(progress);
        float alpha = alphaMin + (alphaMax - alphaMin) * alphaProgress;
        transform(scale, alpha, animateDuration);
    }

    @Override
    protected void onFocusStateChanged(@FocusState int focusState, boolean animate) {
        if (focusState == FocusableLinearLayoutManager.FOCUS_STATE_NORMAL) {
            transform(1.0f, 1.0f, animate ? getDefaultAnimDuration() : 0);
        }

        if (DesignConfig.DEBUG_RECYCLER_VIEW) {
            Log.d(TAG, getLogPrefix() + "focus state to " + focusState + ", animate " + animate +
                    ", view alpha " + itemView.getAlpha() +
                    getLogSuffix());
        }
    }

    private void transform(float scale, float alpha, long duration) {
        itemView.animate().cancel();
        if (titleView != null) titleView.animate().cancel();
        if (summaryView != null) summaryView.animate().cancel();
        if (showIconAnimation()) {
            iconView.animate().cancel();
        }
        if (duration > 0) {
            itemView.animate()
                    .setDuration(duration)
                    .scaleX(scale)
                    .scaleY(scale)
                    .start();
            if (titleView != null) {
                titleView.animate()
                        .setDuration(duration)
                        .alpha(alpha)
                        .start();
            }
            if (summaryView != null) {
                summaryView.animate()
                        .setDuration(duration)
                        .alpha(alpha)
                        .start();
            }
            if (showIconAnimation()) {
                float inverseScale = 1.0f / scale;
                iconView.animate()
                        .setDuration(duration)
                        .scaleX(inverseScale)
                        .scaleY(inverseScale)
                        .start();
            }
        } else {
            if (showIconAnimation()) {
                float inverseScale = 1.0f / scale;
                iconView.setScaleX(inverseScale);
                iconView.setScaleY(inverseScale);
            }
            if (titleView != null) titleView.setAlpha(alpha);
            if (summaryView != null) summaryView.setAlpha(alpha);
            itemView.setScaleX(scale);
            itemView.setScaleY(scale);
        }
    }

    private boolean showIconAnimation() {
        return iconView != null && iconFrame != null && iconFrame.getVisibility() == View.VISIBLE;
    }

    public void setEnabled(boolean enabled) {
        setEnabledStateOnViews(itemView, enabled);
    }

    protected void bindTextView(@Nullable TextView textView, @Nullable final CharSequence text) {
        if (textView != null) {
            if (!TextUtils.isEmpty(text)) {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    protected <V extends View> V findViewById(@IdRes int id) {
        View v = itemView.findViewById(id);
        return v == null ? null : (V) v;
    }

    /**
     * Makes sure the view (and any children) get the enabled state changed.
     */
    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    /**
     * The data of preference needs to display in view.
     */
    protected static class PreferenceData {
        @Nullable
        protected CharSequence title;
        @Nullable
        protected CharSequence summary;
        @Nullable
        protected Drawable icon;
        protected boolean removeIconIfEmpty = true;

        @Nullable
        public CharSequence getTitle() {
            return title;
        }

        @Nullable
        public CharSequence getSummary() {
            return summary;
        }

        @Nullable
        public Drawable getIcon() {
            return icon;
        }

        public boolean isRemoveIconIfEmpty() {
            return removeIconIfEmpty;
        }
    }

}