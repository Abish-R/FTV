package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
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

import accounts.AccountManager;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;

public class EditActivity extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    EditText editText;
    String tag;
    ProgressDialog dialog;
    int theme;

    public final static boolean checkemail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);


        editText = (EditText) findViewById(R.id.editText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
        tag = getIntent().getStringExtra("tag");
        setLayout(tag);

    }

    private void setLayout(String tag) {
        switch (tag) {
            case "name":
                setTitle("Enter Your Name");
                editText.setText(AccountManager.getName(this));
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "email":
                setTitle("Enter Your Email");
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                editText.setText(AccountManager.getEmail(this));
                break;
            case "phone":
                setTitle("Enter Your Number");
                editText.setText(AccountManager.getNumber(this));
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
           /* case "address":
                setTitle("Enter Your Address");


                break;*/
        }
    }

    private void saveResults() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("tag", tag);
        setResult(RESULT_OK, resultIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.save) {

            switch (tag) {
                case "name":
                    if ((editText.getText().toString()).length() < 2) {

                        //   Toast.makeText(getApplicationContext(), "Check the First name", Toast.LENGTH_SHORT).show();
                        editText.setError("Check the Name");
                    } /*else if ((editText.getText().toString()).contains("  ")) {

                        //Toast.makeText(getApplicationContext(), "Check the First name", Toast.LENGTH_SHORT).show();
                        editText.setError("Check the Name");

                    }*/ else {

                        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.editDetails, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                dialog.hide();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getInt("response") == 1) {
                                        dialog.show();
                                        AccountManager.updateName(editText.getText().toString(), EditActivity.this);
                                        saveResults();
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Nothing is changed", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.hide();
                            }

                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String, String> map = new HashMap<>();
                                map.put("first_name", editText.getText().toString());
                                map.put("last_name", "");
                                map.put("customer_id", String.valueOf(AccountManager.getId(EditActivity.this)));
                                return map;
                            }
                        };
                        VolleySingleton.getInstance().getRequestQueue().add(request);


                    }
                    break;
                case "email":
                    if (!checkemail(editText.getText().toString()) || editText.getText().toString().contains("-") || editText.getText().toString().charAt(0) == '.' || editText.getText().toString().charAt(0) == '@' || editText.getText().toString().charAt(0) == '_') {
                        //Toast.makeText(getApplicationContext(),"Not a valid email address",Toast.LENGTH_SHORT).show();
                        editText.setError("Not a valid email address");
                    } else {

                        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.editDetails, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                dialog.hide();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getInt("response") == 1) {
                                        dialog.show();
                                        AccountManager.updateMail(editText.getText().toString(), EditActivity.this);
                                        saveResults();
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Nothing is changed", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.hide();
                            }

                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String, String> map = new HashMap<>();
                                map.put("email", editText.getText().toString());
                                map.put("customer_id", String.valueOf(AccountManager.getId(EditActivity.this)));
                                return map;
                            }
                        };
                        VolleySingleton.getInstance().getRequestQueue().add(request);
                        break;
                    }
                case "phone":
                    boolean mobNoValidation = false;


                    if (((editText.getText().toString().length() == 10)) && ((editText.getText().toString().charAt(0) != '7') || (editText.getText().toString().charAt(0) != '8') || (editText.getText().toString().charAt(0) != '9')))

                        mobNoValidation = true;
                    if (!mobNoValidation) {

                        // Toast.makeText(getApplicationContext(), "Mobile number invalid. Must begin with 7 or 8 or 9", Toast.LENGTH_SHORT).show();
                        editText.setError("Mobile number invalid. Must begin with 7 or 8 or 9 and must be 10 characters in length");
                    } else {

                        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.editDetails, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                dialog.hide();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getInt("response") == 1) {
                                        dialog.show();
                                        AccountManager.updateNumber(editText.getText().toString(), EditActivity.this);
                                        saveResults();
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Nothing is changed", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.hide();
                            }

                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String, String> map = new HashMap<>();
                                map.put("mobile", editText.getText().toString());
                                map.put("customer_id", String.valueOf(AccountManager.getId(EditActivity.this)));
                                return map;
                            }
                        };
                        VolleySingleton.getInstance().getRequestQueue().add(request);
                        break;

                    }


            }


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

}
