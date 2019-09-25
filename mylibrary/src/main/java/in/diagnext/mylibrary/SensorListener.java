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
import android.os.IBinder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import in.diagnext.mylibrary.util.API26Wrapper;
import in.diagnext.mylibrary.util.Util;


public class SensorListener extends Service implements SensorEventListener,StepListener {
    public final static int NOTIFICATION_ID = 1;
    private int stepCount;
    private StepDetector simpleStepDetector;
    private Database database;
    private final BroadcastReceiver shutdownReceiver = new ShutdownRecevier();

    public SensorListener() {
    }

    @Override
    public void onCreate() {
        super.onCreate();



       // showNotification();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database=new Database(this);
        simpleStepDetector = new StepDetector();

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

        simpleStepDetector.registerListener(this,threshold);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(stepCount, true, "175",
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {

        stepCount = stepCount+1;
        int state=database.saveCurrentSteps(stepCount, Util.getToday());
        if(state==2)
            stepCount=0;

        showNotification();

       // listener.step(timeNs,String.valueOf(rounderKm),String.valueOf(roundekcal),hours+"h "+mins+"m");


    }


    public static Notification getNotification(final Context context) {
        SharedPreferences pedosetting = context.getSharedPreferences("pedosetting", Context.MODE_PRIVATE);
        int goal = pedosetting.getInt("goalSteps", 10000);
        int stepCount=0;
        ArrayList<StepMaster> ss =new ArrayList<>();
        Database data=new Database(context);
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
                        .getActivity(context, 0, new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
              .setSmallIcon(R.mipmap.ic_notification).setOngoing(true);
        return notificationBuilder.build();

    }




    private int getDbSteps()
    {
        int stepCount=0;
        ArrayList<StepMaster> ss =new ArrayList<>();
        ss= database.getSteps(String.valueOf(Util.getToday()));
        if(ss.size()>0) {
            stepCount = ss.get(0).steps;
        }
       // data.close();
        return stepCount;
    }
}
