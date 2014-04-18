package mn.uweb.smsdbslave;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class ServiceSMSSender extends IntentService {
    public ServiceSMSSender() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SMS sms = new DBHandler(this).getFirstPendingSMS();

        Log.i("*********", "next sms: " + (sms != null ? String.valueOf(sms.getId()) : "NULL"));

        // There might not be any pending sms
        if (sms == null) return;

        // TODO validate empty sms body. Sending failed
        String body = (sms.getBody() == null ? " " : sms.getBody());

        String SENT = "mn.uweb.smsdbslave.SENT";
        String DELIVERY = "mn.uweb.smsdbslave.DELIVERY";

        // Prepare intents with attached values
        Intent sentIntent = new Intent(SENT);
        Intent deliveryIntent = new Intent(DELIVERY);
        sentIntent.putExtra("id", sms.getId());
        deliveryIntent.putExtra("id", sms.getId());
        PendingIntent sentPI = PendingIntent.getBroadcast(this, sms.getId(), sentIntent, 0);
        PendingIntent deliveryPI = PendingIntent.getBroadcast(this, sms.getId(), deliveryIntent, 0);

        // send the text message
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+97699437911", null, body, sentPI, deliveryPI);  // TODO use sms.getPhone.
    }
}
