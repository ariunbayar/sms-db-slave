package mn.uweb.smsdbslave;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;

public class EventSMSDelivery extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int result_code = getResultCode();
        int id = intent.getIntExtra("id", 0);
        if (id == 0) return;

        DBHandler dbHandler = new DBHandler(context);
        SMS sms = dbHandler.getSMSByIdAndStatus(id, SMS.STATUS_SENDING);

        // check if something is wrong. Error report must be sent upon db select
        if (sms == null) return;

        if (result_code == Activity.RESULT_OK) {
            sms.setStatus(SMS.STATUS_SENT);
        }else{
            sms.setStatus(SMS.STATUS_SEND_FAIL);
            Exception e = new Exception("Error delivering! Result code: " + String.valueOf(result_code));
            ACRA.getErrorReporter().handleException(e);
        }

        dbHandler.updateSMS(sms);
    }
}