package in.diagnext.mylibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import in.diagnext.mylibrary.util.API26Wrapper;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= 26) {
            API26Wrapper.startForegroundService(context, new Intent(context, StepDetector.class));
        } else {
            context.startService(new Intent(context, StepDetector.class));
        }
    }
}
