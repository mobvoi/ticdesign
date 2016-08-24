package ticwear.design.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import ticwear.design.R;
import ticwear.design.widget.FloatingActionButton;

public class ConfirmDialog extends Dialog implements View.OnClickListener {

    private TextView mTitleTv;
    private TextView mMessageTv;
    private TextView mConfirmTv;
    private CheckBox mCheckbox;
    private FloatingActionButton mPositiveBtn;

    /**
     * 如果选择按钮不为选中，是否需要禁用确认按钮，默认为需要禁用
     */
    private boolean mShouldDisableButton = true;

    private Callback mCallback;

    @SuppressLint("PrivateResource")
    public ConfirmDialog(Context context) {
        this(context, R.style.Theme_Ticwear_Dialog_Alert);
        init();
    }

    public ConfirmDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    private void init() {
        setContentView(R.layout.confirm_dialog_ticwear);
        mTitleTv = (TextView) findViewById(android.R.id.title);
        mMessageTv = (TextView) findViewById(R.id.message);
        mConfirmTv = (TextView) findViewById(R.id.confirm);
        mCheckbox = (CheckBox) findViewById(R.id.checkbox);
        FloatingActionButton negativeBtn = (FloatingActionButton) findViewById(R.id.cancel);
        mPositiveBtn = (FloatingActionButton) findViewById(R.id.ok);

        negativeBtn.setOnClickListener(this);
        mPositiveBtn.setOnClickListener(this);
        mCheckbox.setOnClickListener(this);

        updateButton(false);
    }

    public void setShouldDisableButton(boolean shouldDisable) {
        mShouldDisableButton = shouldDisable;
        updateButton(mCheckbox.isChecked());
    }

    public void setTitle(@StringRes int res) {
        mTitleTv.setText(res);
    }

    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            mTitleTv.setText(title);
        }
    }

    public void setMessage(@StringRes int res) {
        mMessageTv.setText(res);
    }

    public void setMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            mMessageTv.setText(message);
        }
    }

    public void setConfirmText(@StringRes int res) {
        mConfirmTv.setText(res);
    }

    public void setConfirmText(String confirmText) {
        if (!TextUtils.isEmpty(confirmText)) {
            mConfirmTv.setText(confirmText);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            if (mCallback != null) {
                mCallback.onCancel(mCheckbox.isChecked());
            }
        } else if (id == R.id.ok) {
            if (mCallback != null) {
                mCallback.onConfirm(mCheckbox.isChecked());
            }
        } else if (id == R.id.checkbox) {
            updateButton(mCheckbox.isChecked());
        }
    }

    private void updateButton(boolean checked) {
        if (mShouldDisableButton && !checked) {
            mPositiveBtn.setEnabled(false);
            mPositiveBtn.setAlpha(0.5f);
        } else {
            mPositiveBtn.setEnabled(true);
            mPositiveBtn.setAlpha(1.0f);
        }
    }

    public interface Callback {
        void onConfirm(boolean checked);
        void onCancel(boolean checked);
    }
}