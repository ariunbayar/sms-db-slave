package mn.uweb.smsdbslave;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.acra.ACRA;

import java.util.prefs.Preferences;


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
        Context ctx = getApplicationContext();
        if (ctx == null) {
            Exception e = new Exception("Probably impossible. Application context was nonexistent!");
            ACRA.getErrorReporter().handleException(e);
            return;
        }
        Toast t = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
        t.show();
    }

    private String _getTextSafe(EditText editText) {
        Editable e = editText.getText();
        return e == null ? "" : e.toString();
    }

    public void saveSettings(View view) {
        // TODO already loaded ACRA settings must affect
        // TODO encrypt these data
        // TODO include cron interval settings
        SharedPreferences.Editor e = prefs.edit();
        e.putString("api_url", _getTextSafe(text_api_url));
        e.putString("api_key", _getTextSafe(text_api_key));
        e.putString("notify", check_notify.isChecked() ? "yes" : "no");
        e.putString("report_url", _getTextSafe(text_report_url));
        e.putString("report_username", _getTextSafe(text_report_username));
        e.putString("report_password", _getTextSafe(text_report_password));
        e.commit();

        showToast("Settings Saved!");
    }
}
