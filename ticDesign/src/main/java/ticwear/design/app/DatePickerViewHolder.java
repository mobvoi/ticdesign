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
import ticwear.design.widget.DatePicker;

class DatePickerViewHolder {
    static final String YEAR = "year";
    static final String MONTH = "month";
    static final String DAY = "day";

    private final Context mContext;

    private DatePicker mDatePicker;

    public DatePickerViewHolder(Context context) {
        this.mContext = context;
    }

    public DatePicker init(ViewGroup parent, int year, int monthOfYear, int dayOfMonth,
                           DatePicker.OnDateChangedListener listener,
                           DatePicker.ValidationCallback callback) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        mDatePicker = (DatePicker) inflater.inflate(R.layout.dialog_date_picker, parent, false);
        mDatePicker.init(year, monthOfYear, dayOfMonth, listener);
        mDatePicker.setValidationCallback(callback);

        return mDatePicker;
    }

    /**
     * Gets the {@link DatePicker} contained in this dialog.
     *
     * @return The calendar view.
     */
    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    /**
     * Sets the current date.
     *
     * @param year        The date year.
     * @param monthOfYear The date month.
     * @param dayOfMonth  The date day of month.
     */
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public void onSaveInstanceState(Bundle state) {
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        final int year = savedInstanceState.getInt(YEAR);
        final int month = savedInstanceState.getInt(MONTH);
        final int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, null);
    }

}