package mn.uweb.smsdbslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;

public class CronFetchSMS extends BroadcastReceiver{
    private DBHandler dbHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        dbHandler = new DBHandler(context);

        fetchPendingSMS(context);
        sendPendingSMS(context);
        submitSentSMS(context);
        submitReceivedSMS(context);
        removeSyncedSMS(context);

        // Update last run date
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putLong("last_cron_run", SystemClock.elapsedRealtime()).commit();
    }

    protected void fetchPendingSMS(final Context context){
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
                    Boolean has_duplicate = dbHandler.hasSMSId(sms.getSMSId());
                    if (has_duplicate == null) {
                        // there is an error. Try next time. Halt!
                        return;
                    }
                    if (has_duplicate) {
                        Exception e = new Exception("Duplicate fetching! Optimization recommended.");
                        ACRA.getErrorReporter().handleException(e);
                    }else{
                        dbHandler.insert(sms);
                        showToastIfAllowed(context, "pending sms to: " + sms.getPhone());
                    }
                }
            }
        };
        // When there is no pending SMS, it means that all sms are synced to SMSDB and removed.
        // In that case we should rely on last_pending_sms_id in SharedPreferences
        // TODO
        Integer last_id = dbHandler.getLastSMSIdForPending();
        smsdb.sms_pending(last_id);
    }

    protected void sendPendingSMS(Context context) {
        Intent sms_sender_intent = new Intent(context, ServiceSMSSender.class);
        context.startService(sms_sender_intent);
    }

    protected void submitSentSMS(Context context) {

    }

    protected void submitReceivedSMS(final Context context) {
        final SMS received_sms = dbHandler.getFirstSMSByStatus(SMS.STATUS_RECEIVED);

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
                    sms.setSynced(1);
                    sms.setId(received_sms.getId());
                    dbHandler.updateSMS(sms);
                }
            }
        };
        smsdb.sms_received(received_sms);
    }

    protected void showToastIfAllowed(Context context, String msg){
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!prefs.getString("notify", "").equals("yes")) return;
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }

    protected void removeSyncedSMS(Context context){
        // TODO
    }
}