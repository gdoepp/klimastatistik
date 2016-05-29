package de.gdoeppert.klimastatistik.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Vector;

import de.gdoeppert.klimastatistik.gui.KlimaHandler;

/**
 * Created by gd on 02.01.16.
 */
public class KlimaWorkTask extends AsyncTask<Void, Void, Void> {

    private Vector<?> tageswerte = null;

    private KlimaHandler handler;
    private ProgressDialog progress = null;

    private Activity activity;

    public KlimaWorkTask(Activity act) {
        activity = act;
    }

    public void setHandler(KlimaHandler vh) {
        handler = vh;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.i("KlimaTask", "working");
        try {
            tageswerte = handler.doWork();
        } catch (Throwable th) {
            tageswerte = null;
            th.printStackTrace();
        }
        Log.i("KlimaTask", "ready");
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        handler.setDirty();
    }

    @Override
    protected void onPreExecute() {
        /*
        progress = new ProgressDialog(activity);
        progress.setIndeterminate(true);
        progress.setMessage("Daten werden ausgewertet...");
        progress.setCancelable(true);
        progress.show();
        */
    }

    @Override
    protected void onPostExecute(Void param) {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }

        handler.setViewParam(tageswerte);
        super.onPostExecute(null);
        handler.postprocess();
    }
}
