package mn.uweb.smsdbslave;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

public class ServiceScheduleCron extends IntentService{
    // TODO read from settings
    public static final Integer CRON_INTERVAL = 20;
    public static final String MESSAGE = "mn.uweb.smsdbslave.ServiceScheduleCron.MESSAGE";

    public ServiceScheduleCron() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        AlarmManager service = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, CronFetchSMS.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        if (intent.getStringExtra(MESSAGE).equals("start")) {
            prefs.edit().putLong("last_cron_run", SystemClock.elapsedRealtime()).commit();
            service.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    CRON_INTERVAL * 1000,
                    pi
            );
        }else{
            prefs.edit().putLong("last_cron_run", 0).commit();
            service.cancel(pi);
        }
    }
}
