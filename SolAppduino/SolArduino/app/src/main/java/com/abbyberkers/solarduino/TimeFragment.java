package com.abbyberkers.solarduino;

/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.format.DateFormat;

import android.support.v4.app.DialogFragment;

import com.abbyberkers.solarduino.MainActivity;

public class TimeFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    boolean start;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour = getArguments().getInt("hour");
        int minute = getArguments().getInt("minute");

        String title = getArguments().getString("title");
        start = getArguments().getBoolean("start");

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

        TextView titleTV = new TextView(getActivity());
        titleTV.setText(title);
        titleTV.setTextColor(Color.WHITE);
        titleTV.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        titleTV.setPadding(5, 3, 5, 3);
        titleTV.setGravity(Gravity.CENTER_HORIZONTAL);
        timePickerDialog.setCustomTitle(titleTV);
        return timePickerDialog;

    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        ((MainActivity) getActivity()).timePass(start, hour, minute);
    }
}
