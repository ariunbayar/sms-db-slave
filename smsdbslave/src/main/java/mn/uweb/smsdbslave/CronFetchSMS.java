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
        cleanupSyncedOrErroneousSMS(context);

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
        Integer last_id = dbHandler.getLastSMSIdForPending();
        smsdb.sms_pending(last_id);
    }

    protected void sendPendingSMS(Context context) {
        Intent sms_sender_intent = new Intent(context, ServiceSMSSender.class);
        context.startService(sms_sender_intent);
    }

    protected void submitSentSMS(final Context context) {
        final SMS sent_sms = dbHandler.getFirstSMSByStatus(SMS.STATUS_SENT, SMS.STATUS_SEND_FAIL);

        if (sent_sms == null) return;

        APISMSDB smsdb = new APISMSDB(context);
        smsdb.post_run = new PostAPIRunnable(){
            @Override
            public void run() {
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
                    sms.setId(sent_sms.getId());
                    dbHandler.updateSMS(sms);
                }
            }
        };
        smsdb.sms_sent(sent_sms);
    }

    protected void submitReceivedSMS(final Context context) {
        final SMS received_sms = dbHandler.getFirstSMSByStatus(SMS.STATUS_RECEIVED);

        if (received_sms == null) return;

        APISMSDB smsdb = new APISMSDB(context);
        smsdb.post_run = new PostAPIRunnable(){
            @Override
            public void run() {
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

    protected void cleanupSyncedOrErroneousSMS(Context context){
        dbHandler.deleteSyncedSMS();
        dbHandler.updateExpiredSendingSMS();
    }
}