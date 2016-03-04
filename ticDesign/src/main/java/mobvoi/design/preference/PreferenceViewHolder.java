package mobvoi.design.preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobvoi.design.R;
import mobvoi.design.widget.TicklableListView;

public class PreferenceViewHolder extends TicklableListView.ViewHolder {

    protected ViewGroup iconFrame;
    protected ImageView iconBackground;
    protected ImageView iconView;
    protected TextView titleView;
    protected TextView summaryView;
    protected ViewGroup widgetFrame;

    protected float iconScaleUp;
    protected float iconScaleDown;
    protected float itemAlphaDown;
    protected long animateDuration;

    protected static final long DEFAULT_ANIMATE_DURATION = 200;

    public PreferenceViewHolder(Context context, @LayoutRes int layoutResId) {
        this(context, layoutResId, 0);
    }

    public PreferenceViewHolder(Context context, @LayoutRes int layoutResId,
                                @LayoutRes int widgetLayoutResId) {
        this(inflateView(context, layoutResId), inflateView(context, widgetLayoutResId));
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
        float iconSizeUp = context.getResources().getDimensionPixelSize(R.dimen.list_item_icon_frame_size_large);
        float iconSizeNormal = context.getResources().getDimensionPixelSize(R.dimen.list_item_icon_frame_size_normal);
        float iconSizeDown = context.getResources().getDimensionPixelSize(R.dimen.list_item_icon_frame_size_small);
        iconScaleUp = iconSizeUp / iconSizeNormal;
        iconScaleDown = iconSizeDown / iconSizeNormal;
        itemAlphaDown = 0.6f;   // TODO: define in attributes.
        animateDuration = DEFAULT_ANIMATE_DURATION; // TODO: define in attributes.
    }

    @CheckResult
    public static View inflateView(Context context, @LayoutRes int layoutResId) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return layoutResId == 0 ? null : layoutInflater.inflate(layoutResId, null);
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
    protected void onFocusStateChanged(int focusState, boolean animate) {
        float scale = 1.0f;
        float alpha = 1.0f;
        switch (focusState) {
            case TicklableListView.FOCUS_STATE_NORMAL:
                break;
            case TicklableListView.FOCUS_STATE_CENTRAL:
                scale = iconScaleUp;
                alpha = 1.0f;
                break;
            case TicklableListView.FOCUS_STATE_NON_CENTRAL:
                scale = iconScaleDown;
                alpha = itemAlphaDown;
                break;
        }
        if (animate) {
            itemView.animate()
                    .setDuration(animateDuration)
                    .alpha(alpha)
                    .scaleX(scale)
                    .scaleY(scale);
            if (showIconAnimation()) {
                float inverseScale = 1.0f / scale;
                iconView.animate()
                        .setDuration(animateDuration)
                        .scaleX(inverseScale)
                        .scaleY(inverseScale);
            }
        } else {
            if (showIconAnimation()) {
                float inverseScale = 1.0f / scale;
                iconView.setScaleX(inverseScale);
                iconView.setScaleY(inverseScale);
            }
            itemView.setAlpha(alpha);
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
    static class PreferenceData {

        @Nullable
        CharSequence title;
        @Nullable
        CharSequence summary;
        @Nullable
        Drawable icon;
        boolean removeIconIfEmpty = true;
    }

}