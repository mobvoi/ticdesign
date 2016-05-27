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

package ticwear.design.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ticwear.design.R;
import ticwear.design.widget.TimePicker;

class TimePickerViewHolder {

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private TimePicker mTimePicker;

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourView;

    private final Context mContext;

    public TimePickerViewHolder(Context context) {
        this.mContext = context;
    }

    public TimePicker init(ViewGroup parent, int hourOfDay, int minute, boolean is24HourView,
                           TimePicker.OnTimeChangedListener listener,
                           TimePicker.ValidationCallback callback) {

        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourView = is24HourView;

        final LayoutInflater inflater = LayoutInflater.from(mContext);

        mTimePicker = (TimePicker) inflater.inflate(R.layout.dialog_time_picker, parent, false);
        mTimePicker.setIs24HourView(mIs24HourView);
        mTimePicker.setCurrentHour(mInitialHourOfDay);
        mTimePicker.setCurrentMinute(mInitialMinute);
        mTimePicker.setOnTimeChangedListener(listener);
        mTimePicker.setValidationCallback(callback);

        return mTimePicker;
    }

    /**
     * Gets the {@link TimePicker} contained in this dialog.
     *
     * @return The TimePicker view.
     */
    public TimePicker getTimePicker() {
        return mTimePicker;
    }

    /**
     * Sets the current time.
     *
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current minute within the hour.
     */
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour);
    }

    public void onSaveInstanceState(Bundle state) {
        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        final int hour = savedInstanceState.getInt(HOUR);
        final int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }

}