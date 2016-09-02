package com.ftv_fashionshop.helixtech_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import app.ThemeSetter;
import helper.Review;


public class ReviewDetail extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    TextView title, name, description, date;
    RatingBar ratingBar;
    View progressBar;
    int theme;
    ImageView profileImage, reviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        title = (TextView) findViewById(R.id.title);
        name = (TextView) findViewById(R.id.name);
        description = (TextView) findViewById(R.id.description);
        date = (TextView) findViewById(R.id.date);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        profileImage = (ImageView) findViewById(R.id.profile_image);
        reviewImage = (ImageView) findViewById(R.id.reviewImage);
        progressBar = findViewById(R.id.progress_bar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            final Review review = new Review(new JSONObject(getIntent().getStringExtra("review")));
            title.setText(review.getTitle());
            name.setText(review.getName());
            description.setText(review.getDescription());
            date.setText(review.getDate());
            ratingBar.setRating(review.getRating());
            if (!review.getReviewImage().isEmpty()) {

                progressBar.setVisibility(View.VISIBLE);
            }
            if (review.getProfileImage().isEmpty()) {
                profileImage.setImageResource(R.drawable.account_circle);
            } else {
                Picasso.with(this).load(review.getProfileImage())
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        profileImage.setImageResource(R.drawable.account_circle);
                    }
                });
            }
            Picasso.with(this).load(review.getReviewImage())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(reviewImage, new Callback() {
                @Override
                public void onSuccess() {

                    progressBar.setVisibility(View.GONE);
                    reviewImage.setVisibility(View.VISIBLE);

                }

                @Override
                public void onError() {

                    progressBar.setVisibility(View.GONE);
                    reviewImage.setVisibility(View.GONE);

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
