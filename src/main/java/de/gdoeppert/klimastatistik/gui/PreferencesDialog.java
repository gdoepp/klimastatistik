package de.gdoeppert.klimastatistik.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klimastatistik.R;

// ...

public class PreferencesDialog extends DialogFragment implements OnClickListener {

    private KlimaStatActivity activity;
    private KlimaStatActivity.Settings settings;

    private int vgljahr;

    public PreferencesDialog() {

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (KlimaStatActivity) activity;
        Log.i("Preferences", "attached activity");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View layout = inflater.inflate(R.layout.preferencesdialog,
                container, false);

         getDialog().setTitle("Einstellungen");

        if (activity ==null) {
            activity = (KlimaStatActivity) getActivity();
            if (activity == null) return layout;
        }
        settings = activity.getSettings();

        vgljahr = settings.vglJahr;
        if (activity != null && vgljahr < 0) vgljahr = activity.getStation().getSelStat().getJahr2() - 1;


        EditText edVgljahr = addSettingsWin(layout, R.id.vgljahr, R.id.vgljahr_decr, R.id.vgljahr_incr, false);
        edVgljahr.setText(String.valueOf(vgljahr));

        EditText edWinTemp = addSettingsWin(layout, R.id.winTemp, R.id.winTemp_decr, R.id.winTemp_incr, false);
        edWinTemp.setText(String.valueOf(settings.winTemp));

        EditText edWinPhen = addSettingsWin(layout, R.id.winPhen, R.id.winPhen_decr, R.id.winPhen_incr, false);
        edWinPhen.setText(String.valueOf(settings.winPhen));

        EditText edWinTrend = addSettingsWin(layout, R.id.winTrend, R.id.winTrend_decr, R.id.winTrend_incr, false);
        edWinTrend.setText(String.valueOf(settings.winTrdTemp));

        EditText edGradTemp = addSettingsWin(layout, R.id.gradt_temp, R.id.gradt_temp_decr, R.id.gradt_temp_incr, true);
        edGradTemp.setText(String.valueOf(settings.gradTemp));

        EditText edGradSchw = addSettingsWin(layout, R.id.gradt_schw, R.id.gradt_schw_decr, R.id.gradt_schw_incr, true);
        edGradSchw.setText(String.valueOf(settings.gradSchwelle));

        RadioButton rd = (RadioButton) layout.findViewById(R.id.radio6190);
        rd.setOnClickListener(this);
        if (settings.jahre == Jahre.Periode.p6190) rd.setChecked(true);

        rd = (RadioButton) layout.findViewById(R.id.radio8110);
        if (settings.jahre == Jahre.Periode.p8110) rd.setChecked(true);
        rd.setOnClickListener(this);
        rd = (RadioButton) layout.findViewById(R.id.radioAlle);
        if (settings.jahre == Jahre.Periode.alle) rd.setChecked(true);
        rd.setOnClickListener(this);
        rd = (RadioButton) layout.findViewById(R.id.radioJahr);
        if (settings.jahre == Jahre.Periode.jahr) rd.setChecked(true);
        rd.setOnClickListener(this);
        rd = (RadioButton) layout.findViewById(R.id.gradt_jahr);
        rd.setChecked(settings.heizmodusJahr);
        rd = (RadioButton) layout.findViewById(R.id.gradt_winter);
        rd.setChecked(!settings.heizmodusJahr);


        final Button onok = (Button) layout.findViewById(R.id.ok);

        onok.setOnClickListener(new OnClickListener() {
           final KlimaStatActivity.Settings settings = PreferencesDialog.this.settings;

            @Override
            public void onClick(View arg0) {


                EditText edVgljahr = (EditText) layout.findViewById(R.id.vgljahr);
                settings.vglJahr = Integer.valueOf(edVgljahr.getText().toString());

                EditText edWinTemp = addSettingsWin(layout, R.id.winTemp, R.id.winTemp_decr, R.id.winTemp_incr, false);
                settings.winTemp = Integer.valueOf(edWinTemp.getText().toString());

                EditText edWinPhen = addSettingsWin(layout, R.id.winPhen, R.id.winPhen_decr, R.id.winPhen_incr, false);
                settings.winPhen = Integer.valueOf(edWinPhen.getText().toString());

                EditText edWinTrend = addSettingsWin(layout, R.id.winTrend, R.id.winTrend_decr, R.id.winTrend_incr, false);
                settings.winTrdTemp = Integer.valueOf(edWinTrend.getText().toString());

                EditText edGradTemp = addSettingsWin(layout, R.id.gradt_temp, R.id.gradt_temp_decr, R.id.gradt_temp_incr, true);
                settings.gradTemp = Float.valueOf(edGradTemp.getText().toString());

                EditText edGradSchw = addSettingsWin(layout, R.id.gradt_schw, R.id.gradt_schw_decr, R.id.gradt_schw_incr, true);
                settings.gradSchwelle = Float.valueOf(edGradSchw.getText().toString());

                RadioButton rb = (RadioButton) layout.findViewById(R.id.gradt_jahr);
                settings.setHeizmodusJahr(rb.isChecked());

                PreferencesDialog.this.dismiss();

                if (activity != null) {
                    Log.d("Preferences", "update");
                    activity.setDirty();
                    activity.update();
                }
            }
        });

        final Button onhelp = (Button) layout.findViewById(R.id.pref_help);

        onhelp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (activity != null) activity.showMessage("Hilfe", activity.getResources().getString(R.string.pref_help));

            }
        });


        final Button oncancel = (Button) layout.findViewById(R.id.cancel);

        oncancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                PreferencesDialog.this.dismiss();

            }
        });

        return layout;
    }

    private EditText addSettingsWin(View layout, int winId, int decrId, int incrId, final boolean isFloat) {
        final EditText edWin = (EditText) layout.findViewById(winId);

        final Button onwinminus = (Button) layout.findViewById(decrId);

        onwinminus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isFloat) {
                    float win = Float.valueOf(edWin.getText().toString());
                    win -= 0.25;
                    edWin.setText(String.valueOf(win));

                } else {
                    int win = Integer.valueOf(edWin.getText().toString());
                    if (win > 0) {
                        win--;
                    }
                    edWin.setText(String.valueOf(win));
                }
            }
        });

        final Button onwinplus = (Button) layout.findViewById(incrId);

        onwinplus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isFloat) {
                    float win = Float.valueOf(edWin.getText().toString());
                    win += 0.25;
                    edWin.setText(String.valueOf(win));

                } else {
                    int win = Integer.valueOf(edWin.getText().toString());
                    win++;
                    edWin.setText(String.valueOf(win));
                }
            }
        });
        return edWin;
    }


    @Override
    public void onClick(View v) {

        RadioButton btn = ((RadioButton) v);
        if (btn.isChecked()) {
            switch (btn.getId()) {
                case R.id.radioAlle:
                    settings.setJahre(Jahre.Periode.alle);
                    break;
                case R.id.radio6190:
                    settings.setJahre(Jahre.Periode.p6190);
                    break;
                case R.id.radio8110:
                    settings.setJahre(Jahre.Periode.p8110);
                    break;
                case R.id.radioJahr:
                    settings.setJahre(Jahre.Periode.jahr);
                    break;
            }
        }
    }
}
