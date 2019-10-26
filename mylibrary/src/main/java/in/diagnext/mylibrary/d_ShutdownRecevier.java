package in.diagnext.mylibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class d_ShutdownRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

            context.startService(new Intent(context, d_StepDetector.class));


    }
}
