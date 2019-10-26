package in.diagnext.mylibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class d_OTPActivity extends AppCompatActivity {

    private TextView mobile_tv;
    private Pinview pinview;
    private String otp;
    private TextView message_Tv;
    private ProgressDialog pd;
    private FirebaseDatabase mfdb;
    private String response;
    private DatabaseReference dref;
    private String customerCare;
    private String cashBalance;
    private String height;
    private String weight;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diag_activity_otp);
        mobile_tv=(TextView)findViewById(R.id.mobile_tv);
        message_Tv=(TextView)findViewById(R.id.message_Tv);

        mobile_tv.setText("+91"+getIntent().getStringExtra("mobile").toString());
        otp=getIntent().getStringExtra("otp".toString());

        //   message_Tv.setText(otp);
        sendSMS(otp,mobile_tv.getText().toString());

        mfdb= FirebaseDatabase.getInstance();

        pinview = (Pinview)findViewById(R.id.myPinView);
        pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean b) {
                // Toast.makeText(d_OTPActivity.this,pinview.getValue(),Toast.LENGTH_SHORT).show();
                if(otp.equals(pinview.getValue())) {

                    pd = new ProgressDialog(d_OTPActivity.this);
                    pd.setTitle("Fetching Profile...");
                    pd.setMessage("Please wait.");
                    pd.setCancelable(false);
                    pd.show();

                    mfdb.getReference().child("d_UserMaster").orderByChild("userMobile").equalTo(getIntent().getStringExtra("mobile").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                String name="",userId="",age="",email="",referenceCode="",imageUrl="",memberid="";
                                boolean gender=true;

                                for (DataSnapshot PackageSnapShot : dataSnapshot.getChildren()) {
                                    d_UserMaster pm = PackageSnapShot.getValue(d_UserMaster.class);
                                    userId=pm.getUserId();
                                    name=pm.getUserName();
                                    gender = pm.isGender();
                                    height = pm.height;
                                    weight = pm.weight;

                                }

                                prefs = getSharedPreferences("pedometer", 0);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("logged", "userlogged");
                                editor.putString("userId", userId);
                                editor.putBoolean("gender", gender);
                                editor.putString("height", height);
                                editor.putString("weight", weight);
                                editor.commit();

                                Intent intent=new Intent(d_OTPActivity.this, d_MainActivity.class);
                                startActivity(intent);
                                finish();
                                pd.dismiss();
                            }
                            else {
                                Intent intent = new Intent(d_OTPActivity.this, d_RegistrationActivity.class);
                                intent.putExtra("mobile",getIntent().getStringExtra("mobile").toString());
                                // intent.putExtra("otp",getIntent().getStringExtra("otp").toString());
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
                    message_Tv.setText("Invalid OTP");
                    message_Tv.setOnClickListener(null);
                }

            }
        });
    }



    public void sendSMS(final String otp, final String mobile)
    {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {


                    URLConnection myURLConnection=null;
                    URL myURL=null;
                    BufferedReader reader=null;

                    String encoded_message= URLEncoder.encode("Dear Customer, Your OTP for diagnext steps login is: "+otp+". Do not share this OTP with anyone.");

                    String mainUrl="http://control.msg91.com/api/sendotp.php?";

                    StringBuilder sbPostData = new StringBuilder(mainUrl);
                    sbPostData.append("otp_length=5&");
                    sbPostData.append("authkey=239822AqCsdv8l7SJ85bacc120&");
                    sbPostData.append("message="+encoded_message+"&");
                    sbPostData.append("otp="+otp+"&");
                    sbPostData.append("sender="+getResources().getString(R.string.msg_Name)+"&");
                    sbPostData.append("mobile="+mobile);

                    mainUrl = sbPostData.toString();


                    //prepare connection
                    myURL = new URL(mainUrl);
                    myURLConnection = myURL.openConnection();
                    myURLConnection.connect();
                    reader= new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));

                    //reading response
                    String response;
                    while ((response = reader.readLine()) != null)
                        //print response
                        Log.d("RESPONSE", ""+response);

                    //finally close connection
                    reader.close();


                    //conn.connect();
                }
                catch (Exception e) {

                        message_Tv.setText("Error sending OTP, please try after some time.");


                }
            }
        });

        thread.start();


    }


}
