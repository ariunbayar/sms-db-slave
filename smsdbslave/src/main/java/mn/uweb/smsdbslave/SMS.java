package mn.uweb.smsdbslave;

import android.content.Context;

import org.acra.ACRA;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SMS {
    public final int STATUS_TO_SEND = 1;
    public final int STATUS_SENDING = 2;
    public final int STATUS_SENT = 3;
    public final int STATUS_RECEIVED = 4;

    private Integer _id = null;
    private String _phone;
    private String _body;
    private Integer _status;
    private Integer _sms_id = null;
    private Integer _synced;
    private Long _created_at;

    public Integer getId() { return _id; }

    public String getPhone() { return _phone; }

    public String getBody() { return _body; }

    public Integer getStatus() { return _status; }

    public Integer getSMSId() { return _sms_id; }

    public Integer getSynced() { return _synced; }

    public Long getCreatedAt() { return _created_at; }

    public void setId(int id) { _id = id; }

    public void setPhone(String phone) { _phone = phone; }

    public void setBody(String body) { _body = body; }

    public void setStatus(int status) { _status = status; }

    public void setSMSId(int sms_id) { _sms_id = sms_id; }

    public void setSynced(int synced) { _synced = synced; }

    public void setCreatedAt(long created_at) { _created_at = created_at; }

    public boolean populateFromJson(JSONObject json) {
        // Currently only supported for sending sms (`/pending/`)
        try {
            if (json.has("id")) setSMSId(json.getInt("id"));
            if (json.has("phone")) setPhone(json.getString("phone"));
            if (json.has("body")) setBody(json.getString("body"));
            if (json.has("status") && json.getString("status").equals("sending")) {
                setStatus(STATUS_TO_SEND);
            }
            if (json.has("created_at")) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d = parser.parse(json.getString("created_at"));
                setCreatedAt(d.getTime() / 1000);
            }
        }catch (Exception e){
            ACRA.getErrorReporter().handleException(e);
            return false;
        }
        return true;
    }
}