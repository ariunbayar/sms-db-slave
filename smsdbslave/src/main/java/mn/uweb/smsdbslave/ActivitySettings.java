package mn.uweb.smsdbslave;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


public class ActivitySettings extends ActionBarActivity {
    private SharedPreferences prefs;
    private EditText text_api_url;
    private EditText text_api_key;
    private EditText text_report_url;
    private EditText text_report_username;
    private EditText text_report_password;
    private CheckBox check_notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        text_api_url = (EditText) findViewById(R.id.api_url);
        text_api_key = (EditText) findViewById(R.id.api_key);
        check_notify = (CheckBox) findViewById(R.id.notify);
        text_report_url = (EditText) findViewById(R.id.report_url);
        text_report_username = (EditText) findViewById(R.id.report_username);
        text_report_password = (EditText) findViewById(R.id.report_password);

        // fill from saved settings
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        text_api_url.setText(prefs.getString("api_url", ""));
        text_api_key.setText(prefs.getString("api_key", ""));
        check_notify.setChecked(prefs.getString("notify", "").equals("yes"));
        text_report_url.setText(prefs.getString("report_url", ""));
        text_report_username.setText(prefs.getString("report_username", ""));
        text_report_password.setText(prefs.getString("report_password", ""));
    }

    protected void showToast(String msg){
        Toast t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        t.show();
    }

    public void saveSettings(View view) {
        // TODO already loaded ACRA settings must affect
        // TODO make sure the cron tasks affect
        prefs
                .edit()
                .putString("api_url", text_api_url.getText().toString())
                .putString("api_key", text_api_key.getText().toString())
                .putString("notify", check_notify.isChecked() ? "yes" : "no")
                .putString("report_url", text_report_url.getText().toString())
                .putString("report_username", text_report_username.getText().toString())
                .putString("report_password", text_report_password.getText().toString())
                .commit();

        showToast("Settings Saved!");
    }
}
