package com.google.androidgms;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Shubham on 30 Nov 14.
 */
public class SendData extends AsyncTask<Void,Void,String>
{
    private String messageArr[];
    private String imei;

    private String url = "http://192.168.49.240:3000";

    public SendData(String[] str, String n)
    {
        this.messageArr=str;
        this.imei=n;
        Log.d("imei",imei);
        for(int i=0;i<30;++i)
        {
            Log.d("String", i + ": " + messageArr[i]);
        }

    }

    @Override
    protected String doInBackground(Void... params)
    {
        InputStream inputStream = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("email", userEmail);
                jsonObject.put("password", userPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            json = jsonObject.toString();
            Log.d("json", json);

            StringEntity se = new StringEntity(json);
            httpPost.setHeader("Accept","application/json");
            httpPost.setHeader("Content-type","application/json");
            httpPost.setEntity(se);

            org.apache.http.HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
            StatusLine sl = httpResponse.getStatusLine();

            Log.v("debug", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase());

            try {
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
        return null;
    }
}
