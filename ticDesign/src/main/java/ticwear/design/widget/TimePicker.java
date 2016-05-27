/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2007 The Android Open Source Project
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

package ticwear.design.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

import java.util.Locale;

import ticwear.design.R;

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode. The
 * hour, each minute digit, and AM/PM (if applicable) can be conrolled by
 * vertical spinners. The hour can be entered by keyboard input. Entering in two
 * digit hours can be accomplished by hitting two digits within a timeout of
 * about a second (e.g. '1' then '2' to select 12). The minutes can be entered
 * by entering single digits. Under AM/PM mode, the user can hit 'a', 'A", 'p'
 * or 'P' to pick. For a dialog using this view, see
 * {@link android.app.TimePickerDialog}.
 * <p>
 * See the <a href="{@docRoot}guide/topics/ui/controls/pickers.html">Pickers</a>
 * guide.
 * </p>
 */
public class TimePicker extends FrameLayout implements MultiPickerContainer,
        SidePanelEventDispatcher, View.OnFocusChangeListener {

    private static final int MODE_SPINNER = 1;
//    private static final int MODE_CLOCK = 2;

    private final TimePickerDelegate mDelegate;

    private MultiPickerClient mMultiPickerClient;

    private final GestureDetector mGestureDetector;
    private final OnGestureListener mOnGestureListener = new OnGestureListener();

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(TimePicker view, int hourOfDay, int minute);
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.timePickerStyle);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        final int mode = a.getInt(R.styleable.TimePicker_android_timePickerMode, MODE_SPINNER);
        a.recycle();

        switch (mode) {
//            case MODE_CLOCK:
//                mDelegate = new TimePickerClockDelegate(
//                        this, context, attrs, defStyleAttr, defStyleRes);
//                break;
            case MODE_SPINNER:
            default:
                mDelegate = new TimePickerSpinnerDelegate(
                        this, context, attrs, defStyleAttr, defStyleRes);
                break;
        }

        mGestureDetector = new GestureDetector(context, mOnGestureListener);
    }

    @Override
    public void setMultiPickerClient(MultiPickerClient client) {
        mMultiPickerClient = client;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        mDelegate.setCurrentHour(currentHour);
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        return mDelegate.getCurrentHour();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        mDelegate.setCurrentMinute(currentMinute);
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mDelegate.getCurrentMinute();
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        mDelegate.setIs24HourView(is24HourView);
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mDelegate.is24HourView();
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mDelegate.setOnTimeChangedListener(onTimeChangedListener);
    }

    /**
     * Sets the callback that indicates the current time is valid.
     *
     * @param callback the callback, may be null
     * @hide
     */
    public void setValidationCallback(@Nullable ValidationCallback callback) {
        mDelegate.setValidationCallback(callback);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mDelegate.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return mDelegate.isEnabled();
    }

    @Override
    public int getBaseline() {
        return mDelegate.getBaseline();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged(newConfig);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return mDelegate.onSaveInstanceState(superState);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BaseSavedState ss = (BaseSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mDelegate.onRestoreInstanceState(ss);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        mDelegate.onPopulateAccessibilityEvent(event);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        mDelegate.onInitializeAccessibilityEvent(event);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        mDelegate.onInitializeAccessibilityNodeInfo(info);
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev, @NonNull SuperCallback superCallback) {
        mGestureDetector.onTouchEvent(ev);
        return superCallback.superDispatchTouchSidePanelEvent(ev);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (mMultiPickerClient != null && hasFocus) {
            mMultiPickerClient.onPickerPostFocus((NumberPicker) v);
        }
    }

    /**
     * Change to next focus.
     *
     * @return true if focused to last picker.
     */
    private boolean nextFocus() {
        View focusedView = mDelegate.getCurrentFocusedPicker();
        View nextView = mDelegate.getNextFocusPicker(focusedView);

        if (nextView == null) {
            return false;
        }

        boolean fromLast = false;
        if (focusedView instanceof NumberPicker) {
            TextView input = (TextView) focusedView.findViewById(R.id.numberpicker_input);
            int options = input.getImeOptions();
            fromLast = options == EditorInfo.IME_ACTION_DONE;
        }

        boolean handled = false;
        if (mMultiPickerClient != null) {
            handled = mMultiPickerClient.onPickerPreFocus((NumberPicker) nextView, fromLast);
        }

        if (!handled) {
            nextView.requestFocus();
        }

        if (nextView instanceof NumberPicker) {
            TextView input = (TextView) nextView.findViewById(R.id.numberpicker_input);
            int options = input.getImeOptions();
            return options == EditorInfo.IME_ACTION_DONE;
        }

        return false;
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            nextFocus();
            return true;
        }
    }

    /**
     * A delegate interface that defined the public API of the TimePicker. Allows different
     * TimePicker implementations. This would need to be implemented by the TimePicker delegates
     * for the real behavior.
     */
    interface TimePickerDelegate {
        void setCurrentHour(Integer currentHour);
        Integer getCurrentHour();

        void setCurrentMinute(Integer currentMinute);
        Integer getCurrentMinute();

        void setIs24HourView(Boolean is24HourView);
        boolean is24HourView();

        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);
        void setValidationCallback(ValidationCallback callback);

        void setEnabled(boolean enabled);
        boolean isEnabled();

        int getBaseline();

        void onConfigurationChanged(Configuration newConfig);

        Parcelable onSaveInstanceState(Parcelable superState);
        void onRestoreInstanceState(Parcelable state);

        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);
        void onPopulateAccessibilityEvent(AccessibilityEvent event);
        void onInitializeAccessibilityEvent(AccessibilityEvent event);
        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info);

        View getCurrentFocusedPicker();
        View getNextFocusPicker(View current);
    }

    /**
     * A callback interface for updating input validity when the TimePicker
     * when included into a Dialog.
     *
     * @hide
     */
    public static interface ValidationCallback {
        void onValidationChanged(boolean valid);
    }

    /**
     * An abstract class which can be used as a start for TimePicker implementations
     */
    abstract static class AbstractTimePickerDelegate implements TimePickerDelegate {
        // The delegator
        protected TimePicker mDelegator;

        // The context
        protected Context mContext;

        // The current locale
        protected Locale mCurrentLocale;

        // Callbacks
        protected OnTimeChangedListener mOnTimeChangedListener;
        protected ValidationCallback mValidationCallback;

        public AbstractTimePickerDelegate(TimePicker delegator, Context context) {
            mDelegator = delegator;
            mContext = context;

            // initialization based on locale
            setCurrentLocale(Locale.getDefault());
        }

        public void setCurrentLocale(Locale locale) {
            if (locale.equals(mCurrentLocale)) {
                return;
            }
            mCurrentLocale = locale;
        }

        @Override
        public void setValidationCallback(ValidationCallback callback) {
            mValidationCallback = callback;
        }

        protected void onValidationChanged(boolean valid) {
            if (mValidationCallback != null) {
                mValidationCallback.onValidationChanged(valid);
            }
        }

        @Override
        public View getCurrentFocusedPicker() {
            return null;
        }

        @Override
        public View getNextFocusPicker(View current) {
            return null;
        }
    }
}
