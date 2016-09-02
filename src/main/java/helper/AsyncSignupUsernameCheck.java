package helper;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;

import com.ftv_fashionshop.helixtech_android.SignUpActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import app.Constants;


/**
 * Created by Aman Agrawal on 01/06/2016.
 */
public class AsyncSignupUsernameCheck extends AsyncTask<String, Void, String>  {





    private final SignUpActivity context;
        ProgressDialog progress;

        public AsyncSignupUsernameCheck(SignUpActivity c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            final StringBuilder responseOutput = new StringBuilder();
            try {


                URL url = new URL("http://httest.in/ftvws/RUC_UsernameCheck");


                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user_name", params[0])
                        .appendQueryParameter("designer_id", String.valueOf(Constants.designer_id));



                String query = builder.build().getEncodedQuery();

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(query);
                dStream.flush();
                dStream.close();




                String line = "";

                int response_status = connection.getResponseCode();
                if(response_status==200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        responseOutput.append(line);
                    }
                    br.close();
                }


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return responseOutput.toString();
        }

    protected void onPostExecute(String result) {
        progress.dismiss();
       // Toast.makeText(context,result, Toast.LENGTH_SHORT).show();

        try{
            JSONObject jsonRootObject= new JSONObject(result);
            int response=jsonRootObject.getInt("response");

            context.passResponseToSignUpActivity(response);





        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}

