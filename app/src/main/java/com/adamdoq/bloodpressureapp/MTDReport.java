package com.adamdoq.bloodpressureapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MTDReport extends AppCompatActivity {

    // Connect to firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Will create a subtable called "BPReadings" when you first add an entry
    DatabaseReference myRef = database.getReference().child("BPReadings");

    ArrayList<BPReading> bpReadingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mtdreport);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    BPReading bpReading = snapshot.getValue(BPReading.class);
                    bpReadingsList.add(bpReading);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button genReportBtn = findViewById(R.id.btnGenReport);
        genReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport(view);
            }
        });
    }
    //Creates report Layout
    public void generateReport(View view){
        EditText userIDInput = findViewById(R.id.edtGenReport);
        String userID = userIDInput.getText().toString();
        //used for calculating averages
        int systolicTotal = 0;
        int diastolicTotal = 0;
        int counter = 0;
        //calculated averages
        double systolicAverage;
        double diastolicAverage;
        String conditionAverage;
        //Loops through all readings and takes average for ones under the users name.
        for(BPReading bpReading: bpReadingsList){
            if(bpReading.userId.equals(userID)){
                systolicTotal = systolicTotal + Integer.parseInt(bpReading.systolicReading);
                diastolicTotal = diastolicTotal + Integer.parseInt(bpReading.diastolicReading);
                counter++;
            }
        }
        //Assigns the calculated averages.
        systolicAverage = systolicTotal/counter;
        diastolicAverage = diastolicTotal/counter;
        if(systolicAverage > 180 || diastolicAverage > 120){
            conditionAverage = ConditionTypes.HYPERTENSIVE.toString();
        } else if(systolicAverage >= 140 || diastolicAverage >= 90){
            conditionAverage = ConditionTypes.STAGE2.toString();
        } else if(systolicAverage >= 130 || diastolicAverage >= 80){
            conditionAverage = ConditionTypes.STAGE1.toString();
        } else if(systolicAverage >= 120){
            conditionAverage = ConditionTypes.ELEVATED.toString();
        } else {
            conditionAverage = ConditionTypes.NORMAL.toString();
        }

        TextView txtSystolicAvg = findViewById(R.id.txtSystolicAvg);
        TextView txtDiastolicAvg = findViewById(R.id.txtDiastolicAvg);
        TextView txtConditionAvg = findViewById(R.id.txtConditionAvg);

        txtSystolicAvg.setText(Double.toString(systolicAverage));
        txtDiastolicAvg.setText(Double.toString(diastolicAverage));
        txtConditionAvg.setText(conditionAverage);

        Toast.makeText(MTDReport.this,
                "Month-to-Date Report:\n" +
                        "Systolic Average: " + systolicAverage + "\n" +
                        "Diastolic Average: " + diastolicAverage + "\n" +
                        "Condition Average: " + conditionAverage + "\n",
                Toast.LENGTH_LONG).show();
    }
}
