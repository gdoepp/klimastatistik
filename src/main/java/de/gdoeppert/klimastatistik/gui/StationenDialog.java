package de.gdoeppert.klimastatistik.gui;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import de.gdoeppert.klima.model.Stat;
import de.gdoeppert.klimastatistik.R;
import de.gdoeppert.klimastatistik.tasks.UpdateTask;

// ...

public class StationenDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private KlimaStatActivity activity;
    private Stat[] stationen = null;
    private Stat statSelected = null;
    private View layout;

    public StationenDialog() {
        activity = (KlimaStatActivity) this.getActivity();
    }

    public void setActivity(KlimaStatActivity act) {
        activity = act;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (KlimaStatActivity) activity;
        Log.i("Stationen", "attached activity");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.stationendialog,
                container, false);

        getDialog().setTitle("Stationen");

        TextView txt = (TextView) layout.findViewById(R.id.station);

        if (activity == null) {
            activity = (KlimaStatActivity) getActivity();
            if (activity == null) return layout;
        }

        Stat selstat = activity.getStation().getSelStat();

        if (selstat != null) {
            txt.setText(selstat.getName());
        } else {
            txt.setText("#leer#");
        }

        txt = (TextView) layout.findViewById(R.id.heute_trim);
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy");
        txt.setText(format1.format(activity.getHeute().getTime()));

        final CheckBox trim = (CheckBox) layout.findViewById(R.id.chkTrim);
        trim.setChecked(false);

        final Spinner list = (Spinner) layout.findViewById(R.id.station_alle_list);

        setStationAdapter(list, false);

        list.setOnItemSelectedListener(this);

        CheckBox onlyLoc = (CheckBox) layout.findViewById(R.id.local_station);
        if (activity.getLocation() != null) {
            onlyLoc.setEnabled(true);
            onlyLoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    setStationAdapter(list, b);
                }
            });
        } else {
            onlyLoc.setEnabled(false);
        }

        final Button ondelstat = (Button) layout.findViewById(R.id.deleteStat);

        ondelstat.setEnabled(selstat != null);

        ondelstat.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Calendar trimDat = trim.isChecked() ? activity.getHeute() : null;
                UpdateTask upd = new UpdateTask(activity, UpdateTask.Operation.del_this, trimDat);
                upd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity);
                activity.setStationenDirty();

                StationenDialog.this.dismiss();

            }
        });

        final Button onupdstat = (Button) layout.findViewById(R.id.updateStat);
        onupdstat.setEnabled(selstat != null);

        onupdstat.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (isNetworkAvailable(activity)) {
                    Calendar trimDat = trim.isChecked() ? activity.getHeute() : null;
                    UpdateTask upd = new UpdateTask(activity, UpdateTask.Operation.upd_this, trimDat);
                    upd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity);

                    StationenDialog.this.dismiss();
                } else {
                    activity.showMessage("Stationen", "kein Netzzugriff");
                }

            }
        });

        final Button onupdall = (Button) layout.findViewById(R.id.updateAll);
        onupdall.setEnabled(selstat != null);
        onupdall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (isNetworkAvailable(activity)) {
                    Calendar trimDat = trim.isChecked() ? activity.getHeute() : null;
                    UpdateTask upd = new UpdateTask(activity, UpdateTask.Operation.upd_alle, trimDat);
                    upd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity);

                    StationenDialog.this.dismiss();
                } else {
                    activity.showMessage("Stationen", "kein Netzzugriff");
                }
            }
        });


        final Button onimp = (Button) layout.findViewById(R.id.import_station);

        onimp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (isNetworkAvailable(activity)) {

                    if (statSelected != null) {
                        Calendar trimDat = trim.isChecked() ? activity.getHeute() : null;
                        UpdateTask upd = new UpdateTask(activity, UpdateTask.Operation.import_one, trimDat);
                        upd.setStation(statSelected.getStat());
                        upd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity);
                    }

                    activity.setStationenDirty();
                    StationenDialog.this.dismiss();
                } else {
                    activity.showMessage("Stationen", "kein Netzzugriff");
                }

            }
        });

        final Button onimplist = (Button) layout.findViewById(R.id.importList);

        onimplist.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (isNetworkAvailable(activity)) {

                    UpdateTask upd = new UpdateTask(activity, UpdateTask.Operation.import_stats, null);
                    upd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, activity);

                    activity.setStationenDirty();
                    StationenDialog.this.dismiss();
                } else {
                    activity.showMessage("Stationen", "kein Netzzugriff");
                }

            }
        });

        final Button onhelp = (Button) layout.findViewById(R.id.stat_help);

        onhelp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                activity.showMessage("Hilfe", activity.getResources().getString(R.string.stationen_help));

            }
        });

        final Button oncancel = (Button) layout.findViewById(R.id.cancel);

        oncancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                StationenDialog.this.dismiss();

            }
        });

        return layout;
    }

    private void setStationAdapter(Spinner spinner, boolean onlyLoc) {

        Vector<String> liste2 = new Vector<String>();
        int sel = 0;

        if (onlyLoc) {
            Location loc = activity.getLocation();
            if (loc != null) {
                stationen = activity.getStation().getStationenAlle(loc.getLatitude(), loc.getLongitude());
            } else {
                stationen = activity.getStation().getStationenAlle();
            }
        } else {
            stationen = activity.getStation().getStationenAlle();
        }

        for (Stat s : stationen) {
            liste2.add(s.getName());
            if (s == activity.getStation().getSelStat()) {
                sel = liste2.size() - 1;
            }
        }

        ArrayAdapter stationenAdapter = new ArrayAdapter(getActivity(), R.layout.listitem,
                liste2);
        stationenAdapter.setDropDownViewResource(R.layout.listitem);
        spinner.setAdapter(stationenAdapter);
        spinner.invalidate();
    }

    private static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("Stationen", "isNetworkAvailable() " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        statSelected = stationen[i];
        TextView det = (TextView) layout.findViewById(R.id.stat_details);
        det.setText("Daten von " + statSelected.getJahr1() + " bis " + statSelected.getJahr2() + "\n" +
                "Höhe " + statSelected.getHoehe());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        statSelected = null;
    }

}
