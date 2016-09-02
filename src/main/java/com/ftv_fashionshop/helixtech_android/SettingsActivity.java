package com.ftv_fashionshop.helixtech_android;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import accounts.AccountManager;
import accounts.SocialProfileClasses;
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;

public class SettingsActivity extends AppCompatActivity {
    private static final int EDIT_REQUEST = 4321;
    TextView phone;
    TextView email;
    TextView name;
    int theme;
    ProgressDialog dialog;
    ImageView profile_image;
    TextView address;
    int customer_id,imageAdded=0;
    //Constant for enable camera request
    private static final int REQUEST_CAMERA = 4343;
    //Constant for ebaling gallery to select image
    private static final int SELECT_FILE = 12324;

    FloatingActionButton editImage;
    String userChoosenTask;
RelativeLayout changePasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        profile_image = (ImageView) findViewById(R.id.profile_image);
        editImage = (FloatingActionButton) findViewById(R.id.edit_image);
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        phone = (TextView) findViewById(R.id.number);
        address = (TextView) findViewById(R.id.address);
        changePasswordLayout = (RelativeLayout) findViewById(R.id.ChangePasswordLayout);
        setData();
        customer_id = AccountManager.getId(this);
        if(AccountManager.getCurrentSignIn(this) == SocialProfileClasses.SocialAccount.NORMAL) {

        }else
            changePasswordLayout.setVisibility(View.GONE);

        View nameLayout = findViewById(R.id.nameLayout);
        nameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = "name";
                startEditActivity(tag);
            }
        });
        View emailLayout = findViewById(R.id.emailLayout);
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = "email";
                startEditActivity(tag);
            }
        });
        View phoneLayout = findViewById(R.id.numberLayout);
        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = "phone";
                startEditActivity(tag);
            }
        });
        View addressLayout = findViewById(R.id.addressLayout);
        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent act2 = new Intent(SettingsActivity.this, AndroidGPSTrackingActivity.class);
                act2.putExtra("calling-activity", Constants.ACTIVITY_1);
                // or ActivityConstants.ACTIVITY_3 if called form Activity3
                startActivity(act2);

            }
        });
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
//             if(imageAdded==1) {
//                 File filePath = Share.uploadImage(SettingsActivity.this, profile_image, String.valueOf(AccountManager.getId(getApplicationContext())));
//                 new AsyncReviewImagePost(SettingsActivity.this, filePath).execute(String.valueOf(AccountManager.getId(getApplicationContext())), "0", "4");
//             }

            }

        });
    }

    public void goToForgotPasswordActivity(View view) {

        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.forgotPasswordURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("response") == 1) {
                        dialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "New password has been sent to your registered mail", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                        startActivity(intent);
                    } else
                        Toast.makeText(SettingsActivity.this, "Unknown Error Occured", Toast.LENGTH_SHORT).show();
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
                //      map.put("customer_id", String.valueOf(customer_id));
                map.put("user_name", AccountManager.getUserName(SettingsActivity.this));
                map.put("designer_id", String.valueOf(Constants.designer_id));
                return map;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    /**
     * Used to show alert to selected image from gallery or take photo from camera.
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = SignUpActivity.Utility.checkPermission(SettingsActivity.this);

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
            case SignUpActivity.Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
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
            else if (requestCode == EDIT_REQUEST) {
                    editLayout(data);
            }
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
        profile_image.setImageBitmap(bm);
        //imageAdded=1;
        File filePath = Share.uploadImage(SettingsActivity.this, profile_image, String.valueOf(AccountManager.getId(getApplicationContext())));
        new AsyncReviewImagePost(SettingsActivity.this, filePath).execute(String.valueOf(AccountManager.getId(getApplicationContext())), "0","4");
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
        profile_image.setImageBitmap(thumbnail);
        //imageAdded=1;
        File filePath = Share.uploadImage(SettingsActivity.this, profile_image, String.valueOf(AccountManager.getId(getApplicationContext())));
        new AsyncReviewImagePost(SettingsActivity.this, filePath).execute(String.valueOf(AccountManager.getId(getApplicationContext())), "0","4");
    }

    /**
     * Used to convert bitmap into string.
     *
     * @param bmp
     * @return
     */
    public String getStringImage(Bitmap bmp) {
        bmp = profile_image.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void setData() {

        if (AccountManager.getImageURL(this).isEmpty()) {
            profile_image.setImageResource(R.drawable.account_circle);
        } else {
            Picasso.with(SettingsActivity.this).load(AccountManager.getImageURL(this))
                    .placeholder(profile_image.getDrawable())
                    .into(profile_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    profile_image.setImageResource(R.drawable.account_circle);

                }
            });
        }
        name.setText(AccountManager.getName(this));
        email.setText(AccountManager.getEmail(this));
        String phoneText = AccountManager.getNumber(this);
        if (phoneText.isEmpty()) {
            phone.setText("Click to set Phone Number");
        } else
            phone.setText(AccountManager.getNumber(this));
        String addressText = AccountManager.getAddress(this);
        if (addressText.isEmpty())
            address.setText("Click to set address");
        else
            address.setText(addressText);
    }

    private void startEditActivity(String tag) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("tag", tag);
        startActivityForResult(intent, EDIT_REQUEST);
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == EDIT_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                editLayout(data);
//            }
//        }
//    }



    private void editLayout(Intent data) {
        switch (data.getStringExtra("tag")) {
            case "name":
                name.setText(AccountManager.getName(this));
                break;
            case "email":
                email.setText(AccountManager.getEmail(this));
                break;
            case "phone":
                phone.setText(AccountManager.getNumber(this));
                break;
            case "address":
                address.setText(AccountManager.getAddress(this));
                break;
        }
    }
}
