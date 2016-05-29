package de.gdoeppert.klimastatistik.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private FragmentManager fm;
    private Map<Integer, KlimaFragment> fragments = new HashMap<Integer, KlimaFragment>();
    int nPages = 0;

    public SectionsPagerAdapter(String[] liste1, FragmentManager fm) {
        super(fm);
        nPages = liste1.length;
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("KlimaActivity", "getPager-Item: " + position);
        KlimaFragment f = fragments.get(position);
        if (f == null) {
            f = KlimaFragment.newInstance(position + 1);
            if (f != null) {
                Log.d("KlimaActivity", "create new fragment instance " + position);
                fragments.put(position, f);
            }
        }
        return f;
    }

    @Override
    public int getCount() {
        return nPages;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

    public void setDirty() {
        for (KlimaFragment f : fragments.values()) {
            if (f != null && f.klimaHandler != null) {
                f.klimaHandler.setDirty();
                Log.i("SelectionsPagerAdapter", "dirty frag " + f.klimaHandler.toString());
            }
        }
    }


    public void saveState(Bundle bundle) {
        Log.println(Log.DEBUG, "s-p-adapter", "save state"); //$NON-NLS-1$ //$NON-NLS-2$
        if (bundle == null)
            return;

        bundle.putInt("FragN", this.getCount());
        for (Map.Entry<Integer, KlimaFragment> fragEnt : fragments.entrySet()) {
            if (fragEnt.getValue() != null) {
                bundle.putString("Frag" + fragEnt.getKey(), fragEnt.getValue().getTag());
                Log.d("s-p-a", "Frag" + fragEnt.getKey() + " = " + fragEnt.getValue().getTag());
            }
        }
    }

    public void restoreState(Bundle bundle) {
        Log.println(Log.DEBUG, "s-p-adapter", "restore state"); //$NON-NLS-1$ //$NON-NLS-2$
        if (bundle == null)
            return;

        int n = bundle.getInt("FragN");
        for (int j = 0; j <= n; j++) {
            String tag = bundle.getString("Frag" + j);
            if (tag != null) {
                KlimaFragment frag = (KlimaFragment) fm.findFragmentByTag(tag);
                if (frag != null) {
                    fragments.put(j, frag);
                    Log.d("s-p-a", "Frag" + j + " = " + frag.getTag());
                }
            }
        }

    }

}
