package in.diagnext.mylibrary;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class Database extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "diagsteps";
    private static final String TABLE_STEPS = "StepMaster";
    private final static int DB_VERSION = 2;
    public static final String PREFS_STEPS = "steps";

    private static final String CREATE_TABLE_STEPS = "CREATE TABLE "
            + TABLE_STEPS + "(StepId INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, steps INTEGER);";
    private SharedPreferences stepPref;
//, km REAL, kcal REAL, time INTEGER

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_STEPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_STEPS + "'");
        onCreate(db);
    }

    public int saveCurrentSteps(int steps, long date) {
        ContentValues values = new ContentValues();

        int i=0;
        if(columnExists(String.valueOf(date))) {
            //update
            values.put("steps", steps);
            getWritableDatabase().update(TABLE_STEPS, values,"date = " +date,null);
            i=1;
        }
        else {
            //insert
          //  stepPref = context.getSharedPreferences(PREFS_STEPS, 0);
          //  SharedPreferences.Editor editor = stepPref.edit();
          //  editor.putInt("step", 0);
          //  editor.commit();

            values.put("steps", 0);
            values.put("date", date);
            getWritableDatabase().insert(TABLE_STEPS, null, values);
            i=2;
        }
        return i;
    }

    public boolean columnExists(String value) {
        String sql = "SELECT EXISTS (SELECT * FROM "+ TABLE_STEPS +" WHERE date="+value+" LIMIT 1)";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToFirst();

        // cursor.getInt(0) is 1 if column with value exists
        if (cursor.getInt(0) == 1) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public ArrayList<StepMaster> getSteps(String value)
    {
        ArrayList<StepMaster> stepMasterArrayList = new ArrayList<StepMaster>();

        String selectQuery = "SELECT * FROM "+ TABLE_STEPS +" WHERE date="+value+" LIMIT 1";
        try (Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null)) {
            cursor.moveToFirst();


            if (cursor.getCount() >= 1) {

                StepMaster stepMaster = new StepMaster();
                stepMaster.setSteps(cursor.getInt(cursor.getColumnIndex("steps")));

                stepMasterArrayList.add(stepMaster);
            }
        }

        return stepMasterArrayList;
    }

    public ArrayList<StepMaster> getWeeklySteps() {
        ArrayList<StepMaster> stepMasterArrayList = new ArrayList<StepMaster>();

        String selectQuery = "SELECT * FROM (SELECT * FROM " + TABLE_STEPS + " ORDER BY date DESC LIMIT 10) ORDER BY date ASC";
        try (Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null)) {

            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    do {
                        StepMaster stepMaster = new StepMaster();
                        stepMaster.setDate(cursor.getLong(cursor.getColumnIndex("date")));
                        stepMaster.setSteps(cursor.getInt(cursor.getColumnIndex("steps")));
                        stepMasterArrayList.add(stepMaster);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();

            return stepMasterArrayList;
        }
    }

    @Override
    public void close() {

        super.close();

    }
}
