package mn.uweb.smsdbslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;
import org.json.JSONException;

public class CronFetchSMS extends BroadcastReceiver{
    @Override
    public void onReceive(final Context context, Intent intent) {
        final DBHandler dbhandler = new DBHandler(context);
        APISMSDB smsdb = new APISMSDB(context);
        smsdb.post_run = new PostAPIRunnable(){
            @Override
            public void run() {
                // TODO ask API to differentiate errors from info
                if (this.json.has("errors")){
                    // return code must be 200
                    try {
                        String msg = this.json.getJSONObject("errors").getString("id");
                        showToastIfAllowed(context, msg);
                    }catch(Exception e){
                        ACRA.getErrorReporter().handleException(e);
                    }
                    return;
                }
                SMS sms = new SMS();
                if (sms.populateFromJson(this.json)) {
                    Boolean has_duplicate = dbhandler.has(sms.getSMSId());
                    if (has_duplicate == null) {
                        // there is an error. Try next time. Halt!
                        return;
                    }
                    if (has_duplicate) {
                        Exception e = new Exception("Duplicate fetching! Optimization recommended.");
                        ACRA.getErrorReporter().handleException(e);
                    }else{
                        dbhandler.insert(sms);
                        showToastIfAllowed(context, "pending sms to: " + sms.getPhone());
                    }
                }
            }
        };
        Integer last_id = dbhandler.getLastPendingSMSId();
        smsdb.sms_pending(last_id);
    }

    protected void showToastIfAllowed(Context context, String msg){
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!prefs.getString("notify", "").equals("yes")) return;
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }
}