package de.gdoeppert.klimastatistik.gui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.gdoeppert.klimastatistik.R;
import de.gdoeppert.klimastatistik.tasks.KlimaWorkTask;

/**
 * Created by gd on 02.01.16.
 */
public class KlimaFragment extends Fragment implements View.OnClickListener, View.OnSystemUiVisibilityChangeListener {


    public static KlimaFragment newInstance(int sectionNumber) {
        Log.d("KlimaFragment", "new Fragment: " + sectionNumber);

        KlimaFragment fragment = new KlimaFragment();
        Bundle args = new Bundle();
        args.putInt("pos", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("KlimaFragment", "pause");
        running = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("KlimaFragment", "stop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("KlimaFragment", "start");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("KlimaFragment", "resume");
        running = true;
        update();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        int i = getArguments().getInt("pos") - 1;
        init(i);
        Log.i("KlimaFragment", "create fragment " + i + " tag: " + getTag());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_klima, container, false);

        int layoutid = klimaHandler.getLayoutId();

        topView = inflater.inflate(layoutid, container, false);

        klimaHandler.setView(topView);

        frameLayout = (FrameLayout) rootView.findViewById(R.id.frame);

        frameLayout.addView(topView);

        if (klimaHandler.prefersFullpage()) {
            topView.setOnClickListener(this);

            topView.setOnSystemUiVisibilityChangeListener(this);
        }


        update();

        Log.i("KlimaFragment", "create view ");

        return rootView;
    }

    protected void init(int i) {

        String[] liste1 = getResources().getStringArray(R.array.contentPages);
        String[] liste2 = getResources().getStringArray(R.array.contentClasses);

        try {
            //Log.d("KlimaFragment", "old handler: " + klimaHandler);

            if (klimaHandler == null) {
                Class<?> cls = Class.forName("de.gdoeppert.klimastatistik.gui." + liste2[i]);
                klimaHandler = (KlimaHandler) cls.newInstance();
                klimaHandler.setActivity(getKActivity());
                klimaHandler.setMode(liste1[i]);
            }


            Log.d("KlimaFragment", "modus: " + liste1[i] + " handler: " + liste2[i]);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public KlimaStatActivity.Settings getSettings() {

        return getKActivity().getSettings();
    }

    public void update() {

        Log.i("KlimaFragment", "update");

        if (!running) {
            Log.i("KlimaFragment", "pausing");
            return;
        }

        if (klimaHandler == null) {

            Log.w("KlimaFragment", "kh: " + klimaHandler + ", act: " + getKActivity() + ", fra: " + frameLayout);
        }

        if (klimaHandler != null) {
            if (klimaHandler.needsUpdate()) {

                if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
                    // return;
                    task.cancel(true);
                }

                task = new KlimaWorkTask(getKActivity());
                task.setHandler(klimaHandler);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.i("KlimaFragment", "started task " + klimaHandler.toString());
            } else {
                Log.i("KlimaFragment", "postprocess");
                klimaHandler.postprocess();
            }
        } else {
            Log.w("KlimaFragment", "no klimaHandler");
        }
    }

    @Override
    public void onClick(View v) {

        int mode = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                ;

        int curVis = v.getSystemUiVisibility();
        v.setSystemUiVisibility(curVis ^ mode);

    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {


        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if ((visibility & (View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)) > 0) {
            Log.d("klimaFragment", "ui on->off");
            toolbar.setVisibility(View.GONE);
        } else {
            Log.d("klimaFragment", "ui off->on");
            toolbar.setVisibility(View.VISIBLE);
        }
    }


    public KlimaStatActivity getKActivity() {
        return (KlimaStatActivity) super.getActivity();
    }

    protected KlimaWorkTask task = null;

    protected KlimaHandler klimaHandler = null;
    protected View topView = null;
    protected FrameLayout frameLayout;
    protected boolean running = false;

}
