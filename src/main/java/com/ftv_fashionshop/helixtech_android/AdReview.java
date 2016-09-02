package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;

/**
 * Created by  : Aman Agrawal
 * Created on  : 09/06/2016.
 * Verified by:
 * Verified on:
 * This activity is used to provide customers with the feature of adding review for the designers by giving in appropriate title, description and star rating.
 * The customer can also upload of photo if he/she wishes to do it.
 * The customer can also share his/her review on various social media.
 */
public class AdReview extends AppCompatActivity {

    ImageView reviewImg;
    /*TextView prodName;
    TextView price;*/
    EditText title;
    boolean backPressed = false;
    int theme;
    AccountManager accountManager;
    int a=0,b=0;
    TextView cusName;
    EditText description;
    //Constant for enable camera request
    private static final int REQUEST_CAMERA = 4343;
    //Constant for ebaling gallery to select image
    private static final int SELECT_FILE = 12324;
    RatingBar ratingBar;
    String userChoosenTask;
    Bitmap bitmap;


    String product_id;
    Button submitButton;
    ProgressDialog dialog;
    String ratedValue = null;
    FloatingActionButton editImage;

    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addreview);
        initializeViews();

        //Setting up back button on the anctivity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            /**Used to display rating on the rating bar by converting it into String
             * @param ratingBar
             * @param rating
             * @param fromUser
             */
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                ratedValue = String.valueOf(ratingBar.getRating());

            }
        });

        //Here we are receiving the product id using intents from the previous activity.
        product_id = (getIntent().getStringExtra("product_id"));

        // prodName.setText(getIntent().getStringExtra("product_name"));
        // price.setText("₹ " + getIntent().getStringExtra("price"));
        /*Picasso.with(this).load(getIntent().getStringExtra("image_url")).fit().into(imageView, new Callback() {
            @Override
            public void onSuccess() {

                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.ic_error);
            }
        });*/

        cusName.setText("Review By: " + AccountManager.getName(this));

        //On clicking the submit button the review is added for the particular designer and an alert is shown if the customer wants to share his/her review on social media.
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                    if(title.getText().toString().length() != 0 && description.getText().toString().length() != 0 && ratedValue != null )
                    {
                        addReview();
                    }

                    else if(ratedValue == null && title.getText().toString().length() != 0 && description.getText().toString().length() != 0) {
                        AlertDialog alertDialog = new AlertDialog.Builder(AdReview.this).create();
                        //    alertDialog.setTitle("Warning");
                        alertDialog.setMessage("You have not entered the rating so by default it will taken as 5");

                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ratedValue = "5";
                                addReview();
                            }
                        });

                        alertDialog.show();

                    }

                    else {
                        AlertDialog alertDialog = new AlertDialog.Builder(AdReview.this).create();
                        //    alertDialog.setTitle("Warning");
                        alertDialog.setMessage("None of the fields can be left empty");

                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        alertDialog.show();
                    }


            }
        });



        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();



            }
        });


    }



    /**
     * Used to initialize various views used in the layout of this activity.
     */
    private void initializeViews() {

        reviewImg = (ImageView) findViewById(R.id.reviewImg);

        title = (EditText) findViewById(R.id.title);
        cusName = (TextView) findViewById((R.id.cusName));
        description = (EditText) findViewById(R.id.description);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        submitButton = (Button) findViewById(R.id.submitButton);
        editImage = (FloatingActionButton) findViewById(R.id.editImage);
        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

    }

    @Override
    public void onBackPressed() {

        if (!backPressed) {
            backPressed = true;
            if(title.getText().toString().length() != 0 || description.getText().toString().length() != 0  || ratedValue != null || a==1)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(AdReview.this).create();
                //    alertDialog.setTitle("Warning");
                alertDialog.setMessage("You are in the middle of reviewing! Can we discard?");

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    backPressed = false;
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(r, 2000);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Used to add review for the designer and send details like title,description and star rating it to backend using the web servic {@link UrlConstants#addReview}
     */
    private void addReview() {
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.addReview, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.hide();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("response") == 1) {
                        if (a == 1) {
                            callImageUpload();
                        }else
                            finish();
                    } else if (jsonObject.getInt("response") == 2) {
                        if (a == 1) {
                            callAlert(2);

                        }else
                            finish();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.hide();
                Snackbar.make(findViewById(R.id.review_container), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addReview();
                            }
                        }).show();
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("review_title", title.getText().toString());
                map.put("review_description", description.getText().toString());
                map.put("star_rating", ratedValue);
                map.put("customer_id", String.valueOf(AccountManager.getId(AdReview.this)));
                return map;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void callImageUpload(){
            File filePath = Share.uploadImage(AdReview.this, reviewImg, String.valueOf(accountManager.getId(getApplicationContext())));
            new AsyncReviewImagePost(AdReview.this, filePath).execute(String.valueOf(accountManager.getId(getApplicationContext())), "1", "3");

   }

    public void callAlert(int val){
        if(val==1) {
            showAlert("Share Review","Review Added Successfully.\nThe review will be updated in 24 hours.\n" +
                    "Do you want to share your review on social media?","Share");
        }else if(val==2){
            showAlert("Update Review","You already posted review. Do you want to change?","Post");
        }
    }

    public void showAlert(final String tit,String msg,String btnPostive){
        final AlertDialog alertDialog = new AlertDialog.Builder(AdReview.this).create();
        alertDialog.setTitle(tit);
        alertDialog.setMessage(msg);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, btnPostive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if(tit.equals("Update Review"))
                        updateReview(2);
                    else
                        Share.sharing(AdReview.this,title.getText().toString(), reviewImg);
                } catch (Exception e) {
                    Toast.makeText(AdReview.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.show();
    }

    private void updateReview(final int val) {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.updateReview, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.hide();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("response") == 1) {
                        if(a==1)
                            callImageUpload();
                    }else
                        finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.hide();
                Snackbar.make(findViewById(R.id.review_container), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateReview(0);
                            }
                        }).show();
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("review_title", title.getText().toString());
                map.put("designer_id", String.valueOf(Constants.designer_id));
                map.put("review_description", description.getText().toString());
                map.put("star_rating", ratedValue);
                map.put("customer_id", String.valueOf(AccountManager.getId(AdReview.this)));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AdReview.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = SignUpActivity.Utility.checkPermission(AdReview.this);

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

    /**Used to request permission depending on user chosen task to upload picture from gallery or camera.
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

    /**Starts the process of capturing image from camera or gallery depending upon the response of user.
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

    /**Used to select image from gallery.
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
        reviewImg.setImageBitmap(bm);
        a=1;
    }

    /**Used to capture image from camera.
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
        reviewImg.setImageBitmap(thumbnail);
        a=1;
    }

    /**Used to convert bitmap into string.
     * @param bmp
     * @return
     */
    public String getStringImage(Bitmap bmp) {
        bmp = reviewImg.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**Used to implement back button.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }


}
