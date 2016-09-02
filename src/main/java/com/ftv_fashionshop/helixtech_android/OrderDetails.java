package com.ftv_fashionshop.helixtech_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import accounts.AccountManager;
import app.ThemeSetter;


/**
 * Created by  : Aman Agrawal
 * Created on  : 10/06/2016.
 * Verified by:
 * Verified on:
 * This activity is used to display the details of the orders placed by the customer such as purchase date, quantity, total price, email, phone number and also display the product image and per unit price.
 * */
public class OrderDetails extends AppCompatActivity {

    TextView purchaseDate;
    TextView items;
    TextView grandTotal;
    TextView phone;
    TextView email;
//    TextView prodName;
    ImageView prodImg;
    int theme;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
//    TextView priceUt;
    //Todo mange your order


    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        initializeViews();
        setTitle(getIntent().getStringExtra("product_name"));
//        prodName.setText(getIntent().getStringExtra("product_name"));
//        priceUt.setText("Price: ₹ " + getIntent().getStringExtra("price"));
        Picasso.with(this).load(getIntent().getStringExtra("image_url")).fit().centerInside().into(prodImg, new Callback() {
            @Override
            public void onSuccess() {

                prodImg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

                prodImg.setVisibility(View.VISIBLE);
                prodImg.setImageResource(R.drawable.ic_error);
            }
        });
        if (AccountManager.getNumber(this).isEmpty()) {
            phone.setVisibility(View.GONE);
        } else {
            phone.setText(AccountManager.getNumber(this));
        }
        email.setText(AccountManager.getEmail(this));

        purchaseDate.setText("Ordered on\n" + getIntent().getStringExtra("purchase_date"));
        items.setText("Items\n" + getIntent().getStringExtra("quantity"));
        grandTotal.setText("Grand Total:\n ₹" + (Integer.valueOf(getIntent().getStringExtra("quantity")) * (Integer.valueOf(getIntent().getStringExtra("price")))));
    }


    /**
     * Used to initialize various widgets given in the layout for this activity.
     */
    private void initializeViews() {

        purchaseDate = (TextView) findViewById(R.id.purchaseDate);
        items = (TextView) findViewById(R.id.items);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        phone = (TextView) findViewById(R.id.phone);
        email = (TextView) findViewById(R.id.email);
//        prodName = (TextView) findViewById(R.id.prodName);
//        priceUt = (TextView) findViewById(R.id.priceUt);
        prodImg = (ImageView) findViewById(R.id.prodImg);

        prodImg.setOnClickListener(new View.OnClickListener(){

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProductDescriptionActivity.class);
                intent.putExtra("product_id", getIntent().getIntExtra("product_id",-1));
                startActivity(intent);
            }
        });


    }
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
