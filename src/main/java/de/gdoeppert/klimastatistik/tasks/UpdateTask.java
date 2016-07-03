package de.gdoeppert.klimastatistik.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Calendar;

import de.gdoeppert.klimastatistik.gui.KlimaStatActivity;
import de.gdoeppert.klimastatistik.model.StationUpdaterFromINet;

/**
 * Created by gd on 04.01.16.
 */
public class UpdateTask extends AsyncTask<KlimaStatActivity, String, KlimaStatActivity> {

    private boolean all = false;
    private boolean history = false;
    private int statid = -1;
    private String result;
    private Activity activity;
    private ProgressDialog progress = null;
    private Calendar trimDat = null;


    public enum Operation {upd_alle, upd_this, del_this, import_one}

    ;
    Operation operation;


    public UpdateTask(Activity act, Operation op, Calendar td) {
        this.operation = op;
        activity = act;
        trimDat = td;
    }

    @Override
    protected KlimaStatActivity doInBackground(KlimaStatActivity... objs) {

        KlimaStatActivity activity = (KlimaStatActivity) objs[0];

        Log.i("UpdateTask", "working");
        StationUpdaterFromINet upd = new StationUpdaterFromINet(activity.getDb());

        switch (operation) {

            case upd_alle:
                result = upd.insertTageswerte(false, -1, trimDat);
                upd.insertWetterlagen(trimDat);
                break;
            case upd_this:
                result = upd.insertTageswerte(false, activity.getStation().getSelStat().getStat(), trimDat);
                upd.insertWetterlagen(trimDat);
                break;
            case import_one:
                result = upd.insertTageswerte(true, statid, trimDat);
                if (result != null && !result.isEmpty()) {
                    result = upd.insertTageswerte(false, statid, trimDat);
                }
                break;
            case del_this:
                result = upd.deleteTageswerte(activity.getStation().getSelStat().getStat());
                break;
        }

        upd.updateStationAktuell();
        upd.close();

        Log.i("UpdateTask", "ready");
        return activity;
    }

    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(activity);
        progress.setIndeterminate(true);
        progress.setMessage(operation == Operation.del_this ? "Daten werden gelöscht" : "Daten werden eingespielt...");
        progress.setCancelable(true);
        progress.show();
    }

    @Override
    protected void onPostExecute(KlimaStatActivity stf) {

        stf.getStation().init();

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }

        if (operation != Operation.del_this) {
            stf.showUpdateResult(result);
        }

        Log.d("UpdateTask", "post execute update");
        stf.setDirty();
        stf.setStationenDirty();
        stf.update();

        super.onPostExecute(stf);
    }

    public void setStation(int statid) {
        this.statid = statid;
    }
}
