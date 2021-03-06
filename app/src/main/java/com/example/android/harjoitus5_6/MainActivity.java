package com.example.android.harjoitus5_6;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.harjoitus5_6.data.DatabaseContract;
import com.example.android.harjoitus5_6.data.TrainingDbHelper;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase mDb;
    private EditText mDate;
    private EditText mSport;
    private EditText mDuration;
    private EditText mRpe;
    private EditText mSharpness;

    private RecyclerView mRecyclerView;
    private TrainingAdapter mTrainingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDate = (EditText) findViewById(R.id.et_date);
        mSport = (EditText) findViewById(R.id.et_sport);
        mDuration = (EditText) findViewById(R.id.et_duration);
        mRpe = (EditText) findViewById(R.id.et_rpe);
        mSharpness = (EditText) findViewById(R.id.et_sharpness);

        mRecyclerView = (RecyclerView) findViewById(R.id.all_entries_list_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        TrainingDbHelper dbHelper = new TrainingDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        Cursor cursor = getAllEntries();
        mTrainingAdapter = new TrainingAdapter(this, cursor);
        mRecyclerView.setAdapter(mTrainingAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                deleteEntry(id);
                mTrainingAdapter.swapCursor(getAllEntries());
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    private Cursor getAllEntries(){
        return mDb.query(DatabaseContract.TrainingEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.TrainingEntry.COLUMN_DATE);
    }

    public void addNewEntry(View view) {
        if (mDate.getText().length() == 0 ||
                mDuration.getText().length() == 0 ||
                mRpe.getText().length() == 0 ||
                mSharpness.getText().length() == 0) {
            return;
        }

        float duration = 1;
        int durationMinutes = 0;
        int rpe = 1;
        int sharpness = 1;

        try {
            duration = Float.parseFloat(mDuration.getText().toString());
            rpe = Integer.parseInt(mRpe.getText().toString());
            sharpness = Integer.parseInt(mSharpness.getText().toString());
            durationMinutes = Math.round(duration * 60);

            // Validate input values
            if (durationMinutes > 0 && rpe > 0 && rpe <= 10 && sharpness > 0 && sharpness <= 10) {
                createNewEntry(mDate.getText().toString(), durationMinutes, rpe, sharpness, mSport.getText().toString());
                Toast toast = Toast.makeText(this, R.string.entry_created, Toast.LENGTH_LONG);
                toast.show();
            } else {
                // Give error message about inputs
            }
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
        }

        mTrainingAdapter.swapCursor(getAllEntries());
    }

    private long createNewEntry(String date, int duration, int rpe, int sharpness, String sport) {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseContract.TrainingEntry.COLUMN_DATE, date);
        cv.put(DatabaseContract.TrainingEntry.COLUMN_DURATION, duration);
        cv.put(DatabaseContract.TrainingEntry.COLUMN_RPE, rpe);
        cv.put(DatabaseContract.TrainingEntry.COLUMN_SHARPNESS, sharpness);
        cv.put(DatabaseContract.TrainingEntry.COLUMN_SPORT, sport);

        return mDb.insert(DatabaseContract.TrainingEntry.TABLE_NAME, null, cv);
    }

    private boolean deleteEntry(long id) {
        return mDb.delete(DatabaseContract.TrainingEntry.TABLE_NAME, DatabaseContract.TrainingEntry._ID + "=" + id, null) > 0;
    }
}
