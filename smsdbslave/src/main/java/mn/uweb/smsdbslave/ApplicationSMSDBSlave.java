package mn.uweb.smsdbslave;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import android.app.Application;
import android.content.SharedPreferences;

@ReportsCrashes(formKey = "")
public class ApplicationSMSDBSlave extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ACRAConfiguration acra_conf = ACRA.getConfig();
        acra_conf.setFormUri(getSetting("report_url"));
        acra_conf.setFormUriBasicAuthLogin(getSetting("report_username"));
        acra_conf.setFormUriBasicAuthPassword(getSetting("report_password"));
        acra_conf.setAdditionalSharedPreferences(new String[]{"settings"});
        acra_conf.setReportType(Type.JSON);
        acra_conf.setHttpMethod(Method.PUT);
        ACRA.init(this);
    }

    protected String getSetting(String name) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        return prefs.getString(name, "");
    }
}

