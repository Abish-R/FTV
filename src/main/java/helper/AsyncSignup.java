package helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ftv_fashionshop.helixtech_android.SignInActivity;
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


public class AsyncSignup extends AsyncTask<String, Void, String> {

    private final Context context;
    ProgressDialog progress;
    int id;
    SharedPreferences pref;

    public AsyncSignup(Context c) {
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


            URL url = new URL("http://httest.in/ftvws/RUC_CustomerSignup");


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("designer_id", params[0])
                    .appendQueryParameter("first_name", params[1])
                    .appendQueryParameter("last_name", params[2])
                    .appendQueryParameter("user_name", params[3])
                    .appendQueryParameter("password", params[4])
                    .appendQueryParameter("email", params[5])
                    .appendQueryParameter("mobile", params[6])
                    .appendQueryParameter("gender", params[7])
                    .appendQueryParameter("dob", params[8])
                    .appendQueryParameter("street", params[9])
                    .appendQueryParameter("city", params[10])
                    .appendQueryParameter("state", params[11])
                    .appendQueryParameter("country", params[12])
                    .appendQueryParameter("pincode", params[13]);


            String query = builder.build().getEncodedQuery();

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(query);
            dStream.flush();
            dStream.close();


            String line = "";

            int response_status = connection.getResponseCode();
            if (response_status == 200) {
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
        //Toast.makeText(context,result, Toast.LENGTH_SHORT).show();

        try {
            JSONObject jsonRootObject = new JSONObject(result);
            int response = jsonRootObject.getInt("response");
            if(response==2)
            {
                Toast.makeText(context,"Email is already Registered", Toast.LENGTH_SHORT).show();
            }
            if (response == 1) {
            id = jsonRootObject.getInt("customer_id");

            pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("id", id);
            editor.commit();

            int id1 = pref.getInt("id", 0);



           ((SignUpActivity) context).callSignIn(id);

            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
