package in.diagnext.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class GraphActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(GraphActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_report)
            {
                Intent intent1 = new Intent(GraphActivity.this, GraphActivity.class);
                startActivity(intent1);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_health) {
                Intent intent2 = new Intent(GraphActivity.this, HealthActivity.class);
                startActivity(intent2);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_more)
            {
                Intent intent3=new Intent(GraphActivity.this,MoreActivity.class);
                startActivity(intent3);
                finish();
            }
            return true;
        }
    };
    private TextView weekDay_tv;
    private BarChart chart;
    private String userId;
    private FirebaseDatabase mfdb;
    private long dbDate;
    ArrayList<StepMaster> list=new ArrayList<>();
    private long _startingDay;
    private TextView totalSteps_Tv;
    private TextView averageSteps_tv;
    private Database database;
    private SharedPreferences userPref;
    private SharedPreferences pedosetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        setTitle("Reports");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(1);
        mItem.setChecked(true);

        pedosetting = getSharedPreferences("pedosetting", Context.MODE_PRIVATE);


        userPref = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        database=new Database(this);

        chart = findViewById(R.id.barchart);
       // bindBar();


        weekDay_tv=(TextView)findViewById(R.id.weekDay_tv);
        totalSteps_Tv=(TextView)findViewById(R.id.totalSteps_Tv);
        averageSteps_tv=(TextView)findViewById(R.id.averageSteps_tv);
        getWeekDays();


        bindData();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent(GraphActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void bindData()
    {
        ArrayList<StepMaster> ss =new ArrayList<>();
        ss=database.getWeeklySteps();

        int tStep = 0;
        int avgStep=0;
        ArrayList arrList =new ArrayList();

        for(int i=0;i<ss.size();i++)
        {

            list.add(new StepMaster(ss.get(i).date,ss.get(i).steps));
            arrList.add(ss.get(i).date);
            tStep = tStep + ss.get(i).steps;
        }
        if(list.size()>0) {
            avgStep = tStep / list.size();
            totalSteps_Tv.setText(tStep + " Steps");
            averageSteps_tv.setText("Daily Average : " + avgStep);
        }
        else
        {
            totalSteps_Tv.setText("0 Steps");
            averageSteps_tv.setText("Daily Average : 0");
        }

        ArrayList NoOfStep = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

        // get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        _startingDay = cal.getTimeInMillis();
        for(int i=0;i<=6;i++)
        {
            if(arrList.contains(_startingDay))
            {
                cal.setTimeInMillis(_startingDay);
                int position= arrList.indexOf(_startingDay);
                NoOfStep.add(new BarEntry(list.get(position).steps, i));
            }
            else
            {
                //NoOfStep.add(new BarEntry(0f, i));
            }
            cal.add(Calendar.DATE, 1);
            _startingDay=cal.getTimeInMillis();
        }

        ArrayList year = new ArrayList();


        year.add("M");
        year.add("T");
        year.add("W");
        year.add("T");
        year.add("F");
        year.add("S");
        year.add("S");


        BarDataSet bardataset = new BarDataSet(NoOfStep, "No Of Steps");
        chart.animateY(2000);
        chart.setPinchZoom(false);
        chart.setDrawValueAboveBar(true);
        chart.setDescription("");
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();
        }

        {

            LimitLine ll1 = new LimitLine(pedosetting.getInt("goalSteps", 500), "Goal ("+pedosetting.getInt("goalSteps", 500)+")");
            ll1.setLineWidth(2f);
            ll1.enableDashedLine(2f, 2f, 0f);
            ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll1.setTextSize(10f);
            //  ll1.setLineColor(R.color.black);
            //ll1.setTypeface(tfRegular);


            // draw limit lines behind data instead of on top
            yAxis.setDrawLimitLinesBehindData(false);

            // add limit lines
            yAxis.addLimitLine(ll1);

            //xAxis.addLimitLine(llXAxis);
        }

        BarData data = new BarData(year, bardataset);
        // bardataset.setColor(R.color.blue);
        bardataset.setColor(Color.rgb(255,215,0));
        bardataset.setValueTextSize(16);
        bardataset.setValueTextColor(Color.rgb(175,238,238));
        chart.setData(data);


        if(ss.size()==0)
        {
            totalSteps_Tv.setText(0 + " Steps");
            averageSteps_tv.setText("Daily Average : "+0);
        }

    }

    public void bindBar()
    {


        ArrayList NoOfStep = new ArrayList();

        NoOfStep.add(new BarEntry(0f, 0));
        NoOfStep.add(new BarEntry(0f, 1));
        NoOfStep.add(new BarEntry(0f, 2));
        NoOfStep.add(new BarEntry(0f, 3));
        NoOfStep.add(new BarEntry(0f, 4));
        NoOfStep.add(new BarEntry(0f, 5));
        NoOfStep.add(new BarEntry(0f, 6));

        ArrayList year = new ArrayList();


        year.add("M");
        year.add("T");
        year.add("W");
        year.add("T");
        year.add("F");
        year.add("S");
        year.add("S");


        BarDataSet bardataset = new BarDataSet(NoOfStep, "No Of Steps");
        chart.animateY(2000);
        chart.setPinchZoom(false);
        chart.setDrawValueAboveBar(true);
        chart.setDescription("");
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();
        }

        {

            LimitLine ll1 = new LimitLine(pedosetting.getInt("goalSteps", 500), "Goal");
            ll1.setLineWidth(2f);
            ll1.enableDashedLine(2f, 2f, 0f);
            ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll1.setTextSize(10f);
            //  ll1.setLineColor(R.color.black);
            //ll1.setTypeface(tfRegular);


            // draw limit lines behind data instead of on top
            yAxis.setDrawLimitLinesBehindData(false);

            // add limit lines
            yAxis.addLimitLine(ll1);

            //xAxis.addLimitLine(llXAxis);
        }

        BarData data = new BarData(year, bardataset);
        // bardataset.setColor(R.color.blue);
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chart.setData(data);
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
}
