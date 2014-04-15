package mn.uweb.smsdbslave;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class ScheduleCronService extends IntentService{
    // TODO read from settings
    public static final Integer CRON_INTERVAL = 20;
    public static final String MESSAGE = "mn.uweb.smsdbslave.ScheduleCronService.MESSAGE";

    public ScheduleCronService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmManager service = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("mn.uweb.smsdbslave.CronFetchSMS");
        PendingIntent pending = PendingIntent.getBroadcast(this, 8647, i, PendingIntent.FLAG_CANCEL_CURRENT);
        if (intent.getStringExtra(MESSAGE).equals("start")) {
            Log.i("*******", "starting");
            service.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    CRON_INTERVAL * 1000,
                    pending
            );
        }else{
            Log.i("*******", "stopping");
            service.cancel(pending);
        }

    }
}
