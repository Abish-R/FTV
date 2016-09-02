package com.ftv_fashionshop.helixtech_android;

/**
 * Created by HARSH GOYNKA on 7/8/2016.
 */
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import accounts.AccountManager;
import app.UrlConstants;
import helper.MultipartUtility;
import helper.MyCustomProgressDialog;

//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;


public class AsyncReviewImagePost extends AsyncTask<String, Void, String> {
    private static final String TAG = "Inside Thread HotelAdd";
    ProgressDialog dialog;
    Context context;
    File image_path;
    AccountManager accountManager;
    String invoker="";

    /**Constructor*/
    public AsyncReviewImagePost(Context conx, File img_path) {
        this.context = conx;
        image_path=img_path;
    }

//    public AsyncReviewImagePost(SignUpActivity signUpActivity) {
//        this.context = signUpActivity;
//    }


    /** Loader screen  **/
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new MyCustomProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
    }

    /** Function to handle background operations*/
    @Override
    protected String doInBackground(String... arg0) {
        String result = null,requestURL=null, charset = "UTF-8";
        invoker=arg0[2];
//        if(arg0[0].equals("edit"))
            requestURL= UrlConstants.imageUploadURL;
      /*  else
            //requestURL = "http://phpws.betterthegame.com/BetterTheGame/WS/fileupload";
            requestURL= UrlConstants.imageUploadURL;*/

        /** Pass image and data to multipart*/
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            if(image_path!=null) {
                multipart.addFilePart("picture", image_path);
                multipart.addFormField("customer_id", arg0[0]);
                multipart.addFormField("status",arg0[1]);
            }
//            if(arg0[0].equals("edit")){
//                multipart.addFormField("review_id", arg0[1]);
//                multipart.addFormField("review_title", arg0[2]);
//                multipart.addFormField("review_description", arg0[3]);
//            }else {
//                multipart.addFormField("hotel_id", arg0[1]);
//                multipart.addFormField("uuid", arg0[2]);
//                multipart.addFormField("review_title", arg0[3]);
//                multipart.addFormField("review_description", arg0[4]);
//            }

            /** Response which I received */
            List<String> response = multipart.finish();
            //result=multipart.finish();
            System.out.println("SERVER REPLIED:");
            if(image_path==null)
                result=response.get(response.size()-1);
            else
                result=response.get(0);
//            for (String line : response) {
//                System.out.println(line);
//                //Toast.makeText(context, "txt " + line, Toast.LENGTH_SHORT).show();
//            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return result;
    }

    /**Function execute last to update*/
    @Override
    protected void onPostExecute(String result) {
        // stuff after posting data
        dialog.dismiss();
        if(result==null)
            Toast.makeText(context, "Server Error or Network Error!", Toast.LENGTH_SHORT);
        else {
            int validation = 0;
            JSONObject root = null;
            String message;
            try {
                Log.d("Inside Post", "message");
                root = new JSONObject(result);
                validation = root.getInt("response");
                message = root.getString("message");

                /** After successfull image upload */
                if (validation == 1 && invoker.equals("1")) {
                    //Toast.makeText(context, "done!", Toast.LENGTH_SHORT);
                    saveImage(root.getString("image_url"));
                    Intent intent = new Intent(context, SignInActivity.class);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }else if (validation == 1 && invoker.equals("2")) {
                    Toast.makeText(context, "Image Changed", Toast.LENGTH_SHORT);
                    //((Activity) context).finish();
                }else if (validation == 1 && invoker.equals("3")) {
                    //Toast.makeText(context, "Image Changed", Toast.LENGTH_SHORT);
                    ((AdReview) context).callAlert(1);
                }else if (validation == 1 && invoker.equals("4")) {
                    saveImage(root.getString("image_url"));
                    Toast.makeText(context, "Image Changed", Toast.LENGTH_SHORT);
                }else
                    Toast.makeText(context, "Image Upload Failed", Toast.LENGTH_SHORT);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    public void saveImage(String url){
        AccountManager.saveImageChanged(context,url);
    }

}
