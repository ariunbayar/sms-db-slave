package mn.uweb.smsdbslave;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        AlarmManager service = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, CronFetchSMS.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        if (intent.getStringExtra(MESSAGE).equals("start")) {
            prefs.edit().putBoolean("cron_scheduled", true).commit();
            service.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    CRON_INTERVAL * 1000,
                    pi
            );
        }else{
            prefs.edit().putBoolean("cron_scheduled", false).commit();
            service.cancel(pi);
        }

    }
}
