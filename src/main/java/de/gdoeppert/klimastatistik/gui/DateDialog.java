package de.gdoeppert.klimastatistik.gui;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class DateDialog extends DialogFragment {

    OnDateSetListener ondateSet;

    public DateDialog() {
    }

    public void setCallBack(OnDateSetListener ondate) {
        ondateSet = ondate;
    }

    private int year, month, day;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        year = args.getInt("year");
        month = args.getInt("month");
        day = args.getInt("day");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dia = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth, ondateSet, year, month, day);

        return dia;
    }

}