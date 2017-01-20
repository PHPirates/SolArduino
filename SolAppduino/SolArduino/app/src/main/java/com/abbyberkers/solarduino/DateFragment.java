package com.abbyberkers.solarduino;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.TextView;

public class DateFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    boolean start;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Use the current date as the default date in the picker
        int year = getArguments().getInt("year"); //use the already chosen date from Main
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");
        start = getArguments().getBoolean("start");

        String title = getArguments().getString("title");

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

        // TextView that contains title
        TextView titleTV = new TextView(getActivity());
        titleTV.setText(title);
        titleTV.setTextColor(Color.WHITE);
        titleTV.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        titleTV.setPadding(5, 3, 5, 3);
        titleTV.setGravity(Gravity.CENTER_HORIZONTAL);

        datePickerDialog.setCustomTitle(titleTV);
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
//        String alarmType = getArguments().getString("alarmType");
//        if (alarmType != null) {
            ((MainActivity) getActivity()).datePass(start, year, month, day); //send data to showreminder

//        } else {
//            ((MainActivity) getActivity()).datePass(year, month, day); //send data to showalarm
//        }
    }


}
