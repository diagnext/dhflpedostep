package in.diagnext.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.diagnext.mylibrary.util.Util;
import solar.blaz.date.week.WeekDatePicker;


public class d_HealthActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(d_HealthActivity.this, d_MainActivity.class);
                startActivity(intent);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_report)
            {
            Intent intent1 = new Intent(d_HealthActivity.this, d_GraphActivity.class);
            startActivity(intent1);
            finish();
            }
            else if (item.getItemId() == R.id.navigation_health) {
                Intent intent2 = new Intent(d_HealthActivity.this, d_HealthActivity.class);
                startActivity(intent2);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_more)
            {
                    Intent intent3=new Intent(d_HealthActivity.this, d_MoreActivity.class);
                    startActivity(intent3);
                    finish();
            }
            return true;
        }
    };
    private LineChart chart;
    private SharedPreferences userPref;
    private boolean gender;
    private String height;
    private String weight;
    private String userId;
    private TextView title_txt;
    private EditText heightHealth_Et;
    private TextView titleWeight_txt;
    private EditText weightHealth_Et;
    private DatabaseReference dref;
    private TextView totalSteps_Tv;
    private TextView averageSteps_tv;
    private TextView weekDay_tv;
    private String weightMasterId;
    private long dbDate;
    ArrayList<d_WeightMaster> list=new ArrayList<>();
    private long _startingDay;
    private String _date;
    private WeekDatePicker datePicker;
    private long selectedDate;
    private TextView kg_txt;
    private TextView tDays_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diag_activity_health);

        //setTitle("Health");


            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);


            try {
                Menu menu = navigation.getMenu();
                MenuItem mItem = menu.getItem(2);
                mItem.setChecked(true);
                dref = FirebaseDatabase.getInstance().getReference();

                    userPref = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
                    userId = userPref.getString("userId", "");
                    gender = userPref.getBoolean("gender", true);
                    height = userPref.getString("height", "");
                    weight = userPref.getString("weight", "");



                totalSteps_Tv = (TextView) findViewById(R.id.totalSteps_Tv);
                averageSteps_tv = (TextView) findViewById(R.id.averageSteps_tv);
                weekDay_tv = (TextView) findViewById(R.id.weekDay_tv);
                kg_txt = (TextView) findViewById(R.id.kg_txt);
                tDays_txt = (TextView) findViewById(R.id.tDays_txt);

                kg_txt.setText(weight + " Kg");

                float _BMI = getBMI(Float.parseFloat(height), Float.parseFloat(weight));
                DecimalFormat df = new DecimalFormat("0.00");
                float _roundedBMI = Float.parseFloat(df.format(_BMI));
                totalSteps_Tv.setText("BMI (Kg/m2) : " + Float.parseFloat(df.format(_BMI)));

                averageSteps_tv.setText(getBMIStatus(_roundedBMI));

                chart = findViewById(R.id.barWeightchart);
                setData();
                getWeekDays();
                if(d_Constants.isNetworkAvailable(this))
                bindData();
                else
                    Toast.makeText(this,"Internet is Required",Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                userPref.edit().clear().commit();

                Intent intent = new Intent(d_HealthActivity.this, d_LoginActivity.class);
                startActivity(intent);
                finish();
            }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent(d_HealthActivity.this, d_MainActivity.class);
        startActivity(intent);
        finish();
    }

    private String getBMIStatus(float _roundedBMI)
    {
        String bmiStatus="";
        if(_roundedBMI <= 18.5f)
            bmiStatus="Under Weight";
        else if(_roundedBMI > 18.5f && _roundedBMI <=24.9)
            bmiStatus="Normal";
        else if(_roundedBMI >= 25f && _roundedBMI <=29.9)
            bmiStatus="Over Weight";
        else if(_roundedBMI >= 30f && _roundedBMI <=34.9)
            bmiStatus="Obese";
        else if(_roundedBMI > 35f)
            bmiStatus="Extremely Obese";
        else
            bmiStatus="Invalid";
        return  bmiStatus;
    }

    private void setData() {

        chart.setDescription("");
        ArrayList<Entry> entries = new ArrayList<>();





        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(entries, "Weight");


        set1.setCircleRadius(0f);


        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(set1);


        String [] day={"M","T","W","T","F","S","S"};



        // create a data object with the data sets
        LineData data = new LineData(day,sets);

        // set data
        chart.setData(data);
        chart.invalidate();
    }

    public long milliseconds(String date)
    {
        //String date_ = date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            Date mDate = sdf.parse(date);
            long timeInMilliseconds = mDate.getTime();
            return timeInMilliseconds;
        }
        catch (Exception e)
        {

        }

        return 0;
    }

    public void addWeight_Click(View view) {
        if(d_Constants.isNetworkAvailable(this)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.diag_layout_date_weight, null);
            dialogBuilder.setView(dialogView);

            title_txt = (TextView) dialogView.findViewById(R.id.title_txt);
            title_txt.setText("Enter Weight in Kgs");

            final EditText pick = (EditText) dialogView.findViewById(R.id.pickupKm_Et);
            pick.setText(weight);
            pick.setSelection(pick.getText().length());


            Button ok_btn = (Button) dialogView.findViewById(R.id.ok_btn);
            Button cancel_btn = (Button) dialogView.findViewById(R.id.cancel_btn);

            _date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            selectedDate = milliseconds(_date);

            datePicker = (WeekDatePicker) dialogView.findViewById(R.id.date_picker);
            datePicker.setDateIndicator(LocalDate.now(), true);

            datePicker.setOnDateSelectedListener(new WeekDatePicker.OnDateSelected() {
                @Override
                public void onDateSelected(org.threeten.bp.LocalDate date) {
                    _date = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    selectedDate = milliseconds(_date);
                    // bindData();
                }


            });


            final AlertDialog dlg = dialogBuilder.create();
            Window window = dlg.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.CENTER;
            // wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);

            ok_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pick.getText().toString().length() != 0) {
                        Map<String, Object> taskMap = new HashMap<String, Object>();

                        taskMap.put("weight", pick.getText().toString());
                        dref.child("d_UserMaster").child(userId).updateChildren(taskMap);

                        if (Util.getToday() == selectedDate) {
                            SharedPreferences.Editor editor = userPref.edit();
                            editor.putString("weight", pick.getText().toString());
                            editor.commit();

                            weight = pick.getText().toString();

                            float _BMI = getBMI(Float.parseFloat(height), Float.parseFloat(weight));
                            DecimalFormat df = new DecimalFormat("0.00");
                            float _roundedBMI = Float.parseFloat(df.format(_BMI));
                            totalSteps_Tv.setText("BMI (Kg/m2) : " + Float.parseFloat(df.format(_BMI)));

                            averageSteps_tv.setText(getBMIStatus(_roundedBMI));
                        }


                        insertWeight(selectedDate, pick.getText().toString());

                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                        }
                        dlg.hide();
                    } else
                        Toast.makeText(d_HealthActivity.this, "Weight is Required.", Toast.LENGTH_LONG).show();
                }
            });

            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                    }
                    dlg.hide();
                }
            });

            dlg.show();
        }
        else
            Toast.makeText(this,"Internet is Required",Toast.LENGTH_LONG).show();
    }

    public void heightWeight_Click(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.diag_health_layout, null);
        dialogBuilder.setView(dialogView);


        heightHealth_Et=(EditText)dialogView.findViewById(R.id.heightHealth_Et);
        weightHealth_Et=(EditText)dialogView.findViewById(R.id.weightHealth_Et);

        heightHealth_Et.setText(height);
        weightHealth_Et.setText(weight);


        Button ok_btn=(Button)dialogView.findViewById(R.id.ok_btn);
        Button cancel_btn=(Button)dialogView.findViewById(R.id.cancel_btn);


        final AlertDialog dlg = dialogBuilder.create();
        Window window = dlg.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        // wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(heightHealth_Et.getText().toString().length() !=0 &&  weightHealth_Et.getText().toString().length() !=0 ) {
                    Map<String, Object> taskMap = new HashMap<String, Object>();
                    taskMap.put("height", heightHealth_Et.getText().toString());
                    taskMap.put("weight", weightHealth_Et.getText().toString());
                    dref.child("d_UserMaster").child(userId).updateChildren(taskMap);

                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("height", heightHealth_Et.getText().toString());
                    editor.putString("weight", weightHealth_Et.getText().toString());
                    editor.commit();

                    height=heightHealth_Et.getText().toString();
                    weight=weightHealth_Et.getText().toString();

                    float _BMI=getBMI(Float.parseFloat(height),Float.parseFloat(weight));
                    DecimalFormat df = new DecimalFormat("0.00");
                    float _roundedBMI = Float.parseFloat(df.format(_BMI));
                    totalSteps_Tv.setText("BMI (Kg/m2) : "+Float.parseFloat(df.format(_BMI)));

                    averageSteps_tv.setText(getBMIStatus(_roundedBMI));



                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                    }
                    dlg.hide();

                    bindData();
                }
                else
                {
                    Toast.makeText(d_HealthActivity.this,"All Fields Required.",Toast.LENGTH_LONG).show();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                }
                dlg.hide();
            }
        });

        dlg.show();
    }

    public float getBMI(float _height, float _weight)
    {
        float bmi=0f;
        // convert height cm to meter
        float mHeight = _height * 0.01f;
        mHeight = mHeight*mHeight;

        return bmi=_weight/mHeight;
    }

    public void getWeekDays()
    {
        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        DateFormat df = new SimpleDateFormat("MMM dd");

        String fromDate = df.format(cal.getTimeInMillis());


        cal.add(Calendar.DATE, 6);
        String toDate = df.format(cal.getTimeInMillis());

        weekDay_tv.setText(fromDate + " - "+toDate);

    }

    public void insertWeight(final long date, final String _weight)
    {
        dref.child("d_WeightMaster").orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Long> arrDate=new ArrayList<>();
                    for (DataSnapshot StepSnapShot : dataSnapshot.getChildren()) {
                        d_WeightMaster dWeightMaster = StepSnapShot.getValue(d_WeightMaster.class);

                        weightMasterId = dWeightMaster.getWeightMasterId();

                        list.add(new d_WeightMaster(dWeightMaster.getWeightMasterId(), dWeightMaster.getUserId(), dWeightMaster.getDate(), dWeightMaster.getWeight()));
                        arrDate.add(dWeightMaster.getDate());
                        //dbDate = dWeightMaster.getDate();
                    }
                    if(arrDate.contains(date)) {
                        //update
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put("date", date);
                        taskMap.put("userId", userId);
                        taskMap.put("weight", _weight);


                        dref.child("d_WeightMaster/" + list.get(arrDate.indexOf(date)).weightMasterId).updateChildren(taskMap);


                    }
                    else {
                            //insert
                            String key = dref.child("d_WeightMaster").push().getKey();

                           d_WeightMaster sm = new d_WeightMaster();
                            sm.setWeightMasterId(key);
                            sm.setUserId(userId);
                            sm.setDate(date);
                            sm.setWeight(_weight);

                        dref.child("d_WeightMaster").child(key).setValue(sm);


                    }


                }
                else {


                    //insert
                    String key = dref.child("d_WeightMaster").push().getKey();

                    d_WeightMaster sm = new d_WeightMaster();
                    sm.setWeightMasterId(key);
                    sm.setUserId(userId);
                    sm.setDate(date);
                    sm.setWeight(_weight);

                    dref.child("d_WeightMaster").child(key).setValue(sm);




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void bindData()
    {
        dref.child("d_WeightMaster").orderByChild("userId").equalTo(userId).limitToLast(30).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList arrList =new ArrayList();
                    ArrayList arrWeight =new ArrayList();
                    arrList.clear();
                    arrWeight.clear();
                    list.clear();
                    for (DataSnapshot StepSnapShot : dataSnapshot.getChildren()) {
                        d_WeightMaster dWeightMaster = StepSnapShot.getValue(d_WeightMaster.class);

                        dbDate = dWeightMaster.getDate();
                        list.add(new d_WeightMaster(dWeightMaster.getWeightMasterId(), dWeightMaster.getUserId(), dWeightMaster.getDate(), dWeightMaster.getWeight()));
                        arrList.add(dWeightMaster.getDate());
                        if(!dWeightMaster.getWeight().equals(weight))
                        arrWeight.add(dWeightMaster.getWeight());
                    }


                    if(arrWeight.size()!=0) {
                        float tWeight = Float.valueOf(Collections.min(arrWeight).toString());
                        float wLoss = Float.valueOf(weight) - tWeight;

                        String tday =String.format("%.2f", wLoss) + " Kg";
                        tDays_txt.setText(tday.replace(".00",""));
                    }



                    kg_txt.setText(weight +" Kg");


                    chart.setDescription("");
                    ArrayList<Entry> entries = new ArrayList<>();

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                    cal.set(Calendar.MINUTE,0);
                    cal.set(Calendar.SECOND,0);
                    cal.set(Calendar.MILLISECOND,0);

                    // get start of this week in milliseconds
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    _startingDay = cal.getTimeInMillis();


                    entries.clear();
                    for(int i=0;i<=29;i++)
                    {
                        if(arrList.contains(_startingDay))
                        {
                            cal.setTimeInMillis(_startingDay);
                            int position= arrList.indexOf(_startingDay);
                            entries.add(new Entry(Float.valueOf(list.get(position).weight), i));

                        }
                        else
                        {
                            //NoOfStep.add(new BarEntry(0f, i));
                        }


                        cal.add(Calendar.DATE, 1);
                        _startingDay=cal.getTimeInMillis();
                    }

                    // create a dataset and give it a type
                    LineDataSet set1 = new LineDataSet(entries, "Weight");

                    set1.setLineWidth(3f);
                    set1.setCircleRadius(4f);
                    set1.setValueTextSize(14);
                    set1.setCircleColor(Color.rgb(240, 238, 70));
                    set1.setColor(Color.rgb(255,165,0));
                    set1.setValueTextColor(Color.rgb(175,238,238));

                    ArrayList<ILineDataSet> sets = new ArrayList<>();
                    sets.add(set1);


                    String [] day={"M","T","W","T","F","S","S"};



// create a data object with the data sets
                    LineData data = new LineData(day,sets);

// set data
                    chart.setData(data);
                    chart.invalidate();


                }
                else
                {
                    kg_txt.setText(weight+" Kg");
                    tDays_txt.setText("0.0 Kg");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
