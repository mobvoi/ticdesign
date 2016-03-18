package ticwear.design.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

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

    public View init(int year, int monthOfYear, int dayOfMonth,
                     DatePicker.OnDateChangedListener listener,
                     DatePicker.ValidationCallback callback) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.dialog_date_picker, null);

        mDatePicker = (DatePicker) view.findViewById(R.id.tic_datePicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, listener);
        mDatePicker.setValidationCallback(callback);

        return view;
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