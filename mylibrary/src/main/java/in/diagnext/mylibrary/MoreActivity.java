package in.diagnext.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MoreActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(MoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_report)
            {
                Intent intent1 = new Intent(MoreActivity.this, GraphActivity.class);
                startActivity(intent1);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_health) {
                Intent intent2 = new Intent(MoreActivity.this, HealthActivity.class);
                startActivity(intent2);
                finish();
            }
            else if (item.getItemId() == R.id.navigation_more)
            {
                Intent intent3=new Intent(MoreActivity.this,MoreActivity.class);
                startActivity(intent3);
                finish();
            }
            return true;
        }
    };
    private TextView height_tv;
    private TextView weight_tv;
    private String userId;
    private boolean gender;
    private String height;
    private String weight;
    private Spinner stepGoal_ddl;
    private DatabaseReference dref;
    private SharedPreferences userPref;
    private int goalStep;
    private String sensitivity;
    private Spinner sensitivity_ddl;
    private Spinner gender_ddl;
    private TextView title_txt;
    private SharedPreferences pedosetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        setTitle("More");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(3);
        mItem.setChecked(true);

        dref = FirebaseDatabase.getInstance().getReference().child("UserMaster");

        pedosetting = getSharedPreferences("pedosetting", Context.MODE_PRIVATE);
        goalStep = pedosetting.getInt("goalSteps", 500);
        sensitivity = pedosetting.getString("sensitivity", "Medium");


        userPref = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        userId = userPref.getString("userId", "");
        gender = userPref.getBoolean("gender", true);
        height = userPref.getString("height", "");
        weight = userPref.getString("weight", "");


        stepGoal_ddl=(Spinner)findViewById(R.id.stepGoal_ddl);
        sensitivity_ddl=(Spinner)findViewById(R.id.sensitivity_ddl);
        gender_ddl=(Spinner)findViewById(R.id.gender_ddl);
        height_tv=(TextView)findViewById(R.id.height_tv);
        weight_tv=(TextView)findViewById(R.id.weight_tv);

        String _sex="Female";
        if(gender)
        {
            _sex="Male";
        }

        height_tv.setText(height + " Cms");
        weight_tv.setText(weight + " Kg");


        bindGoal();
        bindSensitivity();
        bindGender();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent(MoreActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void bindGoal()
    {
        final ArrayList<String> item = new ArrayList<String>();
        //item.add("Select Date");
        for (int i = 500; i <= 40000; i=i+500) {
            item.add(String.valueOf(i));

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        stepGoal_ddl.setAdapter(adapter);
        stepGoal_ddl.setSelection(item.indexOf(String.valueOf(goalStep)));

        stepGoal_ddl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Map<String,Object> taskMap = new HashMap<String,Object>();
                //taskMap.put("goalSteps", stepGoal_ddl.getSelectedItem().toString());
                //dref.child(userId).updateChildren(taskMap);

                SharedPreferences.Editor edit = pedosetting.edit();
                edit.putInt("goalSteps", Integer.valueOf(stepGoal_ddl.getSelectedItem().toString()));
                edit.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void bindSensitivity()
    {
        String[] item = new String[]{"Low","Medium","High"};
        //item.add("Select Date");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        sensitivity_ddl.setAdapter(adapter);
        sensitivity_ddl.setSelection(Arrays.asList(item).indexOf(sensitivity));

        sensitivity_ddl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                SharedPreferences.Editor edit = pedosetting.edit();
                edit.putString("sensitivity", sensitivity_ddl.getSelectedItem().toString());
                edit.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void bindGender()
    {

        String[] item = new String[]{"Male","Female"};
        //item.add("Select Date");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        gender_ddl.setAdapter(adapter);
        String _search="Female";
        if(gender)
            _search="Male";
        gender_ddl.setSelection(Arrays.asList(item).indexOf(_search));

        gender_ddl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean _gender = true;
                if(gender_ddl.getSelectedItem().toString().equals("Female"))
                    _gender = false;

                Map<String,Object> taskMap = new HashMap<String,Object>();
                taskMap.put("gender", _gender);
                dref.child(userId).updateChildren(taskMap);

                SharedPreferences.Editor editor = userPref.edit();
                editor.putBoolean("gender", _gender);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void stepGoal_click(View view) {

    }

    public void sensitivity_click(View view) {

    }

    public void height_click(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        showPhysicsDialog("height","Height in Cms");
    }

    public void weight_click(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        showPhysicsDialog("weight","Weight in Kgs");
    }

    public void gender_click(View view) {

    }

    public void showPhysicsDialog(final String _physics, String title) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_physics, null);
        dialogBuilder.setView(dialogView);

        title_txt=(TextView)dialogView.findViewById(R.id.title_txt);
        title_txt.setText(title);

        final EditText pick = (EditText) dialogView.findViewById(R.id.pickupKm_Et);
        if(_physics.equals("height")) {
            pick.setText(height);

        }
        if(_physics.equals("weight")) {
            pick.setText(weight);

        }
        pick.setSelection(pick.getText().length());


        Button ok_btn=(Button)dialogView.findViewById(R.id.ok_btn);
        Button cancel_btn=(Button)dialogView.findViewById(R.id.cancel_btn);


        final AlertDialog dlg = dialogBuilder.create();
        Window window = dlg.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        // wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pick.getText().toString().length() != 0) {
                    Map<String, Object> taskMap = new HashMap<String, Object>();
                    if (_physics.equals("height")) {
                        taskMap.put("height", pick.getText().toString());

                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("height", pick.getText().toString());
                        editor.commit();

                        height_tv.setText(pick.getText().toString() + " Cms");

                    }
                    if (_physics.equals("weight")) {
                        taskMap.put("weight", pick.getText().toString());

                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("weight", pick.getText().toString());
                        editor.commit();

                        weight_tv.setText(pick.getText().toString() + " Kg");
                    }

                    dref.child(userId).updateChildren(taskMap);

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                    }
                    dlg.hide();
                }
                else
                {
                    if (_physics.equals("height"))
                        Toast.makeText(MoreActivity.this,"Height is Required.", Toast.LENGTH_LONG).show();
                    else
                    Toast.makeText(MoreActivity.this,"Weight is Required.", Toast.LENGTH_LONG).show();
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
}
