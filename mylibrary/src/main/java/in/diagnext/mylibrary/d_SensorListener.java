package in.diagnext.mylibrary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import in.diagnext.mylibrary.util.API26Wrapper;
import in.diagnext.mylibrary.util.Util;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


public class d_SensorListener extends Service implements SensorEventListener, d_StepListener {
    public final static int NOTIFICATION_ID = 1;
    private int stepCount;
    private d_StepDetector simpleDStepDetector;
    private d_Database dDatabase;
    private final BroadcastReceiver shutdownReceiver = new d_ShutdownRecevier();
    private SharedPreferences prefs;
    private String userId;
    private final OkHttpClient httpClient = new OkHttpClient();
    public static final long NOTIFY_INTERVAL =60 * 60 * 1000; // 10 seconds

    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private SharedPreferences prefs1;

    public d_SensorListener() {

    }

    @Override
    public void onCreate() {
        super.onCreate();


        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);

    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // display toast
                    try {
                        Toast.makeText(getApplicationContext(), "Synchronizing Pedometer Steps",
                                Toast.LENGTH_LONG).show();

                        Thread thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    //Your code goes here
                                    pushSteps();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        thread.start();


                    }
                    catch (Exception ex)
                    {
                        String msg=ex.getMessage();
                        String msg1=ex.getMessage();
                    }
                }

            });
        }



    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dDatabase =new d_Database(this);
        simpleDStepDetector = new d_StepDetector();

        reRegisterSensor();
        registerBroadcastReceiver();

        showNotification();



        return START_STICKY;
    }

    public void reRegisterSensor() {


        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) { }


        //if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(NOTIFICATION_ID, getNotification(this));
        }
        else if (getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                .getBoolean("notification", true)) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, getNotification(this));
        }
    }

    //Register Broadcast Receiver
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) { }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        stepCount = getDbSteps();
        float threshold = 50f;
        String thres = getSharedPreferences("pedosetting", Context.MODE_PRIVATE).getString("sensitivity", "Medium");
        if(thres.equals("Low"))
            threshold = 30f;
        else if (thres.equals("High"))
            threshold = 60f;

        simpleDStepDetector.registerListener(this,threshold);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleDStepDetector.updateAccel(stepCount, true, "175",
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {

        stepCount = stepCount+1;
        int state= dDatabase.saveCurrentSteps(stepCount, Util.getToday());
        if(state==2)
            stepCount=0;

        showNotification();



       // listener.step(timeNs,String.valueOf(rounderKm),String.valueOf(roundekcal),hours+"h "+mins+"m");


    }


    public static Notification getNotification(final Context context) {
        SharedPreferences pedosetting = context.getSharedPreferences("pedosetting", Context.MODE_PRIVATE);
        int goal = pedosetting.getInt("goalSteps", 10000);
        int stepCount=0;
        ArrayList<d_StepMaster> ss =new ArrayList<>();
        d_Database data=new d_Database(context);
        ss= data.getSteps(String.valueOf(Util.getToday()));
        if(ss.size()>0) {
            stepCount = ss.get(0).steps;
        }
        Notification.Builder notificationBuilder =
                Build.VERSION.SDK_INT >= 26 ? API26Wrapper.getNotificationBuilder(context) :
                        new Notification.Builder(context);
        if (stepCount > 0) {
            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            notificationBuilder.setProgress(goal, stepCount, false).setContentText(
                    stepCount >= goal ?
                            context.getString(R.string.goal_reached_notification,
                                    format.format((stepCount))) :
                            context.getString(R.string.notification_text,
                                    format.format((goal - stepCount)))).setContentTitle(
                    format.format(stepCount) + " " + context.getString(R.string.steps));
        }else { // still no step value?
            notificationBuilder.setContentText(
                    context.getString(R.string.your_progress_will_be_shown_here_soon))
                    .setContentTitle(context.getString(R.string.notification_title));
        }
        notificationBuilder.setPriority(Notification.PRIORITY_MIN).setShowWhen(false)
                .setContentIntent(PendingIntent
                        .getActivity(context, 0, new Intent(context, d_MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
              .setSmallIcon(R.mipmap.ic_notification).setOngoing(true);
        return notificationBuilder.build();

    }




    private int getDbSteps()
    {
        int stepCount=0;
        ArrayList<d_StepMaster> ss =new ArrayList<>();
        ss= dDatabase.getSteps(String.valueOf(Util.getToday()));
        if(ss.size()>0) {
            stepCount = ss.get(0).steps;
        }
       // data.close();
        return stepCount;
    }

    private void pushSteps() throws Exception {

        prefs = getSharedPreferences("pedo", Context.MODE_PRIVATE);
        prefs1 = getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        int stepCount=0;
        ArrayList<d_StepMaster> ss =new ArrayList<>();
        d_Database data=new d_Database(this);
        ss= data.getSteps(String.valueOf(Util.getToday()));
        if(ss.size()>0) {
            stepCount = ss.get(0).steps;

        }
        int dbstep = getTotalCount();

        int pushCount = 0;
        int saveSteps = prefs.getInt("saveSteps", 0);
        if(saveSteps<stepCount)
         pushCount = dbstep+(stepCount-saveSteps);
        else
            pushCount = dbstep;

        prefs.edit().putInt("saveSteps", Integer.valueOf(stepCount)).commit();

        prefs1.edit().putInt("totalSteps", Integer.valueOf(pushCount)).commit();




        userId = prefs1.getString("userId", "");
        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("member_id", userId)
                .add("step_count", String.valueOf(pushCount))
                .build();

        Request request = new Request.Builder()
                .url("https://devapi.dhflgi.com/health/api/pedometer/member/update/stepcount")
                .addHeader("username", "cococure_partner")
                .addHeader("password", "JWGnYDJw7k4YEnxy")
                .post(formBody)
                .build();

        // httpClient.newBuilder().connectTimeout(5, TimeUnit.MINUTES);
        d_Data data1;
        String message, code;
        try (ResponseBody response = httpClient.newCall(request).execute().body()) {

            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String results = response.string();
            results = results.replace(",\"data\":\"Nothing to update\"", "").replace(",\"data\":\"Step Count updated Successfully\"", "");

            Gson gson = new Gson();
            d_loginEntity entity = gson.fromJson(results, d_loginEntity.class);

            message = entity.getMessage();
            data1 = entity.getData();
            code = entity.getCode();

        }
    }

    public int getTotalCount()
    {

        prefs1 = getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        d_Data data = null;
        String message,code = null;
        int count = 0;
        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("member_id", prefs1.getString("member_id", "0000"))
                .add("password", prefs1.getString("password", "0000"))
                .build();

        Request request = new Request.Builder()
                .url("https://devapi.dhflgi.com/health/api/pedometer/member/login")
                .addHeader("username", "cococure_partner")
                .addHeader("password", "JWGnYDJw7k4YEnxy")
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
