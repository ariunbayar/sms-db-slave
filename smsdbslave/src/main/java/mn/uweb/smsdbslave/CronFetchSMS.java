package mn.uweb.smsdbslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;
import org.json.JSONException;

public class CronFetchSMS extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO fetch from /pending/ and save to DB
        // TODO fetch by passing last pending sms id
//        APISMSDB smsdb = new APISMSDB(context);
//        smsdb.post_run = new PostAPIRunnable(){
//            @Override
//            public void run() {
//                try {
//                    Log.i("pending", "id: " + String.valueOf(this.json.getInt("id")));
//                    Log.i("pending", "phone: " + this.json.getString("phone"));
//                    Log.i("pending", "body: " + this.json.getString("body"));
//                    Log.i("pending", "status: " + this.json.getString("status"));
//                    Log.i("pending", "created_at: " + this.json.getString("created_at"));
//                }catch (JSONException e) {
//                    ACRA.getErrorReporter().handleException(e);
//                }
//            }
//        };
//        smsdb.sms_pending();


        String msg = "hello: " + String.valueOf(SystemClock.elapsedRealtime());
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        Log.i("smsdbslave", msg);
    }
}
