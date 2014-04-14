package mn.uweb.smsdbslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventSystemBoot extends BroadcastReceiver {
    public EventSystemBoot() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mServiceIntent = new Intent(context, ScheduleCronService.class);
        context.startService(mServiceIntent);
    }
}
