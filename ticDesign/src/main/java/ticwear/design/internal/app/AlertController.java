/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.internal.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import ticwear.design.R;
import ticwear.design.app.AlertDialog;
import ticwear.design.widget.CheckedTextView;
import ticwear.design.widget.CoordinatorLayout;
import ticwear.design.widget.CursorRecyclerViewAdapter;
import ticwear.design.widget.FloatingActionButton;
import ticwear.design.widget.FloatingActionButton.DelayedConfirmationListener;
import ticwear.design.widget.FocusableLinearLayoutManager;
import ticwear.design.widget.FocusableLinearLayoutManager.ViewHolder;
import ticwear.design.widget.SubscribedScrollView;
import ticwear.design.widget.TicklableRecyclerView;
import ticwear.design.widget.TrackSelectionAdapterWrapper;
import ticwear.design.widget.TrackSelectionAdapterWrapper.OnItemClickListener;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class AlertController {

    static final int[] ENABLED_STATE_SET = {android.R.attr.state_enabled};
    static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};
    static final int[] EMPTY_STATE_SET = new int[0];

    private final Context mContext;
    private final DialogInterface mDialogInterface;
    private final Window mWindow;

    private CharSequence mTitle;
    private CharSequence mMessage;
    private TicklableRecyclerView mListView;
    private View mView;

    private int mViewLayoutResId;

    private int mViewSpacingLeft;
    private int mViewSpacingTop;
    private int mViewSpacingRight;
    private int mViewSpacingBottom;
    private boolean mViewSpacingSpecified = false;

    private final ButtonBundle mButtonBundlePositive;
    private final ButtonBundle mButtonBundleNegative;
    private final ButtonBundle mButtonBundleNeutral;

    private SubscribedScrollView mScrollView;

    private int mIconId = 0;
    private Drawable mIcon;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mMessageView;
    private View mCustomTitleView;

    private boolean mForceInverseBackground;

    private DelayConfirmRequest mDelayConfirmRequest;

    private TrackSelectionAdapterWrapper mAdapter;

    private int mAlertDialogLayout;
    private int mButtonPanelSideLayout;
    private int mListLayout;
    private int mMultiChoiceItemLayout;
    private int mSingleChoiceItemLayout;
    private int mListItemLayout;

    private int mButtonPanelLayoutHint = AlertDialog.LAYOUT_HINT_NONE;

    private Handler mHandler;

    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message m = mButtonBundlePositive.messageForButton(v);
            if (m == null) {
                m = mButtonBundleNegative.messageForButton(v);
            }
            if (m == null) {
                m = mButtonBundleNeutral.messageForButton(v);
            }

            if (m != null) {
                m.sendToTarget();
            }

            // Post a message so we dismiss after the above handlers are executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialogInterface)
                    .sendToTarget();
        }
    };

    private static final class ButtonHandler extends Handler {
        // Button clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;

        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;

                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        return true;
    }

    public AlertController(Context context, DialogInterface di, Window window) {
        mContext = context;
        mDialogInterface = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);

        TypedArray a = context.obtainStyledAttributes(null,
                R.styleable.AlertDialog,
                android.R.attr.alertDialogStyle, 0);

        mAlertDialogLayout = R.layout.alert_dialog_ticwear;
        mButtonPanelSideLayout = a.getResourceId(
                R.styleable.AlertDialog_android_buttonPanelSideLayout, 0);

        mListLayout = a.getResourceId(
                R.styleable.AlertDialog_tic_listLayout,
                R.layout.select_dialog_ticwear);
        mMultiChoiceItemLayout = a.getResourceId(
                R.styleable.AlertDialog_tic_multiChoiceItemLayout,
                android.R.layout.select_dialog_multichoice);
        mSingleChoiceItemLayout = a.getResourceId(
                R.styleable.AlertDialog_tic_singleChoiceItemLayout,
                android.R.layout.select_dialog_singlechoice);
        mListItemLayout = a.getResourceId(
                R.styleable.AlertDialog_tic_listItemLayout,
                android.R.layout.select_dialog_item);

        a.recycle();

        mButtonBundlePositive = new ButtonBundle();
        mButtonBundleNegative = new ButtonBundle();
        mButtonBundleNeutral = new ButtonBundle();
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }

        if (!(v instanceof ViewGroup)) {
            return false;
        }

        ViewGroup vg = (ViewGroup)v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }

        return false;
    }

    public void installContent() {
        /* We use a custom title so never request a window title */
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        int contentView = selectContentView();
        mWindow.setContentView(contentView);
        setupView();
        setupDecor();
    }

    private int selectContentView() {
        if (mButtonPanelSideLayout == 0) {
            return mAlertDialogLayout;
        }
        if (mButtonPanelLayoutHint == AlertDialog.LAYOUT_HINT_SIDE) {
            return mButtonPanelSideLayout;
        }
        // TODO: use layout hint side for long messages/lists
        return mAlertDialogLayout;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * @see AlertDialog.Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    /**
     * Set the view resource to display in the dialog.
     */
    public void setView(int layoutResId) {
        mView = null;
        mViewLayoutResId = layoutResId;
        mViewSpacingSpecified = false;
    }

    /**
     * Set the view to display in the dialog.
     */
    public void setView(View view) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = false;
    }

    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }

    /**
     * Sets a hint for the best button panel layout.
     */
    public void setButtonPanelLayoutHint(int layoutHint) {
        mButtonPanelLayoutHint = layoutHint;
    }

    /**
     * Sets a click listener or a message to be sent when the button is clicked.
     * You only need to pass one of {@code listener} or {@code msg}.
     *  @param whichButton Which button, can be one of
     *            {@link DialogInterface#BUTTON_POSITIVE},
     *            {@link DialogInterface#BUTTON_NEGATIVE}, or
     *            {@link DialogInterface#BUTTON_NEUTRAL}
     * @param text The text to display in button.
     * @param icon The icon to display in button.
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     * @param msg The {@link Message} to be sent when clicked.
     */
    public void setButton(int whichButton, CharSequence text, Drawable icon,
                          DialogInterface.OnClickListener listener, Message msg) {

        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }

        switch (whichButton) {

            case DialogInterface.BUTTON_POSITIVE:
                mButtonBundlePositive.buttonText = text;
                mButtonBundlePositive.buttonIcon = icon;
                mButtonBundlePositive.buttonMessage = msg;
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mButtonBundleNegative.buttonText = text;
                mButtonBundleNegative.buttonIcon = icon;
                mButtonBundleNegative.buttonMessage = msg;
                break;

            case DialogInterface.BUTTON_NEUTRAL:
                mButtonBundleNeutral.buttonText = text;
                mButtonBundleNeutral.buttonIcon = icon;
                mButtonBundleNeutral.buttonMessage = msg;
                break;

            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    /**
     * Specifies the icon to display next to the alert title.
     *
     * @param resId the resource identifier of the drawable to use as the icon,
     *            or 0 for no icon
     */
    public void setIcon(int resId) {
        mIcon = null;
        mIconId = resId;

        if (mIconView != null) {
            if (resId != 0) {
                mIconView.setImageResource(mIconId);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Specifies the icon to display next to the alert title.
     *
     * @param icon the drawable to use as the icon or null for no icon
     */
    public void setIcon(Drawable icon) {
        mIcon = icon;
        mIconId = 0;

        if (mIconView != null) {
            if (icon != null) {
                mIconView.setImageDrawable(icon);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * @param attrId the attributeId of the theme-specific drawable
     * to resolve the resourceId for.
     *
     * @return resId the resourceId of the theme-specific drawable
     */
    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mForceInverseBackground = forceInverseBackground;
    }


    public void setDelayConfirmAction(DelayConfirmRequest request) {
        mDelayConfirmRequest = request;
    }

    public TicklableRecyclerView getListView() {
        return mListView;
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonBundlePositive.textButton;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonBundleNegative.textButton;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonBundleNeutral.textButton;
            default:
                return null;
        }
    }

    public FloatingActionButton getIconButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonBundlePositive.iconButton;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonBundleNegative.iconButton;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonBundleNeutral.iconButton;
            default:
                return null;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    /**
     * Hide all buttons.
     *
     * NOTE: This only work if the buttons are icon button.
     */
    public void hideButtons() {
        mWindow.getDecorView().removeCallbacks(buttonRestoreRunnable);
        mButtonBundlePositive.hideButton();
        mButtonBundleNegative.hideButton();
        mButtonBundleNeutral.hideButton();
    }

    /**
     * Set all buttons minimize.
     *
     * NOTE: This only work if the buttons are icon button.
     */
    public void minimizeButtons() {
        mWindow.getDecorView().removeCallbacks(buttonRestoreRunnable);

        mButtonBundlePositive.minimizeButton();
        mButtonBundleNegative.minimizeButton();
        mButtonBundleNeutral.minimizeButton();
    }

    /**
     * Show all buttons after a period of time.
     *
     * NOTE: This only work if the buttons are icon button.
     */
    public void showButtonsDelayed() {
        mWindow.getDecorView().removeCallbacks(buttonRestoreRunnable);
        long timeout = mContext.getResources()
                .getInteger(R.integer.design_time_action_idle_timeout_short);
        mWindow.getDecorView().postDelayed(buttonRestoreRunnable, timeout);
    }

    public void showButtons() {
        mWindow.getDecorView().removeCallbacks(buttonRestoreRunnable);
        mButtonBundlePositive.showButton();
        mButtonBundleNegative.showButton();
        mButtonBundleNeutral.showButton();
    }

    private Runnable buttonRestoreRunnable = new Runnable() {
        @Override
        public void run() {
            showButtons();
        }
    };

    private void setupDecor() {
        final View decor = mWindow.getDecorView();
        final View parent = mWindow.findViewById(R.id.parentPanel);
        if (parent != null && decor != null) {
            decor.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    if (insets.isRound()) {
                        // TODO: Get the padding as a function of the window size.
                        int roundOffset = mContext.getResources().getDimensionPixelOffset(
                                R.dimen.alert_dialog_round_padding);
                        parent.setPadding(roundOffset, roundOffset, roundOffset, roundOffset);
                    }
                    return insets.consumeSystemWindowInsets();
                }
            });
            decor.setFitsSystemWindows(true);
            decor.requestApplyInsets();
        }
    }

    private void setupView() {
        final ViewGroup contentPanel = (ViewGroup) mWindow.findViewById(R.id.contentPanel);
        setupContent(contentPanel);
        final boolean hasButtons = setupButtons();

        final ViewGroup topPanel = (ViewGroup) mWindow.findViewById(R.id.topPanel);
        final TypedArray a = mContext.obtainStyledAttributes(
                null, R.styleable.AlertDialog, android.R.attr.alertDialogStyle, 0);
        final boolean hasTitle = setupTitle(topPanel);

        if (!hasButtons) {
            final View spacer = mWindow.findViewById(R.id.textSpacerNoButtons);
            if (spacer != null) {
                spacer.setVisibility(View.VISIBLE);
            }
//            mWindow.setCloseOnTouchOutsideIfNotSet(true);
        }

        final FrameLayout customPanel = (FrameLayout) mWindow.findViewById(R.id.customPanel);
        final View customView;
        if (mView != null) {
            customView = mView;
        } else if (mViewLayoutResId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            customView = inflater.inflate(mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }

        final boolean hasCustomView = customView != null;
        if (!hasCustomView || !canTextInput(customView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        if (hasCustomView) {
            final FrameLayout custom = (FrameLayout) mWindow.findViewById(R.id.custom);
            custom.addView(customView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

            if (mViewSpacingSpecified) {
                custom.setPadding(
                        mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom);
            }

            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }
        } else {
            customPanel.setVisibility(View.GONE);
        }

        a.recycle();

        final TicklableRecyclerView listView = mListView;
        if (listView != null && mAdapter != null) {
            listView.setAdapter(mAdapter);
        }
    }

    private boolean setupTitle(ViewGroup topPanel) {
        boolean hasTitle = true;

        if (mCustomTitleView != null) {
            // Add the custom title view directly to the topPanel layout
            LayoutParams lp = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            topPanel.addView(mCustomTitleView, 0, lp);

            // Hide the title template
            View titleTemplate = mWindow.findViewById(R.id.title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
            final boolean hasIconTitle = mIconId != 0 || mIcon != null;
            if (hasTextTitle || hasIconTitle) {
                mTitleView = (TextView) mWindow.findViewById(android.R.id.title);
                mIconView = (ImageView) mWindow.findViewById(android.R.id.icon);

                if (hasTextTitle) {
                    // Display the title if a title is supplied, else hide it.
                    mTitleView.setText(mTitle);
                } else {
                    mTitleView.setVisibility(View.GONE);
                }

                // Do this last so that if the user has supplied any icons we
                // use them instead of the default ones. If the user has
                // specified 0 then make it disappear.
                if (mIconId != 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else {
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                // Hide the title template
                final View titleTemplate = mWindow.findViewById(R.id.title_template);
                titleTemplate.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    // SuppressLint for View.setOnScrollChangeListener, witch is a hidden API before API 23.
    @SuppressLint("NewApi")
    private void setupContent(ViewGroup contentPanel) {
        mScrollView = (SubscribedScrollView) mWindow.findViewById(R.id.scrollView);
        mScrollView.setFocusable(false);

        // Special case for users that only want to display a String
        mMessageView = (TextView) mWindow.findViewById(R.id.message);
        if (mMessageView == null) {
            return;
        }

        if (mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);

            if (mListView != null) {
                final ViewGroup scrollParent = (ViewGroup) mScrollView.getParent();
                final int childIndex = scrollParent.indexOfChild(mScrollView);
                scrollParent.removeViewAt(childIndex);
                scrollParent.addView(mListView, childIndex,
                        new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }

        // Set up scroll indicators (if present).
        final View indicatorUp = mWindow.findViewById(R.id.scrollIndicatorUp);
        final View indicatorDown = mWindow.findViewById(R.id.scrollIndicatorDown);
        if (indicatorUp != null || indicatorDown != null) {
            OnViewScrollListener onViewScrollListener = new OnViewScrollListener() {

                int scrollState = SubscribedScrollView.OnScrollListener.SCROLL_STATE_IDLE;
                boolean scrollDown = true;

                @Override
                public void onViewScrollStateChanged(View view, int state) {
                    scrollState = state;

                    if (scrollState == SubscribedScrollView.OnScrollListener.SCROLL_STATE_IDLE) {
                        showButtonsDelayed();
                    } else if (!scrollDown) {
                        minimizeButtons();
                    }
                }

                @Override
                public void onViewScroll(View view, int l, int t, int oldl, int oldt) {
                    manageScrollIndicators(view, indicatorUp, indicatorDown);

                    if (t == oldt) {
                        return;
                    }

                    boolean newScrollDown = (t - oldt) < 0;
                    if (newScrollDown && !scrollDown) {
                        showButtons();
                        scrollDown = true;
                    } else if (!newScrollDown && scrollDown &&
                            scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        minimizeButtons();
                        scrollDown = false;
                    }
                }
            };
            if (mMessage != null) {
                // We're just showing the ScrollView, set up listener.
                mScrollView.setOnScrollListener(onViewScrollListener);
                // Set up the indicators following layout.
                mScrollView.post(new Runnable() {
                     @Override
                     public void run() {
                             manageScrollIndicators(mScrollView, indicatorUp, indicatorDown);
                         }
                     });

            } else if (mListView != null) {
                // We're just showing the AbsListView, set up listener.
                mListView.setOnScrollListener(onViewScrollListener);
                // Set up the indicators following layout.
                mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            manageScrollIndicators(mListView, indicatorUp, indicatorDown);
                        }
                    });
            } else {
                // We don't have any content to scroll, remove the indicators.
                if (indicatorUp != null) {
                    contentPanel.removeView(indicatorUp);
                }
                if (indicatorDown != null) {
                    contentPanel.removeView(indicatorDown);
                }
            }
        }
    }

    private static void manageScrollIndicators(View v, View upIndicator, View downIndicator) {
        if (upIndicator != null) {
            upIndicator.setVisibility(v.canScrollVertically(-1) ? View.VISIBLE : View.INVISIBLE);
        }
        if (downIndicator != null) {
            downIndicator.setVisibility(v.canScrollVertically(1) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private boolean setupButtons() {
        setupButtonBundles();
        int textButtonCount = setupTextButtons();
        int iconButtonCount = setupIconButtons();
        boolean hasButtons = textButtonCount > 0 || iconButtonCount > 0;
        boolean useTextButtons = textButtonCount > iconButtonCount;

        final View textButtonPanel = mWindow.findViewById(R.id.textButtonPanel);
        if (hasButtons && useTextButtons) {
            textButtonPanel.setVisibility(View.VISIBLE);
        } else {
            textButtonPanel.setVisibility(View.GONE);
        }

        offsetIconButtons(iconButtonCount);

        if (hasButtons && !useTextButtons && mDelayConfirmRequest != null) {
            FloatingActionButton fab = getIconButton(mDelayConfirmRequest.witchButton);
            if (fab != null) {
                fab.startDelayConfirmation(mDelayConfirmRequest.delayDuration, new DelayedConfirmationListener() {
                    @Override
                    public void onButtonClicked(FloatingActionButton fab) {
                    }

                    @Override
                    public void onTimerFinished(FloatingActionButton fab) {
                        fab.performClick();
                    }
                });
            }
        }

        return hasButtons;
    }

    private void setupButtonBundles() {
        mButtonBundlePositive.setup(mWindow, R.id.textButton1, R.id.textSpace1, R.id.iconButton1);
        mButtonBundleNegative.setup(mWindow, R.id.textButton2, R.id.textSpace2, R.id.iconButton2);
        mButtonBundleNeutral.setup(mWindow, R.id.textButton3, R.id.textSpace3, R.id.iconButton3);
    }

    private int setupTextButtons() {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;
        if (mButtonBundlePositive.setupTextButton(mButtonHandler)) {
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }
        if (mButtonBundleNegative.setupTextButton(mButtonHandler)) {
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }
        if (mButtonBundleNeutral.setupTextButton(mButtonHandler)) {
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

        return Integer.bitCount(whichButtons);
    }

    private int setupIconButtons() {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;
        if (mButtonBundlePositive.setupIconButton(mButtonHandler)) {
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }
        if (mButtonBundleNegative.setupIconButton(mButtonHandler)) {
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }
        if (mButtonBundleNeutral.setupIconButton(mButtonHandler)) {
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

        return Integer.bitCount(whichButtons);
    }

    private void offsetIconButtons(int iconButtonCount) {
        int paddingBottomUnit = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.alert_dialog_round_padding_bottom);
        int paddingBottom = iconButtonCount > 1 ? paddingBottomUnit * 2 : paddingBottomUnit;
        int paddingHorizontal;
        if (iconButtonCount == 3) {
            paddingHorizontal = mContext.getResources().getDimensionPixelOffset(
                    R.dimen.alert_dialog_round_button_padding_horizontal_full);
        } else if (iconButtonCount == 2) {
            paddingHorizontal = mContext.getResources().getDimensionPixelOffset(
                    R.dimen.alert_dialog_round_button_padding_horizontal_pair);
        } else {
            paddingHorizontal = 0;
        }

        CoordinatorLayout.LayoutParams lp;
        lp = getCoordinatorLayoutParams(mButtonBundlePositive.iconButton);
        if (lp != null) {
            if (iconButtonCount == 1) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            }
            lp.setMarginStart(paddingHorizontal);
            lp.setMarginEnd(paddingHorizontal);
            lp.bottomMargin = paddingBottom;
            mButtonBundlePositive.iconButton.setLayoutParams(lp);
        }
        lp = getCoordinatorLayoutParams(mButtonBundleNegative.iconButton);
        if (lp != null) {
            if (iconButtonCount == 1) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            }
            lp.setMarginStart(paddingHorizontal);
            lp.setMarginEnd(paddingHorizontal);
            lp.bottomMargin = paddingBottom;
            mButtonBundleNegative.iconButton.setLayoutParams(lp);
        }
        lp = getCoordinatorLayoutParams(mButtonBundleNeutral.iconButton);
        if (lp != null) {
            if (iconButtonCount == 1) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            }
            lp.bottomMargin = paddingBottom;
            mButtonBundleNeutral.iconButton.setLayoutParams(lp);
        }

        int translateY = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.alert_dialog_button_translate_vertical) +
                (paddingBottom - paddingBottomUnit);
        int translateX = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.alert_dialog_button_translate_horizontal);

        if (iconButtonCount == 2) {
            mButtonBundlePositive.setMinimizeTranslation(-translateX, translateY);
            mButtonBundleNegative.setMinimizeTranslation(translateX, translateY);
            mButtonBundleNeutral.setMinimizeTranslation(mButtonBundlePositive.hasIconButton() ?
                    translateX : -translateX, translateY);
        } else if (iconButtonCount == 3) {
            mButtonBundlePositive.setMinimizeTranslation(-translateX, translateY);
            mButtonBundleNegative.setMinimizeTranslation(translateX, translateY);
            mButtonBundleNeutral.setMinimizeTranslation(0, translateY);
        } else {
            mButtonBundlePositive.setMinimizeTranslation(0, translateY);
            mButtonBundleNegative.setMinimizeTranslation(0, translateY);
            mButtonBundleNeutral.setMinimizeTranslation(0, translateY);
        }

    }

    private CoordinatorLayout.LayoutParams getCoordinatorLayoutParams(View view) {
        return view.getLayoutParams() instanceof CoordinatorLayout.LayoutParams ?
                (CoordinatorLayout.LayoutParams) view.getLayoutParams() : null;
    }

    public static class AlertParams {
        public final Context mContext;
        public final LayoutInflater mInflater;

        public int mIconId = 0;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public CharSequence mMessage;
        public CharSequence mPositiveButtonText;
        public Drawable mPositiveButtonIcon;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mNegativeButtonText;
        public Drawable mNegativeButtonIcon;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNeutralButtonText;
        public Drawable mNeutralButtonIcon;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public boolean mCancelable;
        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public DelayConfirmRequest mDelayConfirmRequest;
        public CharSequence[] mItems;
        public RecyclerView.Adapter mAdapter;
        public DialogInterface.OnClickListener mOnClickListener;
        public int mViewLayoutResId;
        public View mView;
        public int mViewSpacingLeft;
        public int mViewSpacingTop;
        public int mViewSpacingRight;
        public int mViewSpacingBottom;
        public boolean mViewSpacingSpecified = false;
        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public int mCheckedItem = -1;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public Cursor mCursor;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public boolean mForceInverseBackground;
        public TrackSelectionAdapterWrapper.OnItemSelectedListener mOnItemSelectedListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;

        /**
         * Interface definition for a callback to be invoked before the ListView
         * will be bound to an adapter.
         */
        public interface OnPrepareListViewListener {

            /**
             * Called before the ListView is bound to an adapter.
             * @param listView The ListView that will be shown in the dialog.
             */
            void onPrepareListView(TicklableRecyclerView listView);
        }

        public AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void apply(AlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId != 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null || mPositiveButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonIcon, mPositiveButtonListener, null);
            }
            if (mNegativeButtonText != null || mNegativeButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonIcon, mNegativeButtonListener, null);
            }
            if (mNeutralButtonText != null || mNeutralButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonIcon, mNeutralButtonListener, null);
            }
            if (mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if (mDelayConfirmRequest != null) {
                dialog.setDelayConfirmAction(mDelayConfirmRequest);
            }
            // For a list, the client can either supply an array of items or an
            // adapter or a cursor
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            } else if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId);
            }

        }

        private void createListView(final AlertController dialog) {
            final TicklableRecyclerView listView = (TicklableRecyclerView)
                    mInflater.inflate(dialog.mListLayout, null);
            listView.setLayoutManager(new FocusableLinearLayoutManager(mContext));
            RecyclerView.Adapter adapter;

            final int layout = mIsSingleChoice
                    ? dialog.mSingleChoiceItemLayout
                    : (mIsMultiChoice ? dialog.mMultiChoiceItemLayout : dialog.mListItemLayout);
            if (mCursor == null) {
                adapter = (mAdapter != null) ? mAdapter
                        : new CheckedItemAdapter(mContext, layout, android.R.id.text1, mItems);
            } else {
                adapter = (mAdapter instanceof CursorRecyclerViewAdapter) ? mAdapter
                        : new CursorRecyclerViewAdapter(mContext, mCursor) {
                    private final int mLabelIndex;

                    {
                        final Cursor cursor = getCursor();
                        mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                    }

                    @Override
                    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
                        CheckedTextView text = (CheckedTextView) viewHolder.itemView.findViewById(android.R.id.text1);
                        text.setText(cursor.getString(mLabelIndex));
                    }

                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View itemView = mInflater.inflate(layout, parent, false);
                        return new ViewHolder(itemView);
                    }
                };
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            /* Don't directly set the adapter on the ListView as we might
             * want to add a footer to the ListView later.
             */
            dialog.mAdapter = new TrackSelectionAdapterWrapper<RecyclerView.ViewHolder>(adapter) {
                private final int mIsCheckedIndex;

                {
                    if (useCursorCheckedColumn()) {
                        final Cursor cursor = ((CursorRecyclerViewAdapter) getAdapter()).getCursor();
                        mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                    } else {
                        mIsCheckedIndex = -1;
                    }
                }
                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
                    if (useCursorCheckedColumn() && mIsCheckedIndex != -1) {
                        final Cursor cursor = ((CursorRecyclerViewAdapter) getAdapter()).getCursor();
                        setItemChecked(cursor.getPosition(), cursor.getInt(mIsCheckedIndex) == 1);
                    } else if (mCheckedItems != null) {
                        boolean isItemChecked = mCheckedItems[position];
                        if (isItemChecked != isItemChecked(position)) {
                            setItemChecked(position, isItemChecked);
                        }
                    } else if (mCheckedItem > -1) {
                        setItemChecked(mCheckedItem, true);
//                        setSelection(checkedItem);
                    }

                    // Update checked state then bind, so we can update the view state in this bind.
                    super.onBindViewHolder(viewHolder, position);
                }

                private boolean useCursorCheckedColumn() {
                    return mIsMultiChoice && getAdapter() instanceof CursorRecyclerViewAdapter;
                }
            };

            if (mOnClickListener != null) {
                dialog.mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(TrackSelectionAdapterWrapper<?> parent, View v, int position, long id) {
                        mCheckedItem = position;
                        mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                dialog.mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(TrackSelectionAdapterWrapper<?> parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = dialog.mAdapter.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(
                                dialog.mDialogInterface, position, dialog.mAdapter.isItemChecked(position));
                    }
                });
            }

            // Attach a given OnItemSelectedListener to the ListView
            if (mOnItemSelectedListener != null) {
                dialog.mAdapter.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                dialog.mAdapter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                dialog.mAdapter.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            dialog.mListView = listView;
        }
    }

    public static class DelayConfirmRequest {
        public final int witchButton;
        public final long delayDuration;

        public DelayConfirmRequest(int witchButton, long delayDuration) {
            this.witchButton = witchButton;
            this.delayDuration = delayDuration;
        }
    }

    private static class CheckedItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final Context mContext;
        private final int mLayoutResource;
        private final int mTextViewResourceId;
        private final CharSequence[] mObjects;

        public CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                CharSequence[] objects) {
            super();

            this.mContext = context;
            this.mLayoutResource = resource;
            this.mTextViewResourceId = textViewResourceId;
            this.mObjects = objects;

            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(mLayoutResource, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            TextView text1 = (TextView) viewHolder.itemView.findViewById(mTextViewResourceId);
            text1.setText(mObjects[position]);
        }

        @Override
        public int getItemCount() {
            return mObjects.length;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class ButtonBundle {
        private Button textButton;
        private Space textSpace;
        private FloatingActionButton iconButton;
        private CharSequence buttonText;
        private Drawable buttonIcon;
        private Message buttonMessage;

        public void setup(Window window,
                          @IdRes int textButtonId, @IdRes int textSpaceId,
                          @IdRes int iconButtonId) {
            textButton = (Button) window.findViewById(textButtonId);
            textSpace = (Space) window.findViewById(textSpaceId);
            iconButton = (FloatingActionButton) window.findViewById(iconButtonId);
        }

        public Message messageForButton(View v) {
            if ((v == textButton || v == iconButton) && buttonMessage != null) {
                return Message.obtain(buttonMessage);
            }

            return null;
        }

        public boolean setupTextButton(@Nullable View.OnClickListener l) {
            textButton.setOnClickListener(l);

            if (TextUtils.isEmpty(buttonText)) {
                textButton.setVisibility(View.GONE);
                textSpace.setVisibility(View.GONE);
                return false;
            } else {
                textButton.setText(buttonText);
                textButton.setVisibility(View.VISIBLE);
                textSpace.setVisibility(View.INVISIBLE);
                return true;
            }
        }

        public boolean setupIconButton(@Nullable View.OnClickListener l) {
            iconButton.setOnClickListener(l);

            if (buttonIcon == null) {
                iconButton.setVisibility(View.GONE);
                return false;
            } else {
                setupIconContent();
                iconButton.setVisibility(View.VISIBLE);
                return true;
            }
        }

        public void setMinimizeTranslation(int x, int y) {
            if (hasIconButton()) {
                iconButton.setMinimizeTranslation(x, y);
            }
        }

        public void hideButton() {
            if (hasIconButton()) {
                iconButton.hide();
            }
        }

        public void minimizeButton() {
            if (hasIconButton()) {
                iconButton.minimize();
            }
        }

        public void showButton() {
            if (hasIconButton()) {
                iconButton.show();
            }
        }

        private boolean hasIconButton() {
            return buttonIcon != null || iconButton.getVisibility() == View.VISIBLE;
        }

        private void setupIconContent() {
            iconButton.setImageDrawable(
                    getStatefulDrawable(iconButton.getContext(), buttonIcon));
            ColorStateList stateList = iconButton.getBackgroundTintList();
            iconButton.setBackgroundTintList(
                    getStatefulColorList(iconButton.getContext(), stateList));
        }

        private static Drawable getStatefulDrawable(@NonNull  Context context,
                                                    @Nullable Drawable drawable) {
            StateListDrawable stateful = null;
            if (drawable != null) {
                if (drawable instanceof StateListDrawable) {
                    stateful = (StateListDrawable) drawable;
                } else {
                    stateful = new StateListDrawable();
                    stateful.addState(ENABLED_STATE_SET, drawable);
                }

                int alpha = getAlphaValue(context);

                Drawable disabled = drawable.getConstantState().newDrawable().mutate();
                disabled.setAlpha(alpha);
                stateful.addState(DISABLED_STATE_SET, disabled);
            }

            return stateful;
        }

        private static ColorStateList getStatefulColorList(@NonNull  Context context,
                                                           @Nullable ColorStateList stateList) {
            ColorStateList filledList = stateList;
            if (stateList != null) {
                int normalColor = stateList.getDefaultColor();
                int[][] states = {
                        ENABLED_STATE_SET,
                        DISABLED_STATE_SET
                };
                int[] colors = {
                        normalColor,
                        normalColor & 0xffffff | (getAlphaValue(context) << 24)
                };
                filledList = new ColorStateList(states, colors);
            }

            return filledList;
        }

        private static int getAlphaValue(@NonNull  Context context) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, typedValue, true);
            float disabledAlpha = typedValue.getFloat();

            return (int) (0xff * disabledAlpha);
        }
    }


    private abstract class OnViewScrollListener extends RecyclerView.OnScrollListener implements
            SubscribedScrollView.OnScrollListener {

        private int oldScrollY = 0;
        private int oldScrollX = 0;

        @Override
        public void onScrollStateChanged(RecyclerView view, int scrollState) {
            onViewScrollStateChanged(view, scrollState);
        }

        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            int scrollX = view.computeHorizontalScrollOffset();
            int scrollY = view.computeVerticalScrollOffset();
            onViewScroll(view, scrollX, scrollY, oldScrollX, oldScrollY);
            oldScrollX = scrollX;
            oldScrollY = scrollY;
        }

        @Override
        public void onScrollStateChanged(SubscribedScrollView view, int scrollState) {
            onViewScrollStateChanged(view, scrollState);
        }

        @Override
        public void onScroll(SubscribedScrollView view, int l, int t, int oldl, int oldt) {
            onViewScroll(view, l, t, oldl, oldt);
        }


        public abstract void onViewScrollStateChanged(View view, int scrollState);

        public abstract void onViewScroll(View view, int l, int t, int oldl, int oldt);
    }
}
