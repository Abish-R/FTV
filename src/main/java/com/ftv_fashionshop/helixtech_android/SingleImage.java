package com.ftv_fashionshop.helixtech_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import app.ThemeSetter;

public class SingleImage extends AppCompatActivity {
    int theme;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";

    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredNoActionBarTheme(this));
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_single_image);
    //  productImage = (ImageView) findViewById(R.id.imageView);
   /*     Bitmap bm = null;
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
   */   Bundle bundle = this.getIntent().getExtras();
        String pic = bundle.getString("image_url");

     //   ImageView productImage = null;
       //  Picasso.with(this).load(pic).into(customImageVIew1);

      //  Bitmap icon = BitmapFactory.decodeResource(getResources(),
       //               R.id.imageViewProduct);
       /* try {
            Bitmap icon =Picasso.with(this)
                    .load(pic).get();
            TouchImageView mImageView = (TouchImageView)findViewById(R.id.customImageVIew1);
            mImageView.setImageBitmap(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        getBitmapFromURL(pic);
        //  Bitmap icon = s.decodeResource(getResources(),
        // R.drawable.helix);



    }

    public void getBitmapFromURL(String src) {
        final ImageView iv = new ImageView(SingleImage.this);
        final TouchImageView mImageView = (TouchImageView) findViewById(R.id.customImageVIew1);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Picasso.with(this).load(src)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(iv, new Callback() {
            @Override
            public void onSuccess() {
                mImageView.setImageBitmap(((BitmapDrawable) iv.getDrawable()).getBitmap());
            }

            @Override
            public void onError() {

            }
        });


        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                back = (ImageView) findViewById(R.id.back);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:   // first finger down only
                        if (back.getVisibility() == View.INVISIBLE)
                            back.setVisibility(View.VISIBLE);
                        break;

                    case MotionEvent.ACTION_UP: // first finger lifted
                        if (back.getVisibility() == View.INVISIBLE)
                            back.setVisibility(View.VISIBLE);
                        break;

//                    case MotionEvent.ACTION_POINTER_UP: // second finger lifted
//                        Toast.makeText(getApplicationContext(),"Pointer Up",Toast.LENGTH_SHORT).show();
//                        break;
//
//                    case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
//                        Toast.makeText(getApplicationContext(),"Pointer Down",Toast.LENGTH_SHORT).show();
//                        break;

//                    case MotionEvent.ACTION_MOVE:
//                        Toast.makeText(getApplicationContext(),"Move",Toast.LENGTH_SHORT).show();
//                        break;
                }
                return false;
            }
        });


    }

    }
