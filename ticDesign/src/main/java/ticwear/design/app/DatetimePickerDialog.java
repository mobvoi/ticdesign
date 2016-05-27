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

package ticwear.design.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ticwear.design.DesignConfig;
import ticwear.design.R;
import ticwear.design.widget.CirclePageIndicator;
import ticwear.design.widget.DatePicker;
import ticwear.design.widget.DatePicker.OnDateChangedListener;
import ticwear.design.widget.MultiPickerContainer;
import ticwear.design.widget.NumberPicker;
import ticwear.design.widget.TicklableFrameLayout;
import ticwear.design.widget.TimePicker;

/**
 * A simple dialog containing an {@link DatePicker} and {@link TimePicker}.
 */
public class DatetimePickerDialog extends AlertDialog implements OnClickListener,
        OnDateChangedListener, TimePicker.OnTimeChangedListener,
        ViewPager.OnPageChangeListener, View.OnClickListener,
        MultiPickerContainer.MultiPickerClient, SidePanelEventDispatcher {

    static final String LOG_TAG = "DTPDialog";

    public static final int PAGE_FLAG_DATE = 1;
    public static final int PAGE_FLAG_TIME = 1 << 1;

    private OnCalendarSetListener mOnCalendarSetListener;
    private Calendar mCurrentCalendar;
    private DatePickerViewHolder mDatePickerViewHolder;
    private TimePickerViewHolder mTimePickerViewHolder;

    private ViewPager mViewPager;
    private PickerPagerAdapter mPagerAdapter;
    private CirclePageIndicator mPageIndicator;

    private boolean mOnLastPage = false;
    private boolean mIsSidePanelTouching = false;

    private NumberPicker mFocusedPicker;

    /**
     * @param context The context the dialog is to run in.
     * @param pageFlag Witch page will show.
     * @param defaultCalendar The initial datetime of the dialog.
     */
    public DatetimePickerDialog(Context context, int pageFlag, Calendar defaultCalendar) {
        this(context, 0, pageFlag, defaultCalendar);
    }

    @StyleRes
    static int resolveDialogTheme(Context context, @StyleRes int resId) {
        if (resId == 0) {
            final TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.datePickerDialogTheme, outValue, true);
            return outValue.resourceId;
        } else {
            return resId;
        }
    }

    /**
     * @param context The context the dialog is to run in.
     * @param theme the theme to apply to this dialog
     * @param pageFlag Witch page will show.
     * @param defaultCalendar The initial datetime of the dialog.
     */
    public DatetimePickerDialog(Context context, @StyleRes int theme, int pageFlag,
                                Calendar defaultCalendar) {
        super(context, resolveDialogTheme(context, theme));

        // Use getContext to use wrapper context.
        context = getContext();

        mCurrentCalendar = defaultCalendar;
        mCurrentCalendar.clear(Calendar.SECOND);

        boolean hasDateView = (pageFlag & PAGE_FLAG_DATE) == PAGE_FLAG_DATE;
        boolean hasTimeView = (pageFlag & PAGE_FLAG_TIME) == PAGE_FLAG_TIME;

        int year = defaultCalendar.get(Calendar.YEAR);
        int month = defaultCalendar.get(Calendar.MONTH);
        int day = defaultCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = defaultCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = defaultCalendar.get(Calendar.MINUTE);

        ValidationCallback validationCallback = new ValidationCallback() {
            @Override
            public void onValidationChanged(boolean valid) {
                final Button positive = getButton(BUTTON_POSITIVE);
                if (positive != null) {
                    positive.setEnabled(valid);
                }
            }
        };

        final Context themeContext = getContext();
        final LayoutInflater inflater = LayoutInflater.from(themeContext);
        TicklableFrameLayout container = (TicklableFrameLayout)
                inflater.inflate(R.layout.dialog_datetime_picker, null);

        container.setSidePanelEventDispatcher(this);

        mViewPager = (ViewPager) container.findViewById(R.id.tic_datetimeContainer);

        List<View> pages = new ArrayList<>(Integer.bitCount(pageFlag));
        if (hasDateView) {
            mDatePickerViewHolder = new DatePickerViewHolder(context);
            DatePicker dateView = mDatePickerViewHolder.init(mViewPager,
                    year, month, day,
                    this, validationCallback);
            dateView.setMultiPickerClient(this);
            dateView.setTag(R.id.title_template, R.string.date_picker_dialog_title);
            pages.add(dateView);
        }
        if (hasTimeView) {
            mTimePickerViewHolder = new TimePickerViewHolder(context);
            TimePicker timeView = mTimePickerViewHolder.init(mViewPager,
                    hour, minute, DateFormat.is24HourFormat(context),
                    this, validationCallback);
            timeView.setMultiPickerClient(this);
            timeView.setTag(R.id.title_template, R.string.time_picker_dialog_title);
            pages.add(timeView);
        }
        mPagerAdapter = new PickerPagerAdapter(pages);
        mViewPager.setAdapter(mPagerAdapter);

        mPageIndicator = (CirclePageIndicator) container.findViewById(R.id.tic_datetimeIndicator);
        mPageIndicator.setViewPager(mViewPager);
        mPageIndicator.setOnPageChangeListener(this);

        if (mPagerAdapter.getCount() < 2) {
            mPageIndicator.setVisibility(View.GONE);
        }

        setView(container);
        setButton(BUTTON_POSITIVE, getContext().getDrawable(R.drawable.tic_ic_btn_next), this);
        setButtonPanelLayoutHint(LAYOUT_HINT_SIDE);

        setTitle(mPagerAdapter.getPageTitle(0));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onPageSelected(0);
    }

    /**
     * @param listener How the parent is notified that the datetime is set.
     */
    public void setOnCalendarSetListener(OnCalendarSetListener listener) {
        this.mOnCalendarSetListener = listener;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mCurrentCalendar.set(year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mCurrentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCurrentCalendar.set(Calendar.MINUTE, minute);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                onConfirm();
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    /**
     * Click on custom acton positive button.
     */
    @Override
    public void onClick(View v) {
        if (mOnLastPage) {
            onConfirm();
            dismiss();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    private void onConfirm() {
        if (mOnCalendarSetListener != null) {
            mOnCalendarSetListener.onCalendarSet(this, mCurrentCalendar);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mOnLastPage = position == mPagerAdapter.getCount() - 1;
        updateButton();

        setTitle(mPagerAdapter.getPageTitle(position));
    }

    private void updateButton() {
        ImageButton button = getIconButton(BUTTON_POSITIVE);
        if (button != null) {
            if (mOnLastPage) {
                button.setImageResource(R.drawable.tic_ic_btn_ok);
                button.setOnClickListener(this);
            } else {
                button.setImageResource(R.drawable.tic_ic_btn_next);
                button.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (!mIsSidePanelTouching) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                showButtons();
            } else {
                minimizeButtons();
            }
        }
    }

    /**
     * Gets the {@link DatePicker} contained in this dialog.
     *
     * @return The calendar view.
     */
    public DatePicker getDatePicker() {
        return mDatePickerViewHolder.getDatePicker();
    }

    /**
     * Gets the {@link TimePicker} contained in this dialog.
     *
     * @return The TimePicker view.
     */
    public TimePicker getTimePicker() {
        return mTimePickerViewHolder.getTimePicker();
    }

    /**
     * Sets the current date.
     *
     * @param year The date year.
     * @param monthOfYear The date month.
     * @param dayOfMonth The date day of month.
     */
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePickerViewHolder.updateDate(year, monthOfYear, dayOfMonth);
    }

    /**
     * Sets the current time.
     *
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current minute within the hour.
     */
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePickerViewHolder.updateTime(hourOfDay, minuteOfHour);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        mDatePickerViewHolder.onSaveInstanceState(state);

        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDatePickerViewHolder.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onPickerPreFocus(NumberPicker numberPicker, boolean fromLast) {
        if (DesignConfig.DEBUG_PICKERS) {
            Log.d(LOG_TAG, "pre focus from last? " + fromLast + ", for " + numberPicker);
        }
        if (fromLast) {
            ImageButton button = getIconButton(BUTTON_POSITIVE);
            onClick(button);
            return true;
        }
        return false;
    }

    @Override
    public void onPickerPostFocus(@NonNull NumberPicker numberPicker) {
        if (DesignConfig.DEBUG_PICKERS) {
            Log.d(LOG_TAG, "focused on " + numberPicker);
        }
        if (mFocusedPicker != null) {
            mFocusedPicker.setOnScrollListener(null);
        }
        mFocusedPicker = numberPicker;
        mFocusedPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, @ScrollState int scrollState) {
                if (!mIsSidePanelTouching) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        showButtonsDelayed();
                    } else {
                        minimizeButtons();
                    }
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent event, @NonNull SuperCallback superCallback) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsSidePanelTouching = true;
            minimizeButtons();
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            mIsSidePanelTouching = false;
            showButtonsDelayed();
        }
        return superCallback.superDispatchTouchSidePanelEvent(event);
    }

    private class PickerPagerAdapter extends PagerAdapter {

        private List<View> mPickerPages;

        public PickerPagerAdapter(List<View> pages) {
            mPickerPages = pages;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mPickerPages.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mPickerPages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            View page = mPickerPages.get(position);
            Object tagTitle = page.getTag(R.id.title_template);
            return tagTitle instanceof Integer ? getContext().getString((int) tagTitle) : null;
        }

        public View getItemPage(int position) {
            return mPickerPages.get(position);
        }
    }

    private interface ValidationCallback extends
            DatePicker.ValidationCallback, TimePicker.ValidationCallback {
    }

    /**
     * The callback used to indicate the user is done filling in the date or time.
     */
    public interface OnCalendarSetListener {

        /**
         * @param dialog DatetimePickerDialog.
         * @param calendar The new set datetime.
         */
        void onCalendarSet(DatetimePickerDialog dialog, Calendar calendar);
    }

    public static class Builder {

        private final Context mContext;

        @StyleRes
        private int theme;
        private int pageFlag;
        private OnCalendarSetListener listener;
        private Calendar defaultCalendar;

        public Builder(Context context) {
            this.mContext = context;
            // Enable all pickers.
            this.pageFlag = PAGE_FLAG_DATE | PAGE_FLAG_TIME;
        }

        public Builder theme(@StyleRes int theme) {
            this.theme = theme;
            return this;
        }

        public Builder disableTimePicker() {
            this.pageFlag &= ~PAGE_FLAG_TIME;
            return this;
        }

        public Builder enableTimePicker() {
            this.pageFlag |= PAGE_FLAG_TIME;
            return this;
        }

        public Builder disableDatePicker() {
            this.pageFlag &= ~PAGE_FLAG_DATE;
            return this;
        }

        public Builder enableDatePicker() {
            this.pageFlag |= PAGE_FLAG_DATE;
            return this;
        }

        public Builder listener(OnCalendarSetListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder defaultValue(Calendar calendar) {
            this.defaultCalendar = calendar;
            return this;
        }

        public DatetimePickerDialog create() {
            DatetimePickerDialog dialog = new DatetimePickerDialog(mContext, theme,
                    pageFlag, defaultCalendar);
            dialog.setOnCalendarSetListener(listener);
            return dialog;
        }

        public DatetimePickerDialog show() {
            final DatetimePickerDialog dialog = create();
            dialog.show();
            return dialog;
        }

    }
}
