package de.gdoeppert.klimastatistik.gui;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import de.gdoeppert.klima.model.DbBase;
import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.Stat;
import de.gdoeppert.klima.model.Station;
import de.gdoeppert.klimastatistik.R;
import de.gdoeppert.klimastatistik.model.KlimaDbHelper;

public class KlimaStatActivity extends AppCompatActivity implements LocationListener, AdapterView.OnItemSelectedListener, ViewPager.OnPageChangeListener {

    public Calendar getHeute() {
        return heute;
    }

    public void resetHeute() {
        heute = new GregorianCalendar();
    }


    public Settings getSettings() {
        return settings;
    }

    public DbBase getDb() {
        return dbB;
    }

    public Station getStation() {
        return station;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        Log.d("KlimaActivity", "onCreate");

        setContentView(R.layout.activity_klima);

        getWindow().getDecorView().setBackgroundColor(Color.DKGRAY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        KlimaDbHelper helper = new KlimaDbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            Class.forName("org.sqldroid.SQLDroidDriver");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db.getPath());

            dbB = new DbBase();

            dbB.setup("SL", conn);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();

        station = new Station();
        station.setDbBean(dbB);

        setStationAdapter();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        String[] liste1 = getResources().getStringArray(R.array.contentPages);
        mSectionsPagerAdapter = new SectionsPagerAdapter(liste1, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(1);

        Spinner list = (Spinner) findViewById(R.id.diagram_mode);
        list.setVisibility(View.VISIBLE);
        setDiagramAdapter(list);

        Criteria whatFor = new Criteria();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        providerName = locationManager.getBestProvider(whatFor, true);
        location = locationManager.getLastKnownLocation(providerName);

    }

    // Menu listener
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }

    // Menu/actions listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_stationen) {

            StationenDialog statDia = new StationenDialog();
            statDia.setActivity(this);
            statDia.show(this.getSupportFragmentManager(), "StationDialog");
            return true;

        } else if (id == R.id.action_settings) {

            PreferencesDialog settings = new PreferencesDialog();
            settings.show(this.getSupportFragmentManager(), "SettingsDialog");
            return true;
        } else if (id == R.id.action_help) {

            if (helpID > 0) {
                showMessage("Hilfe",
                        getResources().getString(helpID));
            }
        } else if (id == R.id.action_info) {

            if (helpID > 0) {
                showMessage("Info",
                        getResources().getString(R.string.info));
        }

        }

        return super.onOptionsItemSelected(item);
    }

    // ViewPager listener
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    // ViewPager listener
    @Override
    public void onPageSelected(int position) {
        Spinner list = (Spinner) findViewById(R.id.diagram_mode);
        list.setSelection(position);
        KlimaFragment frag = (KlimaFragment) mSectionsPagerAdapter.getItem(position);
        if (frag != null && frag.klimaHandler != null && frag.klimaHandler.prefersFullpage()) {
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            //  WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
        if (frag != null && frag.klimaHandler != null) {
            helpID = frag.klimaHandler.getHelpID();
        }

        update();
    }

    // ViewPager listener
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setDiagramAdapter(Spinner spinner) {

        spinner.setOnItemSelectedListener(null);
        String[] liste2 = getResources().getStringArray(R.array.contentPages);

        ArrayAdapter diagramAdapter = new ArrayAdapter(this, R.layout.listitem, liste2);
        diagramAdapter.setDropDownViewResource(R.layout.listitem);
        spinner.setAdapter(diagramAdapter);
        spinner.setSelection(0);
        helpID = R.string.mittel_help;
        spinner.setOnItemSelectedListener(this);
    }

    public void showMessage(String title, String msg) {

        FragmentManager fm = getSupportFragmentManager();
        MessageDialog messageDialog = new MessageDialog();
        messageDialog.setMessage(title, msg); //$NON-NLS-1$
        messageDialog.show(fm, "messageDialog"); //$NON-NLS-1$
    }

    public void showUpdateResult(String statIds) {

        showMessage("Aktualisieren", "Es wurden folgende Stationen aktualisiert:\n" + statIds);
    }

    public void setStationenDirty() {
        setDirty();
        stationsDirty = true;
    }

    public void setDirty() {

        mSectionsPagerAdapter.setDirty();
    }


    public Spinner setStationAdapter() {

        Spinner spinner = (Spinner) findViewById(R.id.station_list);
        spinner.setOnItemSelectedListener(null);

        Vector<String> liste2 = new Vector<String>();
        int sel = 0;
        for (Stat s : getStation().getStationen()) {
            Log.d("KlimaActivity", "station " + s.getName());
            liste2.add(s.getName());
            if (s == getStation().getSelStat()) {
                Log.d("KlimaActivity", "select station: " + (liste2.size() - 1));
                sel = liste2.size() - 1;
            }
        }

        ArrayAdapter stationenAdapter = new ArrayAdapter(this, R.layout.listitem,
                liste2);
        stationenAdapter.setDropDownViewResource(R.layout.listitem);
        spinner.setAdapter(stationenAdapter);
        spinner.setSelection(sel);
        spinner.setOnItemSelectedListener(this);

        return spinner;
    }

    // Spinner events selection listener
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (adapterView.getId() == R.id.station_list) {

            statSelected = getStation().getStationen()[i];
            getStation().setSelStat(statSelected.getStat());
            Log.d("KlimaActivity", "select station");
            update();

        } else if (adapterView.getId() == R.id.diagram_mode) {

            Log.d("KlimaActivity", "select page");
            mViewPager.setCurrentItem(i, true);
            update();
        }
    }

    // Spinner events selection listener
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        if (adapterView.getId() == R.id.station_list) {
            statSelected = null;
            getStation().setSelStat(0);
        }
    }

    public void dateChange(int day, int month, int year) {
        getHeute().set(Calendar.DAY_OF_MONTH, day);
        getHeute().set(Calendar.YEAR, year);
        getHeute().set(Calendar.MONTH, month);
        Log.d("KlimaActivity", "dateChange1");
        update();
    }

    public void dateChange(CharSequence s, int id) {
        switch (id) {
            case R.id.tag_incr:
                getHeute().add(Calendar.DAY_OF_MONTH, 1);
                break;
            case R.id.mon_incr:
                getHeute().add(Calendar.MONTH, 1);
                break;
            case R.id.jahr_incr:
                getHeute().add(Calendar.YEAR, 1);
                break;
            case R.id.tag_decr:
                getHeute().add(Calendar.DAY_OF_MONTH, -1);
                break;
            case R.id.mon_decr:
                getHeute().add(Calendar.MONTH, -1);
                break;
            case R.id.jahr_decr:
                getHeute().add(Calendar.YEAR, -1);
                break;
            default:
                Log.w("KlimaActivity", "date change: " + s);
        }
        Log.d("KlimaActivity", "dateChange");
        update();
    }

    public void update() {

        if (stationsDirty) {
            Log.d("KlimaActivity", "stat dirty");
            setStationAdapter();
            stationsDirty = false;
        }

        KlimaFragment klimaFragment = (KlimaFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());

        Log.d("KlimaActivity", "activity update");
        klimaFragment.update();
    }


    /**
     * Register for the updates when Activity is in foreground
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("KlimaActivity", "request location updates");
        locationManager.requestLocationUpdates(providerName, 120000, 1, this);

        try {
            FileInputStream fis = openFileInput("preferences.txt");
            BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = rd.readLine()) != null) {
                String cols[] = line.split("=");
                if (cols.length == 2) {

                    if (cols[0].equals("vglJahr")) {
                        settings.vglJahr = Integer.valueOf(cols[1]);
                    } else if (cols[0].equals("winTemp")) {
                        settings.winTemp = Integer.valueOf(cols[1]);
                    } else if (cols[0].equals("winPhen")) {
                        settings.winPhen = Integer.valueOf(cols[1]);
                    } else if (cols[0].equals("winTrdTemp")) {
                        settings.winTrdTemp = Integer.valueOf(cols[1]);
                    } else if (cols[0].equals("gradSchwelle")) {
                        settings.gradSchwelle = Float.valueOf(cols[1]);
                    } else if (cols[0].equals("gradTemp")) {
                        settings.gradTemp = Float.valueOf(cols[1]);
                    } else if (cols[0].equals("statsel")) {
                        getStation().setSelStat(Integer.valueOf(cols[1]));
                        setStationAdapter();
                    } else if (cols[0].equals("jahre")) {
                        settings.setJahre(Jahre.Periode.valueOf(cols[1]));
                    } else if (cols[0].equals("gradTageJahre")) {
                        getSettings().setHeizmodusJahr(cols[1].equals("Jahr"));
                    }
                    Log.i("KlimaActivity", "Settings: " + cols[0] + "=" + cols[1]);
                }
            }
            setDirty();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the updates when Activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);

        try {
            FileOutputStream fos = openFileOutput("preferences.txt", //$NON-NLS-1$
                    MODE_PRIVATE);
            PrintWriter pr = new PrintWriter(fos);

            pr.println("vglJahr=" + settings.vglJahr);
            pr.println("winTemp=" + settings.winTemp);
            pr.println("winPhen=" + settings.winPhen);
            pr.println("winTrdTemp=" + settings.winTrdTemp);
            pr.println("gradSchwelle=" + settings.gradSchwelle);
            pr.println("gradTemp=" + settings.gradTemp);
            pr.println("statsel=" + getStation().getSelStat().getStat());
            pr.println("jahre=" + settings.jahre.name());
            pr.println("gradTageJahre=" + (settings.heizmodusJahr ? "Jahr" : "Winter"));

            pr.close();
        } catch (FileNotFoundException e) {
            // nothing... //e.printStackTrace();
        }
        super.onPause();
    }

    // location listener
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d("KlimaActivity", "location: " + location.getLatitude() + ", " + location.getLongitude());
    }

    // location listener
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    // location listener
    @Override
    public void onProviderEnabled(String s) {
    }

    // location listener
    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    protected void onRestoreInstanceState(Bundle state) {

        Log.println(Log.DEBUG, "KlimaActivity", "on restore instance state");

        mSectionsPagerAdapter.restoreState(state);

        int ct = state.getInt("currentTab", 0);
        heute.setTimeInMillis(state.getLong("currentDate"));
        //   int statId = state.getInt("currentStat", 0);

        mViewPager.setCurrentItem(ct);
        // station.setSelStat(statId);

        super.onRestoreInstanceState(state);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.println(Log.DEBUG, "KlimaActivity", "on save instance state");

        outState.putInt("currentTab", mViewPager.getCurrentItem());
        outState.putInt("currentStat", station.getSelStat().getStat());
        outState.putLong("currentDate", heute.getTimeInMillis());

        mSectionsPagerAdapter.saveState(outState);

        super.onSaveInstanceState(outState);
    }


    static class Settings implements Serializable {
        int vglJahr = -1;
        int winTemp = 0;
        int winPhen = 2;
        int winTrdTemp = 2;
        float gradSchwelle = 15f;
        float gradTemp = 20f;
        boolean heizmodusJahr = true;
        Jahre.Periode jahre = Jahre.Periode.alle;

        public void setJahre(Jahre.Periode jahre) {
            this.jahre = jahre;
        }

        public void setHeizmodusJahr(boolean heizmodusJahr) {
            this.heizmodusJahr = heizmodusJahr;
        }
    }

    private Settings settings = new Settings();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private DbBase dbB;
    private Station station;
    private LocationManager locationManager;
    private String providerName;
    private Location location = null;
    private Stat statSelected = null;
    private boolean stationsDirty = false;
    private Calendar heute = new GregorianCalendar();
    private int helpID = 0;
}
