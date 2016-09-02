package com.ftv_fashionshop.helixtech_android;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ftv_fashionshop.helixtech_android.services.GPSTracker;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import accounts.AccountManager;
import app.Constants;
import app.ThemeSetter;
import helper.AsyncSignup;
import helper.AsyncSignupUsernameCheck;
import helper.CheckInternet;
import helper.MyCustomProgressDialog;

/**
 * Created by  : Aman Agrawal
 * Created on  : 4/06/2016.
 * Verified by:
 * Verified on:
 * This activity is used to guide the user into signing up into the page. It includes initializing the various required fields such as name, email, username etc and giving proper validations to them.
 * Also this activity is used to optionally receive profile picture from the user either via camera or gallery.
 */

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    static final int DATE_PICKER_ID = 1111;
    private static final int REQUEST_CAMERA = 4343;
    private static final int SELECT_FILE = 12324;
    private static final int LAST_LOCATION_REQUEST = 9999;
    EditText dob, fname;
    EditText lname;
    EditText mobNo;
    boolean nextPressed = false;
    CheckBox male;
    CheckBox female;
    Button btnShowAddress;
    Button next;
    Button submit;
    EditText stname;
    EditText city;
    int a = 0;
    EditText country;
    EditText pin;
    TextView dp;
    EditText state;
    RelativeLayout first, second;
    RelativeLayout dptext;
    String username;
    GPSTracker gps;
    String fname1, lname1, password1, email1, mobNo1;
    String selectedText;
    String stname1, city1, state1, country1, pin1;
    String gender;
    String userChoosenTask;
    CheckInternet checkInternet;
    boolean username_status = false;
    int diff;
    ActionBar actionBar;
    EditText mEdit;
    ProgressDialog dialog;
    EditText call_calendar;
    AndroidGPSTrackingActivity.LocationReceiver locationReceiver = new AndroidGPSTrackingActivity.LocationReceiver() {
        /**Used to set address into various edit text fields by getting address via interfaces from GPS Tracker activity.
         * @param address
         */
        @Override
        public void onLocationReceived(Address address) {

            stname.setText(address.getAddressLine(0) + " " + address.getAddressLine(1));
            city.setText(address.getSubAdminArea());
            state.setText(address.getAdminArea());
            country.setText(address.getCountryName());
            pin.setText(address.getPostalCode());
            dialog.hide();

        }

        /**
         * Used to show alert to enable GPS on device if it is switched off.
         */
        @Override
        public void GPSOFF() {
            dialog.hide();
            showSettingsAlert();
        }
    };
    private int year;
    private int month;
    private int day;
    private EditText uname, password, email;
    private ImageView imageView;
    private Bitmap bitmap;
    private int PICK_IMAGE_REQUEST = 1;
    private String UPLOAD_URL = "http://httest.in/ftvws/RUC_FileUpload";
    private String KEY_IMAGE = "picture";
    private TextWatcher uname1 = new TextWatcher() {

        /**Used to check username for validations with each character typed
         * @param s
         * @param start
         * @param before
         * @param count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String working = s.toString();
            boolean isValid = true;
            if (working.length() > 0) {
                if ((working.charAt(0) == '.') || (working.charAt(0) == '-') || (working.charAt(0) == '_')) {

                    isValid = false;
                }
                int count1 = 0;
                for (int i = 0; i < working.length(); i++) {
                    if (working.charAt(i) == ('@'))
                        count1++;
                }
                if (count1 > 1)
                    isValid = false;
            }
            if ((working.contains("..")) || (working.contains("--")) || (working.contains("__"))) {
                isValid = false;
            }

            if (!isValid) {
                uname.setText(working.substring(0, working.length() - 1));

                uname.setError("Enter a valid username");
                //  requestFocus(uname);

                uname.setSelection(uname.getText().length());
            } else {
                uname.setError(null);
                //    uname.clearFocus();

            }

        }

        /**
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {
        }

        /**
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

    };
    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {

        /**Used to pick up date from calendar and check for validation
         * @param view
         * @param selectedYear
         * @param selectedMonth
         * @param selectedDay
         */
        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth + 1;
            day = selectedDay;


            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            selectedText = format.format(calendar.getTime());


            Calendar c = Calendar.getInstance();
            int year1 = c.get(Calendar.YEAR);

            diff = year1 - year;
            if (diff > 10 && diff < 100)
                dob.setText(selectedText);
            else {// Toast.makeText(getApplicationContext(),"Enter a valid date of birth", Toast.LENGTH_SHORT).show();
                dob.setError("Enter a valid date of birth");
                requestFocus(dob);
                dob.setText("");
            }
        }
    };

    /**
     * Used to check validity of email entered by the user
     *
     * @param target
     * @return
     */
    public final static boolean checkemail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initializeViews();
        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setInverseBackgroundForced(false);

        submit.setOnClickListener(this);
        dptext.setOnClickListener(this);
        uname = (EditText) findViewById(R.id.uname);
        actionBar = getSupportActionBar();
        checkInternet = new CheckInternet(this);

        uname.addTextChangedListener(uname1);
        uname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    uname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    if (username_status == true) {
                        username_status = false;
                    }
                    //Toast.makeText(getApplicationContext(), "got the focus", Toast.LENGTH_LONG).show();

                } else {

                    if (uname.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (uname.getText().toString().length() > 2) {
                        username = uname.getText().toString();
                        if ((username.charAt(0) == '.') || (username.charAt(0) == '@') || (username.charAt(username.length() - 1) == '.') || (username.charAt(username.length() - 1) == '@')) {
                            uname.setError("Please enter a valid username");
                        } else if (username.length() > 4) {
                            if (checkInternet.internetCheck() == 1) {

                                new AsyncSignupUsernameCheck(SignUpActivity.this).execute(username);
                            } else {
                                //  username_status=false;
                                uname.setError("Connect to internet to verify username");

                                // requestFocus(uname);
                            }

                        } else
                            uname.setError("Please enter more than 4 characters");
                        // requestFocus(uname);

                    }
                    //Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();

                }
            }

        });


        male = (CheckBox) findViewById(R.id.male);
        female = (CheckBox) findViewById(R.id.female);
        male.setChecked(true);
        gender = "male";

        male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (female.isChecked() && isChecked) {

                    female.setChecked(false);
                    male.setChecked(true);
                    gender = "male";
                } else
                    gender = "";
            }
        });
        female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (male.isChecked() && isChecked) {
                    female.setChecked(true);
                    male.setChecked(false);
                    gender = "female";
                } else
                    gender = "";
            }
        });


        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        call_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username_status == false) {
                    if (uname.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (uname.getText().toString().length() > 2) {
                        username = uname.getText().toString();
                        if ((username.charAt(0) == '.') || (username.charAt(0) == '@') || (username.charAt(username.length() - 1) == '.') || (username.charAt(username.length() - 1) == '@')) {
                            uname.setError("Please enter a valid username");
                        } else if (username.length() > 4) {
                            if (checkInternet.internetCheck() == 1) {
                                nextPressed = true;
                                new AsyncSignupUsernameCheck(SignUpActivity.this).execute(username);


                            } else {
                                //  username_status=false;
                                uname.setError("Connect to internet to verify username");

                                // requestFocus(uname);
                            }

                        }
                    } else
                        uname.setError("Please enter more than 4 characters");
                    // requestFocus(uname);


                } else if (checkInternet.internetCheck() == 1) {
                    if (validation() && username_status) {
                        first.setVisibility(View.GONE);
                        second.setVisibility(View.VISIBLE);
                        setTitle("Address");
                        actionBar.setHomeButtonEnabled(true);
                        actionBar.setDisplayHomeAsUpEnabled(true);


                    }

                } else if (validation() && username_status && checkInternet.internetCheck() == 0) {
                    first.setVisibility(View.GONE);
                    second.setVisibility(View.VISIBLE);
                    setTitle("Address");
                    actionBar.setHomeButtonEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    Toast.makeText(SignUpActivity.this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(SignUpActivity.this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
            }
        });
        btnShowAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dialog.show();

                startGPSClass();
                //Toast.makeText(getApplicationContext(),gps.getLocality(),Toast.LENGTH_SHORT).show();
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uploadImage();
                stname1 = stname.getText().toString();
                city1 = city.getText().toString();
                state1 = state.getText().toString();
                country1 = country.getText().toString();
                pin1 = pin.getText().toString();
                int b=0;
                if (!pin1.isEmpty() && pin1.charAt(0) == '0') {
                    Toast.makeText(getApplicationContext(), "Check the Pincode", Toast.LENGTH_SHORT).show();
                } else if ((!stname1.equals("")) && (!city1.equals("")) && (!country1.equals("")) && (!pin1.equals("")) && (!state1.equals(""))) {
                    //  Toast.makeText(getApplicationContext(), "OK done", Toast.LENGTH_SHORT).show();

                    new AsyncSignup(SignUpActivity.this).execute(String.valueOf(Constants.designer_id), fname1, lname1, username, password1, email1, mobNo1, gender, selectedText, stname1, city1, state1, country1, pin1);
                    //  finish();

                } else if ((city1.equals("")) || (country1.equals("")) || (pin1.equals("")) || (stname1.equals("")) || (state1.equals(""))) {
                    Toast.makeText(getApplicationContext(), "None of the address field can be left empty", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();


            }


        });

    }

    public void callSignIn(int id){
        //Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show();


        if(a==1){
            File filePath = Share.uploadImage(SignUpActivity.this, imageView, "SignUp");
            new AsyncReviewImagePost(SignUpActivity.this, filePath).execute(id+"", "0","1");
        }else {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void startGPSClass() {
        if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LAST_LOCATION_REQUEST);
            return;
        }
        gps = new GPSTracker(SignUpActivity.this, locationReceiver);

        //you can hard-code the lat & long if you have issues with getting it
        //remove the below if-condition and use the following couple of lines
        //double latitude = 37.422005;
        //double longitude = -122.084095


        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();


        gps.getAddressFromLocation(latitude, longitude,
                getApplicationContext(), new GeocoderHandler());

    }



    /**
     * On clicking the back button the layouts visibility is set using this function
     * It is also used to give functionality to back button.
     *
     * @param item
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (second.getVisibility() == View.VISIBLE) {
                    first.setVisibility(View.VISIBLE);
                    second.setVisibility(View.GONE);
                    setTitle("SignUp");
                    getSupportActionBar().setDisplayShowHomeEnabled(false);
                } else {
                    startActivity(new Intent(this, LoginControllerActivity.class));
                    finish();
                    overridePendingTransition(0, 0);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get response from asyncSignupUsernameCheck and Check username availability and give appropriate toast and tick or cross symbol in username field
     *
     * @param i
     */
    public void passResponseToSignUpActivity(int i) {
        if (i == 1) {


            //Toast.makeText(getApplicationContext(), "Username available", Toast.LENGTH_SHORT).show();
            username_status = true;
            uname.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.tick), null);
            if (nextPressed == true) {
                if (validation() && username_status) {
                    first.setVisibility(View.GONE);
                    second.setVisibility(View.VISIBLE);
                    setTitle("Address");
                    actionBar.setHomeButtonEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(true);

                }
            }

        } else {

            Toast.makeText(getApplicationContext(), "Not available", Toast.LENGTH_SHORT).show();
            username_status = false;
            uname.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.cross), null);
        }


    }

    /**
     * Used to select date from the calendar by displaying the calendar.
     */
    public void selectDate() {
        showDialog(DATE_PICKER_ID);
    }

    /**
     * Used
     *
     * @param id
     * @return
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_PICKER_ID:

                // open datepicker dialog.
                // set date picker for current date
                // add pickerListener listner to date picker
                return new DatePickerDialog(this, pickerListener, year, month, day);
        }
        return null;
    }

    /**
     * Used to initialize various widgets declared in the layout file for this activity and is called in the {@link #onCreate(Bundle)}
     */
    private void initializeViews() {
        dob = (EditText) findViewById(R.id.dob);
        call_calendar = (EditText) findViewById(R.id.dob);
        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setDrawingCacheEnabled(true);
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);
        mobNo = (EditText) findViewById(R.id.mobNo);

        next = (Button) findViewById(R.id.next);
        submit = (Button) findViewById(R.id.submit);
        stname = (EditText) findViewById(R.id.stname);
        state = (EditText) findViewById(R.id.state);
        city = (EditText) findViewById(R.id.city);
        country = (EditText) findViewById(R.id.country);
        pin = (EditText) findViewById(R.id.pin);
        first = (RelativeLayout) findViewById(R.id.first);
        second = (RelativeLayout) findViewById(R.id.second);
        dptext = (RelativeLayout) findViewById(R.id.dptext);
        dp = (TextView) findViewById(R.id.dp);
        btnShowAddress = (Button) findViewById(R.id.btnShowAddress);

    }

    /**
     * Used to give validation for various SignUp fields like first name, last name, username, mobile number etc.
     *
     * @return boolean for the next button depending on which the onClick of next button will function.
     */
    private boolean validation() {

        boolean next_button_validation = false;
        fname1 = fname.getText().toString();
        lname1 = lname.getText().toString();

        password1 = password.getText().toString();
        email1 = email.getText().toString();
        mobNo1 = mobNo.getText().toString();

        boolean mobNoValidation = false;


        if (((mobNo.length() == 10)) && ((mobNo1.charAt(0) == '7') || (mobNo1.charAt(0) == '8') || (mobNo1.charAt(0) == '9')))

            mobNoValidation = true;

        if (fname1.length() < 2) {
            next_button_validation = false;
            //   Toast.makeText(getApplicationContext(), "Check the First name", Toast.LENGTH_SHORT).show();
            fname.setError("Check the First name");
            requestFocus(fname);
        } else if (fname1.contains("  ")) {
            next_button_validation = false;
            //Toast.makeText(getApplicationContext(), "Check the First name", Toast.LENGTH_SHORT).show();
            fname.setError("Check the First name");
            requestFocus(fname);

        } else if (lname1.length() < 1) {
            next_button_validation = false;
            // Toast.makeText(getApplicationContext(), "Check the Second name", Toast.LENGTH_SHORT).show();
            lname.setError("Check the Second name");
            requestFocus(lname);
        } else if (lname1.contains("  ")) {
            next_button_validation = false;
            // Toast.makeText(getApplicationContext(), "Check the Second name", Toast.LENGTH_SHORT).show();
            lname.setError("Check the Second name");
            requestFocus(lname);

        } else if (password1.length() < 5) {
            next_button_validation = false;
            // Toast.makeText(getApplicationContext(), "Password must be more than 5 characters", Toast.LENGTH_SHORT).show();
            password.setError("Password must be more than 5 characters");
            requestFocus(password);
        } else if (password1.contains("  ")) {
            next_button_validation = false;
            //Toast.makeText(getApplicationContext(), "Check the Password", Toast.LENGTH_SHORT).show();
            password.setError("Password cannot contain spaces");
            requestFocus(password);
        } else if (!checkemail(email.getText().toString())) {
            next_button_validation = false;
            //Toast.makeText(getApplicationContext(),"Not a valid email address",Toast.LENGTH_SHORT).show();
            email.setError("Not a valid email address");
            requestFocus(email);
        } else if (email1.contains("-") || email1.charAt(0) == '.' || email1.charAt(0) == '@' || email1.charAt(0) == '_') {
            next_button_validation = false;
            email.setError("Not a valid email address");
            requestFocus(email);

        } else if (!mobNoValidation) {
            next_button_validation = false;
            // Toast.makeText(getApplicationContext(), "Mobile number invalid. Must begin with 7 or 8 or 9", Toast.LENGTH_SHORT).show();
            mobNo.setError("Mobile number invalid. Must begin with 7 or 8 or 9");
            requestFocus(mobNo);
        } else if (gender.equals(" ")) {
            next_button_validation = false;
            Toast.makeText(getApplicationContext(), "Gender field cannot be left empty", Toast.LENGTH_SHORT).show();
        } else if (selectedText == null|| selectedText.length()<10) {
            next_button_validation = false;
            //Toast.makeText(getApplicationContext(),"Date of Birth cannot be left empty",Toast.LENGTH_SHORT).show();
            dob.setError("Date of Birth cannot be left empty");
            requestFocus(dob);

        } else {
            next_button_validation = true;

        }

        return next_button_validation;
    }

    @Override
    public void onClick(View v) {
        if (v == dptext) {

            selectImage();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Used to give functionality to the back button by specifying which activity will finish and which activity will the back button lead to.
     */
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        if (first.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
            Intent intent = new Intent(this, LoginControllerActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();

        } else {

            first.setVisibility(View.VISIBLE);
            second.setVisibility(View.GONE);
            setTitle("SignUp");
            getSupportActionBar().setDisplayShowHomeEnabled(false);


        }


    }

    /**
     * Used to show alert to selected image from gallery or take photo from camera.
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(SignUpActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Used to start camera to capture image.
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);

    }

    /**
     * Used to select image from gallery.
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

    }

    /**
     * Used to request permission depending on user chosen task to upload picture from gallery or camera.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
//code for deny
                }
                break;
        }
    }

    /**
     * Starts the process of capturing image from camera or gallery depending upon the response of user.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    /**
     * Used to select image from gallery.
     *
     * @param data
     */
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        imageView.setImageBitmap(bm);
        a = 1;

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * Used to capture image from camera.
     *
     * @param data
     */
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(thumbnail);
        a = 1;
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Used to convert bitmap into string.
     *
     * @param bmp
     * @return
     */
    public String getStringImage(Bitmap bmp) {
        bmp = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * Used to upload image to backend using the webservice {@link #UPLOAD_URL}
     */
    private void uploadImage() {
        //Showing the progress dialog

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        dialog.hide();

                        try {
                            JSONObject jsonRootObject = new JSONObject(s);
                            int response = jsonRootObject.getInt("response");
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //Showing toast message of the response
                        Toast.makeText(SignUpActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                    dialog.hide();


                        //Showing toast
                        Toast.makeText(SignUpActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Getting Image Name


                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put("customer_id", String.valueOf(1));


                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    /**
     * used to provide permission to read external storage and show alert dialog to do the same.
     */
    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(
                SignUpActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        SignUpActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public interface LocationReceiver {
        /**
         * This funtion is called when the address is successfully received.
         *
         * @param address address recieved is passed as a parameter to this function.
         */
        public void onLocationReceived(Address address);

        void GPSOFF();
    }

    public static class Utility {
        public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public static boolean checkPermission(final Context context) {
            int currentAPIVersion = Build.VERSION.SDK_INT;
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((SignUpActivity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Permission necessary");
                        alertBuilder.setMessage("External storage permission is necessary");
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.KITKAT)
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((SignUpActivity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        });
                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                    } else {
                        ActivityCompat.requestPermissions((SignUpActivity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    public class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;


            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();


                    locationAddress = bundle.getString("address");


                    break;
                default:
                    locationAddress = null;
            }
            //   tvAddress.setText(locationAddress);

        }
    }


}
