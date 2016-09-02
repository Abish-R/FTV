package com.ftv_fashionshop.helixtech_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ftv_fashionshop.helixtech_android.AdReview;
import com.ftv_fashionshop.helixtech_android.OrderDetails;
import com.ftv_fashionshop.helixtech_android.R;
import com.ftv_fashionshop.helixtech_android.Share;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import app.Constants;


/**
 * Created by Aman Agrawal on 08/06/2016.
 */
public class MyOrdersAdapter extends RecyclerView.Adapter {
    JSONArray data;
    Context context;
    LayoutInflater inflater;

    boolean bitmap;

    public MyOrdersAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new JSONArray();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder4(inflater.inflate(R.layout.myorders_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder4)
            try {
                ((MyViewHolder4) holder).bind(data.getJSONObject(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    class MyViewHolder4 extends RecyclerView.ViewHolder {

        ImageView backgroundImage, share;
        TextView name, price, quantity, date, totalprice;
        ProgressBar progressBar;
      //  Button review;

        public MyViewHolder4(View itemView) {
            super(itemView);
            backgroundImage = (ImageView) itemView.findViewById(R.id.imageView);
            share = (ImageView) itemView.findViewById(R.id.share);
            date = (TextView) itemView.findViewById(R.id.purchaseDate);
            quantity = (TextView) itemView.findViewById(R.id.qty);
            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price_per_unit);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            totalprice = (TextView) itemView.findViewById(R.id.price);

         //   review = (Button) itemView.findViewById(R.id.review);
        }

        public void bind(final JSONObject object) throws JSONException {
            progressBar.setVisibility(View.VISIBLE);
            backgroundImage.setVisibility(View.INVISIBLE);
            quantity.setText("Qty: " + object.getString("quantity"));
            date.setText("Purchased on: " + object.getString("purchase_date"));

            name.setText(object.getString("product_name"));
            price.setText("Price/ut: ₹" + object.getString("price"));
            totalprice.setText("Total Price: ₹ " + object.getInt("price") * object.getInt("quantity"));

            Picasso.with(context).load(object.getJSONArray("images").getString(Constants.imageNumber)).fit().into(backgroundImage, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    backgroundImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {
                    progressBar.setVisibility(View.GONE);
                    backgroundImage.setVisibility(View.VISIBLE);
                    backgroundImage.setImageResource(R.drawable.ic_error);
                }
            });


          /*  review.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AdReview.class);
                    try {
                        intent.putExtra("product_name", object.getString("purchase_date"));
                        intent.putExtra("product_id", object.getString("product_id"));
                        intent.putExtra("price", object.getString("price"));
                        intent.putExtra("image_url", object.getString("image_url"));


                        context.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });*/
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        onclickItemview(data.getJSONObject(getLayoutPosition()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });


            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        Share.sharing(context, object.getString("product_name"), backgroundImage);
                    }catch (Exception e) {
                        Toast.makeText(context, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    private void onclickItemview(JSONObject jsonObject) throws JSONException {

        Intent intent = new Intent(context, OrderDetails.class);
        try {
            intent.putExtra("product_name", jsonObject.getString("product_name"));
            intent.putExtra("purchase_date", jsonObject.getString("purchase_date"));
            intent.putExtra("quantity", jsonObject.getString("quantity"));
            intent.putExtra("price", jsonObject.getString("price"));
            intent.putExtra("product_id", jsonObject.getInt("product_id"));
            intent.putExtra("image_url", jsonObject.getString("image_url"));
            context.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
