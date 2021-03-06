package mn.uweb.smsdbslave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ActivityMain extends ActionBarActivity {

    List<SMS> SMSList = new ArrayList<SMS>();
    Button button_toggle_alarm;
    private Handler cronServiceNotifier;

    private class SMSListAdapter extends ArrayAdapter<SMS> {
        public SMSListAdapter() {
            super(ActivityMain.this, R.layout.sms_list_item, SMSList);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.sms_list_item, parent, false);
                if (view == null) return null;
            }

            SMS cur_sms = SMSList.get(position);

            TextView txt_id = (TextView) view.findViewById(R.id.sms_id);
            txt_id.setText(String.valueOf(cur_sms.getId()));

            TextView txt_orig_id = (TextView) view.findViewById(R.id.sms_orig_id);
            txt_orig_id.setText("#" + String.valueOf(cur_sms.getSMSId()));

            TextView txt_phone = (TextView) view.findViewById(R.id.sms_phone);
            txt_phone.setText(cur_sms.getPhone());

            TextView txt_body = (TextView) view.findViewById(R.id.sms_body);
            txt_body.setText(cur_sms.getBody());

            TextView txt_created_at = (TextView) view.findViewById(R.id.sms_created_at);
            txt_created_at.setText(cur_sms.getCreatedAtDisplay());

            TextView txt_status = (TextView) view.findViewById(R.id.sms_status);
            txt_status.setText(cur_sms.getStatusDisplay());
            txt_status.setTextColor(cur_sms.isSynced() ? Color.GREEN : Color.RED);

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_toggle_alarm = (Button) findViewById(R.id.btnToggleAlarm);

        initList((ListView) findViewById(R.id.listSMS));
        initCronServiceNotifier();
    }

    protected void initCronServiceNotifier(){
        cronServiceNotifier = new Handler () {
            public void handleMessage (Message msg) {
                if (button_toggle_alarm.isEnabled()){
                    updateTextForToggleButton();
                }
            }
        };

        Thread timer = new Thread() {
            public void run () {
                for (;;) {
                    cronServiceNotifier.sendEmptyMessage(0);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.start();
    }

    protected void initList(ListView listSMS){
        ArrayAdapter<SMS> adapter = new SMSListAdapter();
        listSMS.setAdapter(adapter);
        DBHandler dbHandler = new DBHandler(this);
        if (dbHandler.count() > 0) {
            SMSList.addAll(dbHandler.getAll());
        }
    }

    protected boolean isCronServiceStarted(){
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        Long last_cron_run = prefs.getLong("last_cron_run", 0);
        Long seconds_since_last_run = (SystemClock.elapsedRealtime() - last_cron_run) / 1000;
        Boolean is_started =  seconds_since_last_run < ServiceScheduleCron.CRON_INTERVAL * 2;
        return is_started;
    }

    protected void updateTextForToggleButton(){
        if (isCronServiceStarted()) {
            button_toggle_alarm.setText("Running. Tap to Stop periodic task");
        }else{
            button_toggle_alarm.setText("Stopped. Tap to Run periodic task");
        }
    }

    public void toggleAlarm(View view){
        button_toggle_alarm.setEnabled(false);
        Intent cron_service_intent = new Intent(this, ServiceScheduleCron.class);
        String message = (isCronServiceStarted() ? "stop" : "start");
        cron_service_intent.putExtra(ServiceScheduleCron.MESSAGE, message);
        startService(cron_service_intent);

        button_toggle_alarm.setText("Please wait ...");
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextForToggleButton();
                button_toggle_alarm.setEnabled(true);
            }
        }, 500);
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