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

            // TODO what if there is no Internet connection
            // TODO what if submitted API key was invalid
            // TODO what if API server responded with 500 error
            // TODO what if server returned valid JSON with failure message

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

        if (response_code != 200) {
            // TODO
        }
        if (response_text == null) {
            // TODO throw exception. Submit error to server
            String msg = "No response data.";
            if (response_code != null) {
                msg += " Response code: " + Integer.toString(response_code);
            }
            showToastIfAllowed(msg);
        }else{
            try	{
                JSONObject payload = new JSONObject(response_text);
                return payload;
                // TODO check
            } catch(JSONException e) {
                ACRA.getErrorReporter().handleException(e);
            }
            // TODO
            showToastIfAllowed(response_text);
        }
        return null;
    }

    protected String getSetting(String name) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString(name, "");
    }

    protected void showToastIfAllowed(String msg){
        if (!getSetting("notify").equals("yes")) return;
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }

    public void sms_received(String sms_sender, String sms_body) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("phone", sms_sender);
            payload.put("body", sms_body);
        } catch(JSONException e) {
            ACRA.getErrorReporter().handleException(e);
        }

        new RequestTask().execute(
                "POST",
                getSetting("api_url"),
                getSetting("api_key"),
                payload.toString());
    }

    public void sms_pending() {
        new RequestTask().execute(
                "GET",
                "https://uweb.mn:8081/pending/",
                "4h7NF63AAta6Svq4",
                ""
        );
    }
}
