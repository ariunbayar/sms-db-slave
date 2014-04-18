package mn.uweb.smsdbslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventSystemBoot extends BroadcastReceiver {
    public EventSystemBoot() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent cron_service_intent = new Intent(context, ServiceScheduleCron.class);
        cron_service_intent.putExtra(ServiceScheduleCron.MESSAGE, "start");
        context.startService(cron_service_intent);
    }
}
