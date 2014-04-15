package mn.uweb.smsdbslave;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class ActivityMain extends ActionBarActivity {
    Button button_toggle_alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_toggle_alarm = (Button) findViewById(R.id.btnToggleAlarm);
        updateTextForToggleButton();
    }

    protected boolean isCronServiceStarted(){
        Intent i = new Intent("mn.uweb.smsdbslave.CronFetchSMS");
        PendingIntent p = PendingIntent.getBroadcast(this, 8647, i, PendingIntent.FLAG_NO_CREATE);
        return p != null;
    }

    protected void updateTextForToggleButton(){
        if (isCronServiceStarted()) {
            button_toggle_alarm.setText("Running. Click here to Stop periodic task");
        }else{
            button_toggle_alarm.setText("Stopped. Click here to Run periodic task");
        }
    }

    public void toggleAlarm(View view){
        button_toggle_alarm.setEnabled(false);
        Intent cron_service_intent = new Intent(this, ScheduleCronService.class);
        String message = (isCronServiceStarted() ? "stop" : "start");
        cron_service_intent.putExtra(ScheduleCronService.MESSAGE, message);
        startService(cron_service_intent);

        button_toggle_alarm.setText("Please wait ...");
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextForToggleButton();
                button_toggle_alarm.setEnabled(true);
            }
        }, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settings_intent = new Intent(this, ActivitySettings.class);
            startActivity(settings_intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}