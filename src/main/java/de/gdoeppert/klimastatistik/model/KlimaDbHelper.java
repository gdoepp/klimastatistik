package de.gdoeppert.klimastatistik.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by gd on 30.12.15.
 * Code from Vaishak Nair on stackoverflow.com
 */
public class KlimaDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "KlimaDbHelper";

    private final Context context;
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "klimastatistik.db";

    private boolean createDb = false, upgradeDb = false;

    public KlimaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Copy packaged database from assets folder to the database created in the
     * application package context.
     *
     * @param db The target database in the application package context.
     */
    private void copyDatabaseFromAssets(SQLiteDatabase db) {
        Log.i(TAG, "copyDatabase");
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            // Open db packaged as asset as the input stream
            myInput = context.getAssets().open("klimastatistik.db");

            // Open the db in the application package context:
            myOutput = new FileOutputStream(db.getPath());

            // Transfer db file contents:
            byte[] buffer = new byte[32 * 1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();

            // Set the version of the copied database to the current
            // version:
            SQLiteDatabase copiedDb = context.openOrCreateDatabase(
                    DATABASE_NAME, 0, null);
            copiedDb.execSQL("PRAGMA user_version = " + DATABASE_VERSION);
            copiedDb.close();
            Log.i(TAG, "copyDatabase finished");
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(TAG + " Error copying database");
        } finally {
            // Close the streams
            try {
                if (myOutput != null) {
                    myOutput.close();
                }
                if (myInput != null) {
                    myInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error(TAG + " Error closing streams");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate db");
        createDb = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade db");
        upgradeDb = true;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i(TAG, "onOpen db");
        if (createDb) {// The db in the application package
            // context is being created.
            // So copy the contents from the db
            // file packaged in the assets
            // folder:
            createDb = false;
            copyDatabaseFromAssets(db);

        }
        if (upgradeDb) {// The db in the application package
            // context is being upgraded from a lower to a higher version.
            upgradeDb = false;
            //copyDatabaseFromAssets(db);
            //TODO Your db upgrade logic here:
        }
    }
}
