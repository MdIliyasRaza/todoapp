package utility.sqlite;

/**
 * Created by diyaayaad on 4/2/16.
 */
public class User {

    String ZOOM_TOKEN,USER_ID,WEB_DOMAIN, APP_SECRET, APP_KEY,USER_EMAIL,API_KEY,API_SECRET;

    public String getAPI_KEY() {
        return API_KEY;
    }

    public void setAPI_KEY(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public String getAPI_SECRET() {
        return API_SECRET;
    }

    public void setAPI_SECRET(String API_SECRET) {
        this.API_SECRET = API_SECRET;
    }

    public String getUSER_EMAIL() {
        return USER_EMAIL;
    }

    public void setUSER_EMAIL(String USER_EMAIL) {
        this.USER_EMAIL = USER_EMAIL;
    }

    public String getZOOM_TOKEN() {
        return ZOOM_TOKEN;
    }

    @Override
    public String toString() {
        return "Used domain:"+WEB_DOMAIN+", to edit Domain and App keys go to keys settings.";
    }

    public void setZOOM_TOKEN(String ZOOM_TOKEN) {
        this.ZOOM_TOKEN = ZOOM_TOKEN;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getWEB_DOMAIN() {
        return WEB_DOMAIN;
    }

    public void setWEB_DOMAIN(String WEB_DOMAIN) {
        this.WEB_DOMAIN = WEB_DOMAIN;
    }

    public String getAPP_KEY() {
        return APP_KEY;
    }

    public void setAPP_KEY(String APP_KEY) {
        this.APP_KEY = APP_KEY;
    }

    public String getAPP_SECRET() {
        return APP_SECRET;
    }

    public void setAPP_SECRET(String APP_SECRET) {
        this.APP_SECRET = APP_SECRET;
    }
}
