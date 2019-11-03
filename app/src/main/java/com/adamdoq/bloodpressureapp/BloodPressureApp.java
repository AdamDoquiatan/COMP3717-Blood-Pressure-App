package com.adamdoq.bloodpressureapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BloodPressureApp extends AppCompatActivity {

    // Connect to firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Will create a subtable called "BPReadings" when you first add an entry
    DatabaseReference dbRef = database.getReference().child("BPReadings");

    ArrayList<BPReading> bpReadingsList = new ArrayList<>();

    final Calendar myCalendar = Calendar.getInstance();
    TextView timeText;
    TextView dateText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        setContentView(R.layout.activity_main);

        // Takes snapshot of firebase, empties + repopulates bpReadingsList, displays tasks
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bpReadingsList.clear();

                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    BPReading bpReading = studentSnapshot.getValue(BPReading.class);
                    bpReadingsList.add(bpReading);
                }

                displayReadings(bpReadingsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Sets default date and time
        Date d = new Date();
        CharSequence defaultDate = DateFormat.format("MM/dd/yy", d.getTime());
        dateText = findViewById(R.id.readingDate);
        dateText.setText(getString(R.string.reading_date) + " " + defaultDate);

        // Sets default time
        CharSequence defaultTime = DateFormat.format("HH:mm", d.getTime());
        timeText = findViewById(R.id.readingTime);

        timeText.setText(getString(R.string.reading_time) + " " + defaultTime);

        // Date Picker stuff
        dateText = findViewById(R.id.readingDate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(dateText);
            }

        };

        dateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(BloodPressureApp.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // TimePicker stuff
        timeText = findViewById(R.id.readingTime);

        timeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(BloodPressureApp.this,
                        new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        timeText.setText(getString(R.string.reading_time) + " " + selectedHour +
                                ":" + selectedMinute);
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle(getString(R.string.time_picker_title));
                mTimePicker.show();

            }
        });

    }

    public void createReading(View view) {
        LinearLayout displayLayout = findViewById(R.id.displayLayout);
        removeAllChildViews(displayLayout);

        EditText editText = findViewById(R.id.txUserId);
        String userId = editText.getText().toString();
        editText.setText("");

        editText = findViewById(R.id.txSystolicReading);
        String systolicReading = editText.getText().toString();
        editText.setText("");

        editText = findViewById(R.id.txDiastolicReading);
        String diastolicReading = editText.getText().toString();
        editText.setText("");

        editText = findViewById(R.id.txCondition);
        String condition = editText.getText().toString();
        editText.setText("");

        BPReading bpReading = new BPReading(userId,
                timeText.getText().toString(), dateText.getText().toString(), systolicReading,
                diastolicReading, condition);

        Task setValueTask = dbRef.child(bpReading.id).setValue(bpReading);

        hideSoftKeyboard(view);

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BloodPressureApp.this,
                        getString(R.string.db_fetch_err) + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Creates a Layout and View for each title.
    private void displayReadings(ArrayList<BPReading> bpReadingsList){
        for (int i = 0; i < bpReadingsList.size(); i++) {

            final int ADDED_MARGINS = 160; //Added each time readings are added to make up for the
            // addition margin from each reading (scrollview takes it's height from the inner
            // linear layout's height, minus margins

            LinearLayout displayLayout = findViewById(R.id.displayLayout);
            int paddingBottom = displayLayout.getPaddingBottom();
            displayLayout.setPadding(0, 0, 0, paddingBottom + ADDED_MARGINS);


            // First Row
            LinearLayout displaySublayout1 = new LinearLayout(this);
            displaySublayout1.setOrientation(LinearLayout.HORIZONTAL);

            TextView userId = viewCreatorLine1(getString(R.string.prepend_userId),
                    bpReadingsList.get(i).userId);

            TextView systolicReading = viewCreatorLine1(getString(R.string.prepend_systolic),
                    bpReadingsList.get(i).systolicReading);

            TextView diastolicReading = viewCreatorLine1(getString(R.string.prepend_diastolic),
                    bpReadingsList.get(i).diastolicReading);

            displaySublayout1.addView(userId);
            displaySublayout1.addView(systolicReading);
            displaySublayout1.addView(diastolicReading);

            // Second Row
            LinearLayout displaySublayout2 = new LinearLayout(this);
            displaySublayout2.setOrientation(LinearLayout.HORIZONTAL);

            TextView date  = viewCreatorLine2(getString(R.string.prepend_date),
                    bpReadingsList.get(i).date);

            TextView time  = viewCreatorLine2(getString(R.string.prepend_time),
                    bpReadingsList.get(i).time);

            displaySublayout2.addView(date);
            displaySublayout2.addView(time);

            // Third Row
            LinearLayout displaySublayout3 = new LinearLayout(this);
            displaySublayout1.setOrientation(LinearLayout.HORIZONTAL);

            TextView condition = viewCreatorLine2(getString(R.string.prepend_condition),
                    bpReadingsList.get(i).condition);

            displaySublayout3.addView(condition);

            // Fourth Row
            LinearLayout displaySublayout4 = new LinearLayout(this);
            displaySublayout1.setOrientation(LinearLayout.HORIZONTAL);


            final String id = bpReadingsList.get(i).id;

            // Sets up edit button -- send the reading id to the other activity
            TextView editReading = viewCreatorLine3("", getString(R.string.edit));
            editReading.setTypeface(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);

            editReading.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout displayLayout = findViewById(R.id.displayLayout);
                    removeAllChildViews(displayLayout);

                    Intent intent = new Intent(view.getContext(), EditEntryActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });

            //Sets up remove button to remove reading from the database + remove all child views so
            // that the snapshot listener thing can reconstruct the list
            TextView removeReadingButton = viewCreatorLine3("", getString(R.string.remove));
            removeReadingButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);

            removeReadingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeAllChildViews((LinearLayout) view.getParent().getParent().getParent());
                    dbRef.child(id).removeValue();
                }
            });

            displaySublayout4.addView(editReading);
            displaySublayout4.addView(removeReadingButton);

            // Wrapper so that removeAllChildViews can delete views in readings at once
            LinearLayout readingWrapperLayout = new LinearLayout(this);
            readingWrapperLayout.setOrientation(LinearLayout.VERTICAL);

            readingWrapperLayout.addView(displaySublayout1);
            readingWrapperLayout.addView(displaySublayout2);
            readingWrapperLayout.addView(displaySublayout3);
            readingWrapperLayout.addView(displaySublayout4);
            displayLayout.addView(readingWrapperLayout);
        }
    }

    // Sets properties for stuff in the first layout of a reading
    public TextView viewCreatorLine1(String prependText, String text){
        TextView readingText = new TextView(this);
        readingText.setText(prependText + text);
        readingText.setId(View.generateViewId());
        readingText.setTextSize(15);
        readingText.setPadding(10,10,10,0);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 10, 0);
        readingText.setLayoutParams(lp);

        return readingText;
    }

    // Sets properties for stuff in the second layout of a reading
    public TextView viewCreatorLine2(String prependText, String text){
        TextView readingText = new TextView(this);
        readingText.setText(prependText + text);
        readingText.setId(View.generateViewId());
        readingText.setTextSize(12);
        readingText.setPadding(10,0,10,0);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 0, 10, 0);
        readingText.setLayoutParams(lp);

        return readingText;
    }

    // Sets properties for stuff in the third layout of a reading
    public TextView viewCreatorLine3(String prependText, String text){
        TextView readingText = new TextView(this);
        readingText.setText(prependText + text);
        readingText.setId(View.generateViewId());
        readingText.setTextSize(12);
        readingText.setPadding(10,0,10,10);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 0, 10, 10);
        readingText.setLayoutParams(lp);

        return readingText;
    }

    // Updates data due textview with calendar picker selection
    private void updateLabel(TextView textView) {
        String myFormat = getString(R.string.date_format);
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        textView.setText(getString(R.string.reading_date) + " " + sdf.format(myCalendar.getTime()));
    }


    public void hideSoftKeyboard(View view){
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    // Removes all readings from list every time a new one is added in order to avoid mysteriously
    // appearing duplicate tasks. For loop was unable to remove all children for some reason, so
    // went with while loop.
    void removeAllChildViews(ViewGroup viewGroup) {

        while (viewGroup.getChildCount() > 0) {
            View child = viewGroup.getChildAt(0);
            viewGroup.removeView(child);
        }
    }
}

// Could have put this in it's own file
class BPReading {
    public String id;
    public String userId;
    public String time;
    public String date;
    public String systolicReading;
    public String diastolicReading;
    public String condition;

    // Add this to get rid on 'no-argument constructor' error. Also make sure the class is
    // static if an inner class (like this one) or that the class is in it's own file.
    public BPReading() {}


    public BPReading(String userId, String time, String date, String systolicReading,
                     String diastolicReading, String condition) {

        this.id = String.valueOf(System.currentTimeMillis());
        this.userId = userId;
        this.time = time;
        this.date = date;
        this.systolicReading = systolicReading;
        this.diastolicReading = diastolicReading;
        this.condition = condition;

    }
}