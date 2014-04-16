package mn.uweb.smsdbslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.Date;

public class EventSMSReceived extends BroadcastReceiver{
    final SmsManager sms = SmsManager.getDefault();

    private Context context;

    public void onReceive(Context _context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        context = _context;

        final Object[] pdusObj = (Object[]) bundle.get("pdus");

        DBHandler dbHandler = new DBHandler(context);

        for (int i = 0; i < pdusObj.length; i++) {
            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
            String sender = currentMessage.getDisplayOriginatingAddress();
            String message = currentMessage.getDisplayMessageBody();

            SMS sms = new SMS();
            sms.setPhone(sender);
            sms.setBody(message);
            sms.setStatus(SMS.STATUS_RECEIVED);
            sms.setCreatedAt(new Date().getTime() / 1000);
            dbHandler.insert(sms);
            showToastIfAllowed("SMS received from: " + sender);
        }
    }

    protected void showToastIfAllowed(String msg){
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!prefs.getString("notify", "").equals("yes")) return;
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }
}
