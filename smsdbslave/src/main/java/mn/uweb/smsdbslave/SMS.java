package mn.uweb.smsdbslave;

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
    private Date _created_at;

    public Integer getId() { return _id; }

    public String getPhone() { return _phone; }

    public String getBody() { return _body; }

    public Integer getStatus() { return _status; }

    public Integer getSMSId() { return _sms_id; }

    public Integer getSynced() { return _synced; }

    public Date getCreatedAt() { return _created_at; }

    public void setId(int _id) { this._id = _id; }

    public void setPhone(String _phone) { this._phone = _phone; }

    public void setBody(String _body) { this._body = _body; }

    public void setStatus(int _status) { this._status = _status; }

    public void setSMSId(int _sms_id) { this._sms_id = _sms_id; }

    public void setSynced(int _synced) { this._synced = _synced; }

    public void setCreatedAt(int _synced) { this._synced = _synced; }
}