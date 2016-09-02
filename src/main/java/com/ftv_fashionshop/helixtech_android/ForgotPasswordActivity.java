package com.ftv_fashionshop.helixtech_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import accounts.AccountManager;

public class ForgotPasswordActivity extends AppCompatActivity {
    String oldPassword;
    String newPassword;
    String newPassword2;
    EditText OldPassword, Newpassword, NewPassword2;
    AccountManager accountManager;
    int customer_id;
    int theme;
    String userUnique;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        //   setTitle("Change Password");
        OldPassword = (EditText) findViewById(R.id.oldPasswordEditText);
        Newpassword = (EditText) findViewById(R.id.NewpasswordEditText);
        NewPassword2 = (EditText) findViewById(R.id.ReEnterpasswordEditText);
        Button login = (Button) findViewById(R.id.buttonSubmit);
        message = getIntent().getStringExtra("message");
        OldPassword.setText(message);
    }
    /** Used to change the theme by dynamically selecting the theme from desginer's side and store it in local database.Further it also used the {@link #setTheme(int)} to set the theme dynamically by using the theme stored in the shared preference.
     * @return theme stored in integer
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();

        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    private int getDesiredTheme() {
        SharedPreferences editor = ForgotPasswordActivity.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = editor.getInt("colour", -1);

        switch (pos) {
            case 0:
                theme = R.style.AppTheme;
                break;
            case 1:
                theme = R.style.AppTheme2;
                break;
            case 2:
                theme = R.style.AppTheme3;
                break;
            case 3:
                theme = R.style.AppTheme4;
                break;
            case 4:
                theme = R.style.AppTheme5;
                break;
            case 5:
                theme = R.style.AppTheme6;
                break;
            case 6:
                theme = R.style.AppTheme7;
                break;

            case 7:
                theme = R.style.AppTheme8;
                break;
            default:
                theme = R.style.AppTheme;
                break;
        }
        return theme;

    }
    public void DirectedToLoginPage( View view){
        oldPassword = OldPassword.getText().toString();
        newPassword = Newpassword.getText().toString();
        newPassword2 = NewPassword2.getText().toString();
        if(newPassword.length() < 3 || newPassword2.length() < 3 || oldPassword.length() < 3 || newPassword.contains(" ") || oldPassword.contains(" ") ){
            Toast.makeText(ForgotPasswordActivity.this, "Password should have more than 3 characters without spaces", Toast.LENGTH_SHORT).show();
        }
        else{
        customer_id = accountManager.getId(this);

            if(customer_id==-1){
                userUnique=getIntent().getStringExtra("user_name");
            }
            else{
                userUnique=customer_id+"";
            }
        if( newPassword.equals(newPassword2)){


        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.NewPasswordURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("response") == 1) {
                        Toast.makeText(ForgotPasswordActivity.this, "Verified the old password", Toast.LENGTH_SHORT).show();
                            finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("user_name", userUnique);
                map.put("old_password",oldPassword );
                map.put("new_password", newPassword);
                return map;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    } else{
            Newpassword.setText("");
            NewPassword2.setText("");
            Toast.makeText(ForgotPasswordActivity.this, "Both of the passwords do not match. Please re-enter again", Toast.LENGTH_SHORT).show();
      }}}
//    public void saveInfo() {
//
//        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
//
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString("old_password", oldPassword);
//        editor.putString("new_password", newPassword);
//        editor.putString("new_password2", newPassword2);
//        editor.apply();
//    }


    }

