package mn.uweb.smsdbslave;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.acra.ACRA;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

public class APISMSDB {

    private Context context;
    public PostAPIRunnable post_run = null;

    public class RequestTask extends AsyncTask<String, String, String>{
        public String response_text = null;
        public Integer response_code = null;

        @Override
        protected String doInBackground(String... args) {
            String method = args[0];
            String api_url = args[1];
            String api_key = args[2];
            String payload = args[3];

            try {
                URL url_rs = new URL(api_url);
                HttpURLConnection conn = (HttpURLConnection) url_rs.openConnection();
                conn.setRequestProperty("Api-Key", api_key);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                if (method.equals("POST")) {
                    conn.setDoOutput(true);  // sends POST request
                }
                conn.connect();
                if (method.equals("POST")) {
                    OutputStream out = conn.getOutputStream();
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
                    pw.write(payload);
                    pw.close();
                }

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                response_code = conn.getResponseCode();
                response_text = sb.toString();
                in.close();
            } catch (Exception e) {
                ACRA.getErrorReporter().handleException(e);
            }
            return response_text;
        }

        @Override
        protected void onPostExecute(String response_text) {
            super.onPostExecute(response_text);

            JSONObject json = check_response(response_text, response_code);
            if (post_run != null && json != null) {
                post_run.json = json;
                post_run.run();
            }
        }
    }

    public APISMSDB(Context _context) {
        context = _context;
    }

    public JSONObject check_response(String response_text, Integer response_code) {
        if (response_code == null || response_text == null || (response_code != 200 && response_code != 201)) {
            String msg = "API error:\n";
            if (response_code != null) {
                msg += "Code: " + Integer.toString(response_code) + "\n";
            }
            if (response_text != null) {
                msg += "Content:\n" + response_text + "\n";
            }
            ACRA.getErrorReporter().handleException(new Exception(msg));
            showToastIfAllowed("API Error");
            return null;
        }

        try	{
            JSONObject payload = new JSONObject(response_text);
            return payload;
        } catch(JSONException e) {
            ACRA.getErrorReporter().handleException(e);
            return null;
        }
    }

    protected String getSetting(String name) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString(name, "");
    }

    protected void showToast(String msg) {
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }

    protected void showToastIfAllowed(String msg){
        if (!getSetting("notify").equals("yes")) return;
        showToast(msg);
    }

    protected String getApiURLFor(String suffix){
        String api_url = getSetting("api_url");
        if (!api_url.startsWith("http")){
            showToast("API URL seems to have set incorrectly!");
            return null;
        }
        api_url += (api_url.endsWith("/") ? "" : "/") + suffix;
        return api_url;
    }

    public void sms_received(SMS sms) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("phone", sms.getPhone());
            payload.put("body", sms.getBody());
            payload.put("created_at", sms.getCreatedAtDisplay());
        } catch(JSONException e) {
            ACRA.getErrorReporter().handleException(e);
            return;
        }

        String api_url = getApiURLFor("sms_received/");
        if (api_url == null) return;

        new RequestTask().execute(
                "POST",
                api_url,
                getSetting("api_key"),
                payload.toString()
        );
    }

    public void sms_pending(Integer last_id) {
        String api_url = getApiURLFor("pending/");
        if (api_url == null) {
            // What can we do. Just halt.
            return;
        }
        if (last_id != null) {
            api_url += "?last_id=" + String.valueOf(last_id);
        }
        new RequestTask().execute(
                "GET",
                api_url,
                getSetting("api_key"),
                ""
        );
    }

    public void sms_sent(SMS sms) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("id", sms.getSMSId());
            payload.put("sent", sms.getStatus() == SMS.STATUS_SENT ? 1 : 0);
        } catch(JSONException e) {
            ACRA.getErrorReporter().handleException(e);
            return;
        }

        String api_url = getApiURLFor("sent/");
        if (api_url == null) return;

        new RequestTask().execute(
                "POST",
                api_url,
                getSetting("api_key"),
                payload.toString()
        );
    }
}
