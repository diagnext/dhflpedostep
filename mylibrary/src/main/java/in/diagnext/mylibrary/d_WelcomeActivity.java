package in.diagnext.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class d_WelcomeActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT=4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diag_activity_welcome);

        SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        if (prefs.getString("logged", "").equals("userlogged")) {
            Intent intent = new Intent(d_WelcomeActivity.this, d_MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(d_WelcomeActivity.this, d_SignUpActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }

    }
}
