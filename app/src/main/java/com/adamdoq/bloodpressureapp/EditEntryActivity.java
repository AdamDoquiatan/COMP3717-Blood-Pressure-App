package com.adamdoq.bloodpressureapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EditEntryActivity extends AppCompatActivity {

    // Connect to firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Will create a subtable called "BPReadings" when you first add an entry
    DatabaseReference myRef = database.getReference().child("BPReadings");


    final Calendar myCalendar = Calendar.getInstance();
    TextView timeText;
    TextView dateText;

    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}


        setContentView(R.layout.edit_entry_activity);

        id = getIntent().getStringExtra(getString(R.string.id_extra));

        // Sets default date and time
        Date d = new Date();
        CharSequence defaultDate = DateFormat.format("MM/dd/yy", d.getTime());
        dateText = findViewById(R.id.readingDate);
        dateText.setText(getString(R.string.reading_date) + " " + defaultDate);

        // Sets default time
        CharSequence defaultTime = DateFormat.format("hh:mm", d.getTime());
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
                new DatePickerDialog(EditEntryActivity.this, date, myCalendar
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
                mTimePicker = new TimePickerDialog(EditEntryActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                timeText.setText(selectedHour + ":" + selectedMinute);
                            }
                        }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getString(R.string.time_picker_title));
                mTimePicker.show();

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
                mTimePicker = new TimePickerDialog(EditEntryActivity.this,
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

    //Uses the ID of the source reading (the id is the key in firebase) to create a new reading and
    // replace the old one
    public void editReading(View view){
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

        BPReading updatedBPReading = new BPReading(userId,
                systolicReading,
                diastolicReading);
        updatedBPReading.id = id;

        Task setValueTask = myRef.child(id).setValue(updatedBPReading);

        hideSoftKeyboard(view);

        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditEntryActivity.this,
                        getString(R.string.db_fetch_err) + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        finish();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Task setValueTask = myRef.child(id).child(id).setValue(id);
        }
        return super.onKeyDown(keyCode, event);
    }
}

