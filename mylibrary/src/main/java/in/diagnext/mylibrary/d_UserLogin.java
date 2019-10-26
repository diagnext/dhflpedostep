package in.diagnext.mylibrary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class d_UserLogin extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();

    private EditText d_userName_Et;
    private EditText d_password_Et;
    private Button btnNext;
    private SharedPreferences prefs;
    private ProgressDialog pd;
    private boolean gender;
    private TextView txt_msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diag_activity_userlogin);


        d_userName_Et=(EditText)findViewById(R.id.d_userName_Et);
        d_password_Et=(EditText)findViewById(R.id.d_password_Et);
        txt_msg=(TextView)findViewById(R.id.txt_msg);
        btnNext=(Button)findViewById(R.id.d_btnNext);
       // btnDisable();

       // d_userName_Et.addTextChangedListener(textWatcher);
       // d_password_Et.addTextChangedListener(textWatcher);

        SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        if (prefs.getString("logged", "").equals("userlogged")) {
            Intent intent = new Intent(d_UserLogin.this, d_MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void login_click(View view) {
        if (d_Constants.isNetworkAvailable(this)) {

            pd = new ProgressDialog(d_UserLogin.this);
            pd.setTitle("Verifying User...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.show();

            if (d_userName_Et.getText().toString().trim().length()==0 || d_password_Et.getText().toString().trim().length()==0)
            {
                txt_msg.setVisibility(View.VISIBLE);
                pd.dismiss();
            }
            else {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                try {
                    sendPost();
                    pd.dismiss();
                } catch (Exception e) {

                    d_UserLogin.this.runOnUiThread(new Runnable() {
                        public void run() {
                            txt_msg.setVisibility(View.VISIBLE);
                            txt_msg.setText("Something went wrong, Please try again.");
                            pd.dismiss();
                        }
                    });


                }
                    }
                });

                thread.start();


            }
        }
        else
        {
        Toast.makeText(this,"No Internet Connection",Toast.LENGTH_LONG).show();

         }
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
            if (d_userName_Et.getText().toString().trim().length()==0 || d_password_Et.getText().toString().trim().length()==0)
            {btnDisable();}
            else
            {btnEnable();}
        }
    };

    public void btnEnable()
    {
        btnNext.setEnabled(true);
        btnNext.setBackgroundColor(ContextCompat.getColor(d_UserLogin.this, R.color.button));
        btnNext.setTextColor(ContextCompat.getColor(d_UserLogin.this, R.color.white));

    }
    public void btnDisable()
    {
        btnNext.setEnabled(false);
        btnNext.setBackgroundColor(ContextCompat.getColor(d_UserLogin.this, R.color.grey));
        btnNext.setTextColor(ContextCompat.getColor(d_UserLogin.this, R.color.white));
    }

    private void sendPost() throws Exception {

        String uName= d_userName_Et.getText().toString();
        String pWord = d_password_Et.getText().toString();
        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("member_id", uName)
                .add("password", pWord)
                .build();

        Request request = new Request.Builder()
                .url("https://devapi.dhflgi.com/health/api/pedometer/member/login")
                .addHeader("username", "cococure_partner")
                .addHeader("password", "JWGnYDJw7k4YEnxy")
                .post(formBody)
                .build();

       // httpClient.newBuilder().connectTimeout(5, TimeUnit.MINUTES);
        d_Data data;
        String message,code;
        try (ResponseBody response = httpClient.newCall(request).execute().body()) {

            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String results = response.string();
            results= results.replace(",\"data\":\"No member found\"","");

            Gson gson = new Gson();
            d_loginEntity entity = gson.fromJson(results, d_loginEntity.class);

            message=entity.getMessage();
            data= entity.getData();
            code=entity.getCode();

        }

        if(code.equals("1")) {
            d_UserLogin.this.runOnUiThread(new Runnable() {
                public void run() {
            txt_msg.setVisibility(View.GONE);
                }
            });

            gender = true;
            prefs = getSharedPreferences("pedometer", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("logged", "userlogged");
            editor.putString("userId", data.getMember_id());
            editor.putBoolean("gender", gender);
            editor.putString("height", "175");
            editor.putString("weight", "78");
            editor.commit();

            Intent intent = new Intent(d_UserLogin.this, d_MainActivity.class);
            startActivity(intent);
            finish();

        }
        else
        {
            d_UserLogin.this.runOnUiThread(new Runnable() {
                public void run() {
                    txt_msg.setVisibility(View.VISIBLE);
                    txt_msg.setText("Invalid member id or password");
                    pd.dismiss();
                }
            });


        }
    }

}
