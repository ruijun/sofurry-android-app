package com.sofurry.adapters;

import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


//~--- classes ----------------------------------------------------------------

/**
 * @author         SoFurry
 */
public class NamesAcDbAdapter {
    public static final String  COLUMN_NAME      = "name";
    private static final String DATABASE_NAME    = "nameac";
    private static final int    DATABASE_VERSION = 2;

    //~--- fields -------------------------------------------------------------

    public Names           names_;
    private final Context  context_;
    private DatabaseHelper dbHelper_;
    private SQLiteDatabase db_;


    //~--- inner classes ------------------------------------------------------

    private static class DatabaseHelper
            extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //~--- methods --------------------------------------------------------

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE names (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }


    /**
     * @author         SoFurry
     */
    public class Names {

        public void addName(String name) {
            if (!hasName(name)) {
                db_.execSQL("INSERT INTO names (name) VALUES (?)", new String[] { name });
            }
        }

        public void clearAllNames() {
            db_.execSQL("DELETE FROM names");
        }

        public void removeName(String name) {
            db_.execSQL("DELETE FROM names WHERE name=?", new String[] { name });
        }

        //~--- get methods ----------------------------------------------------

        public Cursor getNames() {
            return db_.rawQuery("SELECT _id, name FROM names ORDER BY name ASC", null);
        }

        public boolean hasName(String name) {
            Cursor  cursor;
            boolean found = false;

            // Perform query
            cursor = db_.rawQuery("SELECT _id, name FROM names WHERE name=?", new String[] { name });

            // Change the value of 'found' if the name exist
            if (cursor.getCount() > 0) {
                found = true;
            }

            // Remember to close the cursor
            cursor.close();

            // Finally return the value
            return found;
        }
    }


    //~--- constructors -------------------------------------------------------

    public NamesAcDbAdapter(Context context) {
        context_ = context;
    }

    //~--- methods ------------------------------------------------------------

    public void close() {
        dbHelper_.close();
    }

    public NamesAcDbAdapter open() throws SQLException {
        dbHelper_ = new DatabaseHelper(context_);
        db_       = dbHelper_.getWritableDatabase();

        // Instantiate table access classes
        // TODO: Consider lazy loading, using accessors...
        names_ = new Names();

        return this;
    }
}
