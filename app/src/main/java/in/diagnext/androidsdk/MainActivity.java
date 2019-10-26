package in.diagnext.androidsdk;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import in.diagnext.mylibrary.d_UserLogin;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void pedo_click(View view) {
        Intent intent = new Intent(this, d_UserLogin.class);
        startActivity(intent);

    }
}
