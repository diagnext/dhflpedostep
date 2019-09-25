package in.diagnext.mylibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private Button btnNext;
    private EditText mobile_Et;
    private FirebaseDatabase mfdb;
    private ProgressDialog pd;
    private CheckBox checkBox1;
    private WebView wv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mobile_Et=(EditText)findViewById(R.id.mobile_Et);
        btnNext=(Button)findViewById(R.id.btnNext);
//

        // btnDisable();
        // mobile_Et.addTextChangedListener(textWatcher);


        mfdb= FirebaseDatabase.getInstance();

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
            if (mobile_Et.getText().toString().trim().length()==10)
            {btnEnable();}
            else
            {btnDisable();}
        }
    };

    public void btnEnable()
    {
        btnNext.setEnabled(true);
        btnNext.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.button));
        btnNext.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.white));

    }
    public void btnDisable()
    {
        btnNext.setEnabled(false);
        btnNext.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.grey));
        btnNext.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.white));
    }

    public void login_click(View view) {
        if(Constants.isNetworkAvailable(this)) {
            if (mobile_Et.getText().toString().trim().length()==10)
            {

                    pd = new ProgressDialog(LoginActivity.this);
                    pd.setTitle("Verifying Mobile...");
                    pd.setMessage("Please wait.");
                    pd.setCancelable(false);
                    pd.show();
                    final String _otp = randomNumber();

                    mfdb.getReference().child("OTPMaster").orderByChild("mobile").equalTo(mobile_Et.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String _dpOTPMasterId = "";

                                for (DataSnapshot PackageSnapShot : dataSnapshot.getChildren()) {
                                    OTPMaster pack = PackageSnapShot.getValue(OTPMaster.class);

                                    _dpOTPMasterId = pack.getOTPMasterId().toString();
                                }
                                Map<String, Object> taskMap = new HashMap<String, Object>();
                                taskMap.put("mobile", mobile_Et.getText().toString());
                                taskMap.put("otp", _otp);


                                mfdb.getReference("OTPMaster/" + _dpOTPMasterId).updateChildren(taskMap);

                                Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                                intent.putExtra("mobile", mobile_Et.getText().toString());
                                intent.putExtra("otp", _otp);
                                startActivity(intent);
                                finish();
                                pd.dismiss();
                            } else {
                                String key = mfdb.getReference("OTPMaster").push().getKey();


                                OTPMaster om = new OTPMaster();
                                om.setOTPMasterId(key);
                                om.setMobile(mobile_Et.getText().toString());
                                om.setOtp(_otp);


                                mfdb.getReference("OTPMaster").child(key).setValue(om);

                                Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                                intent.putExtra("mobile", mobile_Et.getText().toString());
                                intent.putExtra("otp", _otp);
                                startActivity(intent);
                                finish();
                                pd.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            }
            else
            {
                Toast.makeText(this,"Enter Valid 10 digit Mobile Number.",Toast.LENGTH_LONG).show();
            }

        }
        else
        {
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_LONG).show();

        }

    }

    public String randomNumber()
    {
        Random r = new Random();
        String randomNumber = String.format("%05d", r.nextInt(10001));
        return randomNumber;
    }



}