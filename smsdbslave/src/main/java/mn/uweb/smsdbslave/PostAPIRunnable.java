package mn.uweb.smsdbslave;

import org.json.JSONObject;

abstract public class PostAPIRunnable implements Runnable {
    public JSONObject json;

    abstract public void run();
}
