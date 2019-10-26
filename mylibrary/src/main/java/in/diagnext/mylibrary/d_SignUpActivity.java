package in.diagnext.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class d_SignUpActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "LoginPrefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diag_activity_sign_up);


        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content), (v, insets) -> {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.bottomMargin = insets.getSystemWindowInsetBottom();
                    return insets.consumeSystemWindowInsets();
                });

        SharedPreferences prefs = getSharedPreferences("pedometer", 0);
        if (prefs.getString("logged", "").toString().equals("userlogged")) {
            Intent intent = new Intent(d_SignUpActivity.this, d_MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void next_Click(View view) {
        Intent intent=new Intent(d_SignUpActivity.this, d_LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
