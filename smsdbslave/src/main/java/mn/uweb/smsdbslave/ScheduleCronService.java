package mn.uweb.smsdbslave;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class ScheduleCronService extends IntentService{
    public final Integer CRON_INTERVAL = 20;
    public ScheduleCronService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmManager service = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, CronFetchSMS.class);
        // TODO cancel current or ...
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        service.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                CRON_INTERVAL * 1000,
                pending
        );
    }
}
