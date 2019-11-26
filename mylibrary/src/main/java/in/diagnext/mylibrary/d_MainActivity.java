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
import com.google.android.material.circularreveal.CircularRevealGridLayout;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import in.diagnext.mylibrary.util.API26Wrapper;
import in.diagnext.mylibrary.util.Util;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import xyz.gracefulife.stepindicator.StepsView;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;


public class d_MainActivity extends AppCompatActivity  implements SensorEventListener {
    private d_Database dDatabase;
    private SensorManager sensorManager;
    private Sensor accel;
    private d_StepDetector simpleDStepDetector;
    private int numSteps;
    private float tMin;
    private boolean gender;
    private String height;
    private TextView distance_tv;
    private TextView calories_tv;
    private TextView time_tv;
    private int stepCount;
    private TextView tvSteps;
 //   private ImageView BtnStart;
 //   private ImageView BtnStop;
    private SharedPreferences prefs;
    private TextView totalSteps_txt;
    private TextView policyStart_txt;
    private TextView policyExp_txt;
    private final OkHttpClient httpClient = new OkHttpClient();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(d_MainActivity.this, d_MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_report)
            {
                Intent intent1 = new Intent(d_MainActivity.this, d_GraphActivity.class);
                startActivity(intent1);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_health) {
                Intent intent2 = new Intent(d_MainActivity.this, d_bmiActivity.class);
                startActivity(intent2);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_more)
            {
                Intent intent3=new Intent(d_MainActivity.this, d_MoreActivity.class);
                startActivity(intent3);
                finish();
                return true;
            }
            return false;
        }
    };
    private String userId;
    private StepsView indicatorSteps;
    private int differenceYr=0;
    private TextView targetAchive_txt;
    private ImageView distance_IMG;
    private ImageView calories_IMG;
    private ImageView clock_IMG;
    private CircularRevealGridLayout grdImages;
    private String uName;
    private String pWord;
    private int dbSteps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diag_activity_main_d);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(0);


        tvSteps = (TextView) findViewById(R.id.tv_steps);
        distance_tv = (TextView) findViewById(R.id.distance_tv);
        calories_tv = (TextView) findViewById(R.id.calories_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
      //  BtnStart = (ImageView) findViewById(R.id.btn_start);
      //  BtnStop = (ImageView) findViewById(R.id.btn_stop);
        totalSteps_txt= (TextView)findViewById(R.id.totalSteps_txt);
        policyStart_txt= (TextView)findViewById(R.id.policyStart_txt);
        policyExp_txt= (TextView)findViewById(R.id.policyExp_txt);
        indicatorSteps=(xyz.gracefulife.stepindicator.StepsView)findViewById(R.id.step_view);
        targetAchive_txt=(TextView)findViewById(R.id.targetAchive_txt);
        distance_IMG = (ImageView) findViewById(R.id.distance_IMG);
        calories_IMG = (ImageView) findViewById(R.id.calories_IMG);
        clock_IMG = (ImageView) findViewById(R.id.clock_IMG);
        grdImages = (com.google.android.material.circularreveal.CircularRevealGridLayout)findViewById(R.id.grdImages);

        prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        height = prefs.getString("height","175");
        userId = prefs.getString("userId", "0000");
        int totalSteps = prefs.getInt("totalSteps", 0);
        totalSteps_txt.setText(String.valueOf(totalSteps));
        policyStart_txt.setText("Policy Start date : " +prefs.getString("policyStart", ""));
        policyExp_txt.setText("Policy Exp date : " +prefs.getString("policyExp", ""));

        distance_IMG.setScaleType(ImageView.ScaleType.FIT_XY);
        calories_IMG.setScaleType(ImageView.ScaleType.FIT_XY);
        clock_IMG.setScaleType(ImageView.ScaleType.FIT_XY);
      //  calories_tv = (TextView) findViewById(R.id.calories_tv);
      //  time_tv = (TextView) findViewById(R.id.time_tv);

        String policyStarts = prefs.getString("policyStart", "");
        String policyExp = prefs.getString("policyExp", "");
     try {
         Date dateStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(policyStarts);
         Date dateEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(policyExp);
         differenceYr=getYear(dateStart,dateEnd);
         String text="Target to achieve discount in "+String.valueOf(differenceYr)+" year";
         targetAchive_txt.setText(text);

         DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
         String strFromDate = dateFormat.format(dateStart);
         String strToDate= dateFormat.format(dateEnd);

         policyStart_txt.setText("Target / Policy Start date : " +strFromDate);
         policyExp_txt.setText("Target / Policy End date : " +strToDate);


     }
     catch (Exception ex)
     {


         prefs.edit().clear().commit();
         stopService(new Intent(getBaseContext(), d_SensorListener.class));
         Intent intent = new Intent(d_MainActivity.this, d_UserLogin.class);
         startActivity(intent);
         finish();
     }
        if(differenceYr <=1) {
            if (totalSteps >= 1200000)
                indicatorSteps.setLabels(new String[]{"1,200,000 \n 3%", "1,500,000 \n 5%", "2,000,000 \n 7%", "2,500,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(1)
                        .drawView();
            else if (totalSteps >= 1600000)
                indicatorSteps.setLabels(new String[]{"1,200,000 \n 3%", "1,500,000 \n 5%", "2,000,000 \n 7%", "2,500,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(2)
                        .drawView();
            else if (totalSteps >= 2000000)
                indicatorSteps.setLabels(new String[]{"1,200,000 \n 3%", "1,500,000 \n 5%", "2,000,000 \n 7%", "2,500,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(3)
                        .drawView();
            else if (totalSteps >= 2500000)
                indicatorSteps.setLabels(new String[]{"1,200,000 \n 3%", "1,500,000 \n 5%", "2,000,000 \n 7%", "2,500,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(4)
                        .drawView();
            else
                indicatorSteps.setLabels(new String[]{"1,200,000 \n 3%", "1,500,000 \n 5%", "2,000,000 \n 7%", "2,500,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(0)
                        .drawView();
        }
        else if(differenceYr ==2) {
            if (totalSteps >= 2520000)
                indicatorSteps.setLabels(new String[]{"2,520,000 \n 3%", "3,120,000 \n 5%", "4,160,000 \n 7%", "5,200,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(1)
                        .drawView();
            else if (totalSteps >= 3360000)
                indicatorSteps.setLabels(new String[]{"2,520,000 \n 3%", "3,120,000 \n 5%", "4,160,000 \n 7%", "5,200,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(2)
                        .drawView();
            else if (totalSteps >= 4200000)
                indicatorSteps.setLabels(new String[]{"2,520,000 \n 3%", "3,120,000 \n 5%", "4,160,000 \n 7%", "5,200,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(3)
                        .drawView();
            else if (totalSteps >= 5200000)
                indicatorSteps.setLabels(new String[]{"2,520,000 \n 3%", "3,120,000 \n 5%", "4,160,000 \n 7%", "5,200,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(4)
                        .drawView();
            else
                indicatorSteps.setLabels(new String[]{"2,520,000 \n 3%", "3,120,000 \n 5%", "4,160,000 \n 7%", "5,200,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(0)
                        .drawView();
        }
        else if(differenceYr ==3) {
            if (totalSteps >= 4200000)
                indicatorSteps.setLabels(new String[]{"4,200,000 \n 3%", "4,800,000 \n 5%", "6,400,000 \n 7%", "8,000,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(1)
                        .drawView();
            else if (totalSteps >= 5600000)
                indicatorSteps.setLabels(new String[]{"4,200,000 \n 3%", "4,800,000 \n 5%", "6,400,000 \n 7%", "8,000,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(2)
                        .drawView();
            else if (totalSteps >= 7000000)
                indicatorSteps.setLabels(new String[]{"4,200,000 \n 3%", "4,800,000 \n 5%", "6,400,000 \n 7%", "8,000,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(3)
                        .drawView();
            else if (totalSteps >= 8000000)
                indicatorSteps.setLabels(new String[]{"4,200,000 \n 3%", "4,800,000 \n 5%", "6,400,000 \n 7%", "8,000,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(4)
                        .drawView();
            else
                indicatorSteps.setLabels(new String[]{"4,200,000 \n 3%", "4,800,000 \n 5%", "6,400,000 \n 7%", "8,000,000 \n 10%"})
                        .setProgressColorIndicator(ContextCompat.getColor(this, R.color.yellow))
                        .setBarColorIndicator(ContextCompat.getColor(this, R.color.white))
                        .setCompletedPosition(0)
                        .drawView();




           uName= prefs.getString("uName", "0000");
           pWord = prefs.getString("pWord", "0000");

          // dbSteps = getTotalCount();

        }
    }






    @Override
    protected void onResume() {
        super.onResume();
try {
    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    dDatabase = new d_Database(this);
    stepCount = 0;


    int checkState = prefs.getInt("start", 0);
    if (checkState == 1) {
        sensorManager.registerListener(d_MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        // BtnStart.setVisibility(View.GONE);
        //  BtnStop.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= 26) {
            API26Wrapper.startForegroundService(d_MainActivity.this, new Intent(d_MainActivity.this, d_SensorListener.class));
        } else {
            startService(new Intent(d_MainActivity.this, d_SensorListener.class));
        }

    }


    ArrayList<d_StepMaster> ss = new ArrayList<>();
    ss = dDatabase.getSteps(String.valueOf(Util.getToday()));
    if (ss.size() > 0) {



        tvSteps.setText(String.valueOf(ss.get(0).steps));
        stepCount = ss.get(0).steps;

        bindHealthDatas(stepCount);


    }
    //dDatabase.close();

    onLogin();
}
catch (Exception ex)
{
    sensorManager.unregisterListener(d_MainActivity.this);
    //  BtnStart.setVisibility(View.VISIBLE);
    //  BtnStop.setVisibility(View.GONE);
    prefs.edit().putInt("start", 0).commit();
    stopService(new Intent(getBaseContext(), d_SensorListener.class));


    prefs.edit().clear().commit();
    stopService(new Intent(getBaseContext(), d_SensorListener.class));
    Intent intent = new Intent(d_MainActivity.this, d_UserLogin.class);
    startActivity(intent);
    finish();
}
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
            ArrayList<d_StepMaster> ss =new ArrayList<>();
            ss= dDatabase.getSteps(String.valueOf(Util.getToday()));
            if(ss.size()>0) {



                totalSteps_txt.setText(String.valueOf(prefs.getInt("totalSteps",0)));
            //    prefs.edit().putInt("pushSteps", Integer.valueOf(totalSteps_txt.getText().toString())).commit();



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

        sensorManager.unregisterListener(d_MainActivity.this);
      //  BtnStart.setVisibility(View.VISIBLE);
      //  BtnStop.setVisibility(View.GONE);
        prefs.edit().putInt("start", 0).commit();
        stopService(new Intent(getBaseContext(), d_SensorListener.class));


        prefs.edit().clear().commit();
        stopService(new Intent(getBaseContext(), d_SensorListener.class));
        Intent intent = new Intent(d_MainActivity.this, d_UserLogin.class);
        startActivity(intent);
        finish();
    }

    public void onLogin()
    {
        sensorManager.registerListener(d_MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //BtnStart.setVisibility(View.GONE);
       // BtnStop.setVisibility(View.VISIBLE);
        prefs.edit().putInt("start", 1).commit();


        if (Build.VERSION.SDK_INT >= 26) {
            API26Wrapper.startForegroundService(d_MainActivity.this, new Intent(d_MainActivity.this, d_SensorListener.class));
        } else {
            startService(new Intent(d_MainActivity.this, d_SensorListener.class));

        }
    }



    int getYear(Date date1,Date date2){
        SimpleDateFormat simpleDateformat=new SimpleDateFormat("yyyy");
        int year=Integer.parseInt(simpleDateformat.format(date1));

        return Integer.parseInt(simpleDateformat.format(date2))- Integer.parseInt(simpleDateformat.format(date1));

    }

    public int getTotalCount()
    {
        d_Data data = null;
        String message,code = null;
        int count = 0;
        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("member_id", uName)
                .add("password", pWord)
                .build();

        Request request = new Request.Builder()
                .url("https://api.dhflgi.com/health/api/pedometer/member/login")
                .addHeader("username", "cococure_pedometer")
                .addHeader("password", "25ttgHY2uSgjEb6ctesdf")
                .post(formBody)
                .build();

        // httpClient.newBuilder().connectTimeout(5, TimeUnit.MINUTES);

        try (ResponseBody response = httpClient.newCall(request).execute().body()) {

            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String results = response.string();
            results= results.replace(",\"data\":\"No member found\"","");

            Gson gson = new Gson();
            d_loginEntity entity = gson.fromJson(results, d_loginEntity.class);

            message=entity.getMessage();
            data= entity.getData();
            code=entity.getCode();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(code.equals("1")) {

            count = Integer.valueOf(data.getStep_count());
        }
        return count;
    }
}
