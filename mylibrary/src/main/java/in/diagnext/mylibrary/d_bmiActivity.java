package in.diagnext.mylibrary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
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

public class d_bmiActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(d_bmiActivity.this, d_MainActivity.class);
                startActivity(intent);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_report)
            {
                Intent intent1 = new Intent(d_bmiActivity.this, d_GraphActivity.class);
                startActivity(intent1);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_health) {
                Intent intent2 = new Intent(d_bmiActivity.this, d_bmiActivity.class);
                startActivity(intent2);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_more)
            {
                Intent intent3=new Intent(d_bmiActivity.this, d_MoreActivity.class);
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
    private TextView totalSteps_Tv;
    private TextView averageSteps_tv;
    private TextView kg_txt;
    private WebView webView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_bmi);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);


        try {
            Menu menu = navigation.getMenu();
            MenuItem mItem = menu.getItem(2);
            mItem.setChecked(true);


            userPref = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            userId = userPref.getString("userId", "");
            gender = userPref.getBoolean("gender", true);
            height = userPref.getString("height", "");
            weight = userPref.getString("weight", "");


            kg_txt= (TextView) findViewById(R.id.kg_txt);
            totalSteps_Tv = (TextView) findViewById(R.id.totalSteps_Tv);
            averageSteps_tv = (TextView) findViewById(R.id.averageSteps_tv);

            webView1=(WebView)findViewById(R.id.webView1);
            kg_txt.setText("Current Weight : "+weight + " Kg");

            float _BMI = getBMI(Float.parseFloat(height), Float.parseFloat(weight));
            DecimalFormat df = new DecimalFormat("0.00");
            float _roundedBMI = Float.parseFloat(df.format(_BMI));
            totalSteps_Tv.setText("BMI (Kg/m2) : " + Float.parseFloat(df.format(_BMI)));

            averageSteps_tv.setText(getBMIStatus(_roundedBMI));

            String text;
            text = "<html><body><p align=\"justify\" style=\"color:#A2A1A1\">";
            text+= "<b>Body mass index (BMI)</b> is a value derived from the mass (weight) and height of a person. The BMI is defined as the body mass divided by the square of the body height, and is universally expressed in units of kg/m<sup>2</sup>, resulting from mass in kilograms and height in metres.";
            text+= "</p>" +
                    "<p align=\"justify\" style=\"color:#A2A1A1\">A high BMI can be an indicator of high body fatness. BMI can be used as a screening tool but is not diagnostic of the body fatness or health of an individual.</p>" +
                    //"<p align=\"justify\" style=\"color:#A2A1A1\">To determine if a high BMI is a health risk, a healthcare provider would need to perform further assessments. These assessments might include skinfold thickness measurements, evaluations of diet, physical activity, family history, and other appropriate health screenings.</p>" +
                    "</body></html>";
            webView1.loadData(text, "text/html", "utf-8");
            webView1.setVerticalScrollBarEnabled(true);



        } catch (Exception ex) {
            userPref.edit().clear().commit();

            Intent intent = new Intent(d_bmiActivity.this, d_LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent(d_bmiActivity.this, d_MainActivity.class);
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




    public float getBMI(float _height, float _weight)
    {
        float bmi=0f;
        // convert height cm to meter
        float mHeight = _height * 0.01f;
        mHeight = mHeight*mHeight;

        return bmi=_weight/mHeight;
    }




}
