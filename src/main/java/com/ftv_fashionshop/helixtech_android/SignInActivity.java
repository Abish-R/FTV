package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import accounts.AccountManager;
import accounts.Permissions;
import accounts.SocialProfileClasses;
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;
import helper.PermissionsInterface;


public class SignInActivity extends AppCompatActivity implements AccountManager.SignInCallback, PermissionsInterface {

    private static final int CONTACTS_PERMISSIONS = 12345;
    AccountManager accountManager;
    ProgressDialog dialog;
    int customer_id;
    String username = null;
    EditText usernameEditText;
    EditText passEditText;

    String backendPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredNoActionBarTheme(this));
        super.onCreate(savedInstanceState);
        accountManager = new AccountManager(this);
        customer_id = accountManager.getId(this);
        setContentView(R.layout.activity_login);


        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        accountManager.setSignInCallback(this);

        TextView signUpText = (TextView) findViewById(R.id.signUpText);
        signUpText.setPaintFlags(signUpText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Linkify.addLinks(signUpText, Linkify.ALL);

        TextView forgot_password_text = (TextView) findViewById(R.id.forgot_password);
        forgot_password_text.setPaintFlags(forgot_password_text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Linkify.addLinks(forgot_password_text, Linkify.ALL);

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passEditText = (EditText) findViewById(R.id.passwordEditText);
        Button login = (Button) findViewById(R.id.BLogin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             //   Permissions p = new Permissions();
               // p.setListener(SignInActivity.this);
                //int a = p.askForContactPermission(SignInActivity.this);
           //     if(a==1) {
                    final String username = usernameEditText.getText().toString();
                    final String password = passEditText.getText().toString();
                    if (isValid(username, password)) {
                        accountManager.normalSignIn(username, password);
                    }
                }
          //  }
        });


        signUpText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
            }
        });
askForContactsPermissions();

    }
    private void askForContactsPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setMessage("Contacts access Permissions are Required For proper functioning of the app.");
            dialog.setPositiveButton("Ok",null);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS}, CONTACTS_PERMISSIONS);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                askForContactsPermissions();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, LoginControllerActivity.class);
        startActivity(intent);
        finish();
    }


    public void webServiceCall(View view){
        //view.setBackgroundColor(getResources(getColor(R.color.colorPrimary)));

        username = usernameEditText.getText().toString();
        if(username.length()< 4) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
            //    alertDialog.setTitle("Warning");
            alertDialog.setMessage("Please enter your username in username field!");

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        }

        else
        {
            dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.forgotPasswordURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    dialog.hide();
                    JSONObject jsonObject = new JSONObject(response);
                    backendPassword= jsonObject.getString("message");
                    Intent intent2 = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                    intent2.putExtra("message",backendPassword);
                    if (jsonObject.getInt("response") == 1) {

                        Toast.makeText(SignInActivity.this, "New password has been sent to your registered mail", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                        intent.putExtra("user_name", username );
                        startActivity(intent);

                    }else
                    {
                        Toast.makeText(SignInActivity.this, "Enter a valid Username", Toast.LENGTH_SHORT).show();}
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SignInActivity.this, "Username not verified", Toast.LENGTH_SHORT).show();
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                // map.put("customer_id", String.valueOf(customer_id));
                map.put("user_name",username);
                map.put("designer_id",String.valueOf(Constants.designer_id));
                return map;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }}

    private boolean isValid(String username, String password) {

        int count = 0;
        if (username.length() > 5 && username.contains("@") && username != null) {
            for (int i = 0; i < username.length(); i++) {
                if (username.charAt(i) == ('@'))
                    count++;
            }
        }
        if (!username.isEmpty() && !password.isEmpty()) {
            if (username.charAt(0) == '.' || username.charAt(0) == '@' || username.charAt(0) == '-' || username.charAt(0) == '_' ||
                    username.length() < 3 || username.contains("..") || username.contains("--") || username.contains("__") || count > 1)

                signInError(AccountManager.Error.USERNAME_PASSWORD_MISMATCH);
            else if (password.contains(" ") || password.length() < 3)
                signInError(AccountManager.Error.USERNAME_PASSWORD_MISMATCH);
            else {
                return true;
            }
        } else
            Toast.makeText(SignInActivity.this, "None of the fields can be left empty", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void sendingDataToServer() {

    }

    @Override
    public void signedInSuccessfully() {
        dialog.hide();
        checkSignIn();
    }


    private void checkSignIn() {

        if(accountManager.getCurrentSignIn(this)== SocialProfileClasses.SocialAccount.NOT_SIGNED_IN){
            return;
        }
        else{
            if(accountManager.getCity().isEmpty()){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Designer's contact has been added to your contact list");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent act2 = new Intent(SignInActivity.this, AndroidGPSTrackingActivity.class);
                        act2.putExtra("calling-activity", Constants.ACTIVITY_2);
                        // or ActivityConstants.ACTIVITY_3 if called form Activity3
                        startActivity(act2);
                        finish();
                    }
                });
                alertDialog.show();
            }
            else{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Designer's contact has been added to your contact list");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
                alertDialog.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public void signInError(int error) {
        dialog.hide();
        if (error == AccountManager.Error.NO_INTERNET_CONNECTION) {
            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.UNKNOWN_ERROR) {
            Toast.makeText(this, "Unknown Error Occurred! Please Retry", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.USERNAME_PASSWORD_MISMATCH) {
            Toast.makeText(this, "Please check your credentials and try again", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.INVALID_USERNAME) {
            Toast.makeText(this, "invalid username", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.NO_ADDRESS) {

            Toast.makeText(this, "Please provide the address", Toast.LENGTH_SHORT).show();
            Intent act2 = new Intent(SignInActivity.this, AndroidGPSTrackingActivity.class);
            act2.putExtra("calling-activity", Constants.ACTIVITY_2);
            // or ActivityConstants.ACTIVITY_3 if called form Activity3
            startActivity(act2);
            finish();


        }
    }
    @Override
    public void passvalue(int a)  {
        if(a==1){
            final String username = usernameEditText.getText().toString();
            final String password = passEditText.getText().toString();
            if (isValid(username, password)) {
                accountManager.normalSignIn(username, password);
            }}

    }
}
