package ticwear.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import ticwear.design.R;

public class AlertView extends FrameLayout {

    private final boolean mIsShowTitle;
    private final boolean mIsShowContent;
    private final boolean mIsShowCancelButton;
    private final String mTitleText;
    private final String mMessageText;
    private final int mMessageTextColor;
    private final float mMessageTextSize;
    private final int mTitleTextColor;
    private final float mTitleTextSize;
    private Drawable mPositiveDrawable;
    private Drawable mCancelDrawable;
    private FloatingActionButton cancel;
    private TextView title;
    private TextView message;
    private FloatingActionButton confirmRight;
    private FloatingActionButton confirmCenter;

    public AlertView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.alert_dialog_ticwear, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlertView);
        mCancelDrawable = typedArray.getDrawable(R.styleable.AlertView_tic_av_cancelDrawable);
        if (mCancelDrawable == null) {
            mCancelDrawable = getResources().getDrawable(R.drawable.tic_ic_btn_cancel);
        }
        mPositiveDrawable = typedArray.getDrawable(R.styleable.AlertView_tic_av_positiveDrawable);
        if (mPositiveDrawable == null) {
            mPositiveDrawable = getResources().getDrawable(R.drawable.tic_ic_btn_ok);
        }

        mIsShowTitle = typedArray.getBoolean(R.styleable.AlertView_tic_av_showTitle, true);
        mIsShowContent = typedArray.getBoolean(R.styleable.AlertView_tic_av_showContent, true);
        mIsShowCancelButton = typedArray.getBoolean(R.styleable.AlertView_tic_av_showCancelButton, true);
        mTitleText = typedArray.getString(R.styleable.AlertView_tic_av_title);
        mMessageText = typedArray.getString(R.styleable.AlertView_tic_av_message);
        mMessageTextColor = typedArray.getColor(R.styleable.AlertView_tic_av_messageTextColor, 0xffcccccc);
        mMessageTextSize = typedArray.getDimension(R.styleable.AlertView_tic_av_messageTextSize,
                getResources().getDimension(R.dimen.tic_text_size_medium_1));
        mTitleTextColor = typedArray.getColor(R.styleable.AlertView_tic_av_titleTextColor,
                Color.WHITE);
        mTitleTextSize = typedArray.getDimension(R.styleable.AlertView_tic_av_titleTextSize,
                getResources().getDimension(R.dimen.tic_text_size_medium_1));
        typedArray.recycle();
        setupView();
    }

    private void setupView() {
        findViewById(R.id.textButtonPanel).setVisibility(INVISIBLE);
        findViewById(R.id.customPanel).setVisibility(INVISIBLE);
        cancel = (FloatingActionButton) findViewById(R.id.iconButton2);
        confirmRight = (FloatingActionButton) findViewById(R.id.iconButton1);
        cancel.setImageDrawable(mCancelDrawable);
        confirmCenter = (FloatingActionButton) findViewById(R.id.iconButton3);

        if (mIsShowCancelButton) {
            confirmCenter.setVisibility(INVISIBLE);
            confirmRight.setVisibility(VISIBLE);
            cancel.setVisibility(VISIBLE);
            confirmRight.setImageDrawable(mPositiveDrawable);
            offsetIconButtons(2);

        } else {
            confirmRight.setVisibility(INVISIBLE);
            confirmCenter.setVisibility(VISIBLE);
            cancel.setVisibility(INVISIBLE);
            confirmCenter.setImageDrawable(mPositiveDrawable);
            offsetIconButtons(1);
        }

        title = (TextView) findViewById(android.R.id.title);
        title.setVisibility(mIsShowTitle ? VISIBLE : GONE);
        title.setText(mTitleText);
        title.setTextColor(mTitleTextColor);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize);
        message = (TextView) findViewById(R.id.message);
        message.setVisibility(mIsShowContent ? VISIBLE : GONE);
        message.setText(mMessageText);
        message.setTextColor(mMessageTextColor);
        message.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMessageTextSize);

    }

    public void setMessage(String messageStr) {
        message.setText(messageStr);
    }

    public void setTitle(String titleStr) {
        title.setText(titleStr);
    }

    public void setConfirmDrawable(Drawable drawable) {
        if (confirmCenter.getVisibility() == VISIBLE) {
            confirmCenter.setImageDrawable(drawable);
        } else {
            confirmRight.setImageDrawable(drawable);
        }
    }

    public void setCancelDrawable(Drawable drawable) {
        if (cancel != null) {
            cancel.setImageDrawable(drawable);
        }
    }

    public void setOnConfirmClickLitener(OnClickListener litenner) {
        confirmCenter.setOnClickListener(litenner);
        confirmRight.setOnClickListener(litenner);
    }

    public void setOnCancelClickLitener(OnClickListener litener) {
        if (cancel != null) {
            cancel.setOnClickListener(litener);
        }
    }

    private void offsetIconButtons(int iconButtonCount) {
        int paddingBottomUnit = getResources()
                .getDimensionPixelOffset(R.dimen.alert_dialog_round_padding_bottom);
        int paddingBottom = iconButtonCount > 1 ? paddingBottomUnit * 2 : paddingBottomUnit;
        int paddingHorizontal;
        if (iconButtonCount == 3) {
            paddingHorizontal = getResources().getDimensionPixelOffset(
                    R.dimen.alert_dialog_round_button_padding_horizontal_full);
        } else if (iconButtonCount == 2) {
            paddingHorizontal = getResources().getDimensionPixelOffset(
                    R.dimen.alert_dialog_round_button_padding_horizontal_pair);
        } else {
            paddingHorizontal = 0;
        }

        CoordinatorLayout.LayoutParams lp;
        lp = getCoordinatorLayoutParams(confirmRight);
        if (lp != null) {
            if (iconButtonCount == 1) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            }
            lp.setMarginStart(paddingHorizontal);
            lp.setMarginEnd(paddingHorizontal);
            lp.bottomMargin = paddingBottom;
            confirmRight.setLayoutParams(lp);
        }
        lp = getCoordinatorLayoutParams(cancel);
        if (lp != null) {
            if (iconButtonCount == 1) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            }
            lp.setMarginStart(paddingHorizontal);
            lp.setMarginEnd(paddingHorizontal);
            lp.bottomMargin = paddingBottom;
            cancel.setLayoutParams(lp);
        }
        lp = getCoordinatorLayoutParams(confirmCenter);
        if (lp != null) {
            if (iconButtonCount == 1) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            }
            lp.bottomMargin = paddingBottom;
            confirmCenter.setLayoutParams(lp);
        }
    }

    private CoordinatorLayout.LayoutParams getCoordinatorLayoutParams(View view) {
        return view.getLayoutParams() instanceof CoordinatorLayout.LayoutParams ?
                (CoordinatorLayout.LayoutParams) view.getLayoutParams() : null;
    }
}