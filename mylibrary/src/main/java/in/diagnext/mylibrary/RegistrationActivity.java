package in.diagnext.mylibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class RegistrationActivity extends AppCompatActivity {
    int gender=0;
    private Button btnMale;
    private Button btnFeMale;
    private Button btnSubmit;
    private EditText name_Et;
    private ProgressDialog pd;
    private FirebaseDatabase mfdb;
    private String mobile;
    private EditText height_Et;
    private EditText weight_Et;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setTitle("Enter your details");


        mobile=getIntent().getStringExtra("mobile");
        btnMale=(Button)findViewById(R.id.btnMale);
        btnFeMale=(Button)findViewById(R.id.btnFemale);
        btnSubmit=(Button)findViewById(R.id.btnSubmit);

        name_Et=(EditText)findViewById(R.id.name_Et);
        height_Et=(EditText)findViewById(R.id.height_Et);
        weight_Et=(EditText)findViewById(R.id.weight_Et);


        name_Et.addTextChangedListener(textWatcher);
        height_Et.addTextChangedListener(textWatcher);
        weight_Et.addTextChangedListener(textWatcher);

        mfdb= FirebaseDatabase.getInstance();

        btnDisable();


    }

    TextWatcher textWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (gender==0 || name_Et.getText().toString().trim().length()==0 || height_Et.getText().toString().trim().length()==0 || weight_Et.getText().toString().trim().length()==0)
            {btnDisable();}
            else
            {btnEnable();}
        }
    };

    public void submit_Click(View view) {

        pd = new ProgressDialog(RegistrationActivity.this);
        pd.setTitle("Registering User...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.show();


            insertUser();


    }



    public void insertUser()
    {
        btnDisable();

        Boolean _gender = false;
        if (gender == 1)
            _gender = true;


        Bitmap bitmap = BitmapFactory.decodeResource(RegistrationActivity.this.getResources(),
                R.drawable.avatar);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        String key = mfdb.getReference("UserMaster").push().getKey();

        UserMaster pm = new UserMaster();
        pm.setUserId(key);
        pm.setUserName(name_Et.getText().toString());
        pm.setUserMobile(mobile);
        pm.setGender(_gender);
        pm.setHeight(height_Et.getText().toString());
        pm.setWeight(weight_Et.getText().toString());


        mfdb.getReference("UserMaster").child(key).setValue(pm);


        prefs = getSharedPreferences("pedometer", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logged", "userlogged");
        editor.putString("userId", key);
        editor.putBoolean("gender", _gender);
        editor.putString("height", height_Et.getText().toString());
        editor.putString("weight", weight_Et.getText().toString());
        editor.commit();

        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        pd.dismiss();
    }

    public void male_Click(View view) {
        gender=1;

        btnMale.setBackground(getResources().getDrawable(R.drawable.color_background));
        btnMale.setCompoundDrawablesWithIntrinsicBounds( R.drawable.male_w, 0, 0, 0);
        btnMale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.white));

        btnFeMale.setBackground(getResources().getDrawable(R.drawable.gender_background));
        btnFeMale.setCompoundDrawablesWithIntrinsicBounds( R.drawable.female, 0, 0, 0);
        btnFeMale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.black));

        if (gender==0 || name_Et.getText().toString().trim().length()==0 || height_Et.getText().toString().trim().length()==0 || weight_Et.getText().toString().trim().length()==0)
        {btnDisable();}
        else
        {btnEnable();}
    }

    public void female_Click(View view) {
        gender=2;

        btnFeMale.setBackground(getResources().getDrawable(R.drawable.color_background));
        btnFeMale.setCompoundDrawablesWithIntrinsicBounds( R.drawable.female_w, 0, 0, 0);
        btnFeMale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.white));

        btnMale.setBackground(getResources().getDrawable(R.drawable.gender_background));
        btnMale.setCompoundDrawablesWithIntrinsicBounds( R.drawable.male, 0, 0, 0);
        btnMale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.black));

        if (gender==0 || name_Et.getText().toString().trim().length()==0 || height_Et.getText().toString().trim().length()==0 || weight_Et.getText().toString().trim().length()==0)
        {btnDisable();}
        else
        {btnEnable();}
    }


    public void btnEnable()
    {
        btnSubmit.setEnabled(true);
        btnSubmit.setBackgroundColor(ContextCompat.getColor(RegistrationActivity.this, R.color.button));
        btnSubmit.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.white));

    }
    public void btnDisable()
    {
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundColor(ContextCompat.getColor(RegistrationActivity.this, R.color.grey));
        btnSubmit.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.white));
    }

    public String randomNumber()
    {
        Random r = new Random();
        String randomNumber = String.format("%05d", r.nextInt(10001));
        return randomNumber;
    }


}

