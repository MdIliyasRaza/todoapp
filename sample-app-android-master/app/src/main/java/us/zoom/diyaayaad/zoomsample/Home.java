package us.zoom.diyaayaad.zoomsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingEvent;
import us.zoom.sdk.MeetingOptions;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitializeListener;
import utility.sqlite.DatabaseHandler;
import utility.sqlite.User;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , MeetingServiceListener, ZoomSDKInitializeListener {
    FloatingActionButton fab=null;

    DatabaseHandler dbh ;

    boolean isSDKUserExist;
    User currentSDKUser;

    String bottomMSG;

    boolean RESTOK=false;

    String USER_ID_FROM_REST,USER_TOKEN_FROM_REST,WEB_DOMAIN_T,APP_KEY_T,APP_SECRET_T,API_KEY_T,API_SECRET_T,USER_EMAIL_T;
     int STYPE =MeetingService.USER_TYPE_ZOOM;
     String DISPLAY_NAME="SDK DEMO";//you can get this from the Rest call also .

    @Override
    protected void onDestroy() {
        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(zoomSDK.isInitialized()) {
            MeetingService meetingService = zoomSDK.getMeetingService();
            meetingService.removeListener(this);
        }

        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        dbh = new DatabaseHandler(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         fab = (FloatingActionButton) findViewById(R.id.fab);

        currentSDKUser = dbh.getUser();
        isSDKUserExist = dbh.isSDKUserExist();




        if (isSDKUserExist) {
            initZoomSDK();
             bottomMSG=currentSDKUser.toString();

            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFBB33"/*"#3399ff"*/)));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        } else{

            bottomMSG="Please set SDK keys and Domain from \'Keys Settings\' before using Demo App.";
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.warning_white));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, bottomMSG, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.join_meeting) {
            joinMeetingDialog();
        } else if (id == R.id.instant_meeting) {
              startInstantMeeting();
        } else if (id == R.id.custom_meeting) {
              startCustomMeetingDialog();
        } else if (id == R.id.keys_settings) {

            showKeysSettingsDialogStep1();

        } else if (id == R.id.nav_share) {

            shareZoom();
        } else if (id == R.id.nav_support) {
            supportMe();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void showKeysSettingsDialogStep2(final String app_key, final String app_secret, final String web_domain){
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.custom_dialog_settings, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        // set title
        alertDialogBuilder.setTitle("SDK Keys - Step 2");
        // set custom dialog icon
        alertDialogBuilder.setIcon(R.drawable.zoom_small);
        // set custom_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);
        final EditText API_KEY = (EditText) dialogView
                .findViewById(R.id.app_key);
        final EditText API_SECRET = (EditText) dialogView
                .findViewById(R.id.app_secret);
        final EditText USER_EMAIL = (EditText) dialogView
                .findViewById(R.id.web_domain);

        final TextView dialogInfo = (TextView) dialogView.findViewById(R.id.dialog_info);
        if(isSDKUserExist){
            API_KEY.setText(currentSDKUser.getAPI_KEY());
            API_SECRET.setText(currentSDKUser.getAPI_SECRET());
            USER_EMAIL.setText(currentSDKUser.getUSER_EMAIL());
            dialogInfo.setText("Your [API_KEY,API_SECRET and USER_EMAIL] have been set before ,change values to edit.");
            //dialogInfo.setTextColor(Color.parseColor("#000000"));
        }else{
            API_KEY.setHint("API KEY");
            API_SECRET.setHint("API SECRET");
            USER_EMAIL.setHint("User Email");
            dialogInfo.setText("Set [API_KEY,API_SECRET and User Email] in order to make the REST call!");
           // dialogInfo.setTextColor(Color.parseColor("#3399ff"));
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                String aPI_KEY, aPI_SECRET, uSER_EMAIL;
                                aPI_KEY = API_KEY.getText().toString().trim();
                                aPI_SECRET = API_SECRET.getText().toString().trim();
                                uSER_EMAIL = USER_EMAIL.getText().toString().trim();
                                if (aPI_KEY.equals("") || aPI_SECRET.equals("") || uSER_EMAIL.equals("")) {
                                    showKeysSettingsDialogStep2(app_key, app_secret, web_domain);
                                } else {
                                    Log.e("****", "1111");
                                    String params = "";
                                    String url = "https://api.zoom.us/v1/user/getbyemail";
                                    try {
                                        params = "api_key=" + URLEncoder.encode(aPI_KEY, "UTF-8") + "&api_secret=" +
                                                URLEncoder.encode(aPI_SECRET, "UTF-8") + "&data_type=JSON&email=" +
                                                URLEncoder.encode(uSER_EMAIL, "UTF-8") + "&login_type=100";
                                    } catch (Exception e) {
                                        Log.e("Exception ", "Encoding Params Exception");
                                    }

                                    new ZoomTask(Home.this).execute(url, params);

                                      WEB_DOMAIN_T=web_domain;
                                      APP_SECRET_T=app_secret;
                                      APP_KEY_T=app_key;
                                      USER_EMAIL_T=uSER_EMAIL;
                                      API_KEY_T=aPI_KEY;
                                      API_SECRET_T=aPI_SECRET;


                                }


                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public void showKeysSettingsDialogStep1(){
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.custom_dialog_settings, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        // set title
        alertDialogBuilder.setTitle("SDK Keys - Step 1");
        // set custom dialog icon
        alertDialogBuilder.setIcon(R.drawable.zoom_small);
        // set custom_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);
        final EditText APP_KEY = (EditText) dialogView
                .findViewById(R.id.app_key);
        final EditText APP_SECRET = (EditText) dialogView
                .findViewById(R.id.app_secret);
        final EditText WEB_DOMAIN = (EditText) dialogView
                .findViewById(R.id.web_domain);

        final TextView dialogInfo = (TextView) dialogView.findViewById(R.id.dialog_info);
        if(isSDKUserExist){
            APP_KEY.setText(currentSDKUser.getAPP_KEY());
            APP_SECRET.setText(currentSDKUser.getAPP_SECRET());
            WEB_DOMAIN.setText(currentSDKUser.getWEB_DOMAIN());
            dialogInfo.setText("Your [APP_KEY,APP_SECRET and WEB_DOMAIN] have been set before ,change values to edit.");
            //dialogInfo.setTextColor(Color.parseColor("#000000"));
        }else{
            APP_KEY.setHint("APP KEY");
            APP_SECRET.setHint("APP SECRET");
            WEB_DOMAIN.setHint("WEB DOMAIN (eg:zoom.us)");
            dialogInfo.setText("Set [APP_KEY,APP_SECRET and WEB_DOMAIN] in order to use Zoom SDK Demo !");
            // dialogInfo.setTextColor(Color.parseColor("#3399ff"));
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Next",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                String key, secret, domain;
                                key = APP_KEY.getText().toString().trim();
                                secret = APP_SECRET.getText().toString().trim();
                                domain = WEB_DOMAIN.getText().toString().trim();

                                if (key.equals("") || secret.equals("") || domain.equals("")) { //dialogInfo.setText("Fill All Blanks,Please !");
                                    Toast.makeText(getApplicationContext(), "Fill All Blanks !", Toast.LENGTH_LONG).show();
                                    showKeysSettingsDialogStep1();
                                } else
                                    showKeysSettingsDialogStep2(key, secret, domain);

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();


    }

  public void saveData(){
      if (RESTOK) {
          Log.e("VALUES TO ADD : ", USER_ID_FROM_REST + "\n" + WEB_DOMAIN_T + "\n" + USER_TOKEN_FROM_REST + "\n" + APP_SECRET_T + "\n" + APP_KEY_T + "\n" + USER_EMAIL_T + "\n" + API_KEY_T + "\n" + API_SECRET_T);
          dbh.addOrEditUser(USER_ID_FROM_REST, WEB_DOMAIN_T, USER_TOKEN_FROM_REST, APP_SECRET_T, APP_KEY_T, USER_EMAIL_T, API_KEY_T, API_SECRET_T);
          dbh = new DatabaseHandler(this);
          currentSDKUser = dbh.getUser();
          Log.e("1:","111");
          isSDKUserExist = dbh.isSDKUserExist();
          Log.e("2:","111");
          bottomMSG = currentSDKUser.toString();
          Log.e("3:", "111");
          fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFBB33")));
          Log.e("4:", "111");
          fab.setImageDrawable(getResources().getDrawable(R.drawable.tick));

      } else {

          Toast.makeText(getApplicationContext(),"Something Wrong ! Check Your Keys",Toast.LENGTH_LONG).show();

      }
      //try to re-initialize zoom sdk again
      initZoomSDK();
  }

    private class ZoomTask extends AsyncTask<String,String,String> {
        private ProgressDialog dialog;
        private Home activity;
        public ZoomTask(Home home) {
            this.activity = home;
            dialog = new ProgressDialog(home);
        }
        @Override
        protected String doInBackground(String... params) {
             String targetURL=params[0];
            String urlParameters=params[1];
            Log.e("Param 0: ",params[0]);
            Log.e("Param 1: ", params[1]);
            URL url;
            HttpURLConnection connection = null;
            try {
                //Create connection
                url = new URL(targetURL);
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                connection.setRequestProperty("Content-Length", "" +
                        Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");

                connection.setUseCaches (false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream ());
                wr.writeBytes (urlParameters);
                wr.flush();
                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                JSONObject jo = new JSONObject(response.toString());
                Log.e("response",jo.toString());
                if(jo.has("id")){
                    Log.e("response",jo.toString());
                    //success
                    RESTOK=true;
                    USER_ID_FROM_REST=jo.getString("id");
                    USER_TOKEN_FROM_REST=jo.getString("token");
                }else{

                    RESTOK=false;
                }
                rd.close();


                //return  false;
            } catch (Exception e) {
                Log.e("EXCEPTION","EEEE");
                e.printStackTrace();
               // return null;

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
           // excutePost(params[0], params[1]);

            return null;

        }



        @Override
        protected void onPreExecute() {
           // dialog = new ProgressDialog(Home);
            dialog.setMessage("Loading Data ... (REST CALL)");
            dialog.show();

        }



        @Override
        protected void onPostExecute(String v) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
       saveData();

        }

    }



 //ZOOM SDK METHODS

    public void initZoomSDK(){

        ZoomSDK sdk = ZoomSDK.getInstance();
        if(!sdk.isInitialized()) {

            sdk.initialize(this, currentSDKUser.getAPP_KEY(), currentSDKUser.getAPP_SECRET(), currentSDKUser.getWEB_DOMAIN(), this);
            //set your own keys for dropbox , oneDrive and googleDrive
            sdk.setDropBoxAppKeyPair(this, null/*DROPBOX_APP_KEY*/, null/*DROPBOX_APP_SECRET*/);
            sdk.setOneDriveClientId(this, null/*ONEDRIVE_CLIENT_ID*/);
            sdk.setGoogleDriveClientId(this,null /*GOOGLE_DRIVE_CLIENT_ID*/);
        }
        else registerMeetingServiceListener();
    }


    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {

        Log.i("INITIALIZATION: ", "onZoomSDKInitializeResult, errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);

        if(errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(getApplicationContext(), "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode, Toast.LENGTH_LONG);
        } else {
            Toast.makeText(getApplicationContext(), "Initialize Zoom SDK successfully.", Toast.LENGTH_LONG).show();

            registerMeetingServiceListener();
        }
    }

 @Override
 public void onMeetingEvent(int meetingEvent, int errorCode,
                            int internalErrorCode) {

     if(meetingEvent == MeetingEvent.MEETING_CONNECT_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
         Toast.makeText(getApplicationContext(), "Version of ZoomSDK is too low!", Toast.LENGTH_LONG).show();
     }

     if(meetingEvent == MeetingEvent.MEETING_DISCONNECTED || meetingEvent == MeetingEvent.MEETING_CONNECT_FAILED) {
         Toast.makeText(getApplicationContext(),"MEETING ENDED",Toast.LENGTH_LONG).show();

     }
    }

    private void registerMeetingServiceListener() {
        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        MeetingService meetingService = zoomSDK.getMeetingService();
        if(meetingService != null) {
            meetingService.addListener(this);
        }
    }

    public void startInstantMeeting() {
    if(!dbh.isSDKUserExist()){

        Toast.makeText(getApplicationContext(),"Set SDK Keys First",Toast.LENGTH_LONG).show();
        return;
    }
        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG).show();
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();

        MeetingOptions opts = new MeetingOptions();
//        opts.no_driving_mode = true;
//		  opts.no_meeting_end_message = true;
//        opts.no_titlebar = true;
//        opts.no_bottom_toolbar = true;
//        opts.no_invite = true;
        dbh = new DatabaseHandler(getApplicationContext());
        currentSDKUser=dbh.getUser();
        User usr=currentSDKUser;
        int ret = meetingService.startInstantMeeting(this, usr.getUSER_ID(), usr.getZOOM_TOKEN(), STYPE, DISPLAY_NAME, opts);

        Log.i("Instant:", "Start Instant Meeting, ret=" + ret);
    }

    public  void startCustomMeetingDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //_________

        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.input_meeting_number, null);

        // set title
        builder.setTitle("Start Scheduled Meeting");
        // set custom dialog icon
        builder.setIcon(R.drawable.zoom_small);
        // set custom_dialog to alertdialog builder
        builder.setView(dialogView);
        final EditText meetingNumber = (EditText) dialogView
                .findViewById(R.id.meetingNumber);

        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startCustomMeeting(meetingNumber.getText().toString().trim());

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setBackgroundColor(Color.parseColor("#FFFFBB33"));
        nbutton.setTextColor(Color.parseColor("#FFFFFF"));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setBackgroundColor(Color.parseColor("#62A5F6"));
        pbutton.setTextColor(Color.parseColor("#FFFFFF"));


    }
    public  void startCustomMeeting(String meetingNo){

        if(meetingNo.length() == 0) {
            Toast.makeText(this, "You need to enter a scheduled meeting number.", Toast.LENGTH_LONG).show();
            return;
        }

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG).show();
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();

        MeetingOptions opts = new MeetingOptions();
//		opts.no_driving_mode = true;
//		opts.no_invite = true;
//		opts.no_meeting_end_message = true;
//		opts.no_titlebar = true;
//		opts.no_bottom_toolbar = true;
//		opts.no_dial_in_via_phone = true;
//		opts.no_dial_out_to_phone = true;
//		opts.no_disconnect_audio = true;
        dbh = new DatabaseHandler(getApplicationContext());
        currentSDKUser=dbh.getUser();
        int ret = meetingService.startMeeting(this, currentSDKUser.getUSER_ID(), currentSDKUser.getZOOM_TOKEN(), STYPE, meetingNo, DISPLAY_NAME, opts);
        Log.i("Custom: ", "onClickBtnStartMeeting, ret=" + ret);

    }
//JOIN MEETING

    public  void joinMeetingDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //_________

        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.join_meeting_dialog, null);

        // set title
        builder.setTitle("Start Scheduled Meeting");
        // set custom dialog icon
        builder.setIcon(R.drawable.zoom_small);
        // set custom_dialog to alertdialog builder
        builder.setView(dialogView);
        final EditText meetingNumber = (EditText) dialogView
                .findViewById(R.id.meetingID);
        final EditText meetingPassword = (EditText) dialogView
                .findViewById(R.id.meetingPass);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("JOIN", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                joinMeeting(meetingNumber.getText().toString().trim(), meetingPassword.getText().toString().trim());

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setBackgroundColor(Color.parseColor("#FFFFBB33"));
        nbutton.setTextColor(Color.parseColor("#FFFFFF"));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setBackgroundColor(Color.parseColor("#62A5F6"));
        pbutton.setTextColor(Color.parseColor("#FFFFFF"));


    }
    public  void joinMeeting(String meetingNo,String password){

        if(meetingNo.length() == 0) {
            Toast.makeText(this, "You need to enter a scheduled meeting number.", Toast.LENGTH_LONG).show();
            return;
        }

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG).show();
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();

        MeetingOptions opts = new MeetingOptions();
//		opts.no_driving_mode = true;
//		opts.no_invite = true;
//		opts.no_meeting_end_message = true;
//		opts.no_titlebar = true;
//		opts.no_bottom_toolbar = true;
//		opts.no_dial_in_via_phone = true;
//		opts.no_dial_out_to_phone = true;
//		opts.no_disconnect_audio = true;

        int ret = meetingService.joinMeeting(this, meetingNo, DISPLAY_NAME, password, opts);
        Log.i("JOIN : ", "JOIN Meeting, ret=" + ret);

    }


    //SHARE ZOOM  METHOD

    public void shareZoom(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT," I am using ZOOM CLOUD MEETING \nCheck it out\n@ " +
                "http://zoom.us/pricing \nZOOM VIDEO COMMUNICATIONS TEAM.\n");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }
    //GO TO SUPPORT
    public  void supportMe(){
        String url = "https://support.zoom.us/hc/en-us";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }
}



