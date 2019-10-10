package in.diagnext.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import in.diagnext.mylibrary.util.API26Wrapper;
import in.diagnext.mylibrary.util.Util;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {
    private Database database;
    private SensorManager sensorManager;
    private Sensor accel;
    private StepDetector simpleStepDetector;
    private int numSteps;
    private float tMin;
    private boolean gender;
    private String height;
    private TextView distance_tv;
    private TextView calories_tv;
    private TextView time_tv;
    private int stepCount;
    private TextView tvSteps;
    private ImageView BtnStart;
    private ImageView BtnStop;
    private SharedPreferences prefs;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_report)
            {
                Intent intent1 = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(intent1);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_health) {
                Intent intent2 = new Intent(MainActivity.this, HealthActivity.class);
                startActivity(intent2);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_more)
            {
                Intent intent3=new Intent(MainActivity.this,MoreActivity.class);
                startActivity(intent3);
                finish();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_d);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(0);


        tvSteps = (TextView) findViewById(R.id.tv_steps);
        distance_tv = (TextView) findViewById(R.id.distance_tv);
        calories_tv = (TextView) findViewById(R.id.calories_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
        BtnStart = (ImageView) findViewById(R.id.btn_start);
        BtnStop = (ImageView) findViewById(R.id.btn_stop);


        prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        height = prefs.getString("height","175");

        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                BtnStart.setVisibility(View.GONE);
                BtnStop.setVisibility(View.VISIBLE);

                prefs.edit().putInt("start", 1).commit();


                if (Build.VERSION.SDK_INT >= 26) {
                    API26Wrapper.startForegroundService(MainActivity.this, new Intent(MainActivity.this, SensorListener.class));
                } else {
                    startService(new Intent(MainActivity.this, SensorListener.class));

                }
            }
        });



        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(MainActivity.this);
                BtnStart.setVisibility(View.VISIBLE);
                BtnStop.setVisibility(View.GONE);

                prefs.edit().putInt("start", 0).commit();

                stopService(new Intent(getBaseContext(), SensorListener.class));

            }
        });


    }






    @Override
    protected void onResume() {
        super.onResume();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        database=new Database(this);
        stepCount=0;


        int checkState = prefs.getInt("start",0);
        if(checkState==1)
        {
            sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
            BtnStart.setVisibility(View.GONE);
            BtnStop.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= 26) {
                API26Wrapper.startForegroundService(MainActivity.this, new Intent(MainActivity.this, SensorListener.class));
            } else {
                startService(new Intent(MainActivity.this, SensorListener.class));
            }

        }


        ArrayList<StepMaster> ss =new ArrayList<>();
        ss= database.getSteps(String.valueOf(Util.getToday()));
        if(ss.size()>0) {
            tvSteps.setText(String.valueOf(ss.get(0).steps));
            stepCount = ss.get(0).steps;

            bindHealthDatas(stepCount);
        }
        //database.close();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        else
        {
            ArrayList<StepMaster> ss =new ArrayList<>();
            ss= database.getSteps(String.valueOf(Util.getToday()));
            if(ss.size()>0) {
                tvSteps.setText(String.valueOf(ss.get(0).steps));
                stepCount = ss.get(0).steps;

                bindHealthDatas(stepCount);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void bindHealthDatas(int step)
    {
        float tMin=step*0.009f;
        //Distance
        float mof = 0.413f;
        if(gender)
            mof = 0.415f;

        //Height in feet
        float feetHeight = Integer.valueOf(height) * 0.0328084f;
        float stride = feetHeight * mof;

        //Stride
        float distance = stride * step;
        int roundDistance = Math.round(distance);

        //feet to km
        float feettokm = roundDistance * 0.0003048f;
        DecimalFormat df = new DecimalFormat("0.00");
        float rounderKm = Float.parseFloat(df.format(feettokm));
        float roundekcal = Float.parseFloat(df.format(step*0.045f));

        int hours=(int)(tMin/60);
        int mins =(int)(tMin%60);

        distance_tv.setText(String.valueOf(rounderKm));
        calories_tv.setText(String.valueOf(roundekcal));
        time_tv.setText(hours+"h "+mins+"m");


    }

    public void logout_click(View view) {
        prefs.edit().clear().commit();
        stopService(new Intent(getBaseContext(), SensorListener.class));
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
