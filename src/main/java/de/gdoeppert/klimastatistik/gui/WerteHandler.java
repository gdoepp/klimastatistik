package de.gdoeppert.klimastatistik.gui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 09.01.16.
 */
public abstract class WerteHandler extends KlimaHandler implements DatePickerDialog.OnDateSetListener {

    protected final String[] monatsnamen = {"Ges", "Januar", "Februar", "März", "April",
            "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
    protected final String[] monatsnamen_k = {"Ges", "Jan", "Feb", "März", "April",
            "Mai", "Juni", "Juli", "Aug", "Sep", "Okt", "Nov", "Dez"};


    protected void showDatePicker() {
        DateDialog date = new DateDialog();

        Bundle args = new Bundle();
        args.putInt("year", activity.getHeute().get(Calendar.YEAR));
        args.putInt("month", activity.getHeute().get(Calendar.MONTH));
        args.putInt("day", activity.getHeute().get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);

        date.setCallBack(this);
        date.show(getActivity().getFragmentManager(), "Date Picker");
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        activity.dateChange(dayOfMonth, monthOfYear, year);
    }

    public void onClick(View v) {

        if (v.getId() == R.id.tag_heute ||
                v.getId() == R.id.jahr_heute) {
            showDatePicker();
            return;
        } else if (Button.class.isAssignableFrom(v.getClass())) {
            Button btn = (Button) v;
            activity.dateChange(btn.getText(), v.getId());
        }
    }
}
