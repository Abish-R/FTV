package com.ftv_fashionshop.helixtech_android.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.CartActivity;
import com.ftv_fashionshop.helixtech_android.ProductDescriptionActivity;
import com.ftv_fashionshop.helixtech_android.R;
import com.ftv_fashionshop.helixtech_android.Share;
import com.ftv_fashionshop.helixtech_android.Wishlist;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import accounts.AccountManager;
import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;
import helper.HorizontalSpaceItemDecoration;
import helper.ToggleLikeOrWishList;


/**
 * Created by Aman Agrawal on 07/06/2016.
 */
public class WishlistPageAdapter extends RecyclerView.Adapter {

    JSONArray data;
    Context context;
    LayoutInflater inflater;
    String wishlist_status;
    int index;
    int a=0;
    ProgressDialog dialog;

    String customer_id;
    int posi;
    Animation animation;

    public WishlistPageAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new JSONArray();
        customer_id = String.valueOf(AccountManager.getId(context));
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_around_center_point);
    }


    public void setData(JSONArray data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder3(inflater.inflate(R.layout.wishlist_card_view, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        index = position;
        if (holder instanceof MyViewHolder3)
            try {
                ((MyViewHolder3) holder).bind(data.getJSONObject(position), position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
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



    private void togglewishList(final String product_id, final int adapterPosition) {
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleWishList, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("response") == 1) {
                        removeItem(adapterPosition);

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
                map.put("customer_id", customer_id);
                map.put("product_id", product_id);
                map.put("status", "0");
                return map;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void removeItem(int adapterPosition) throws JSONException {

        List<JSONObject> temp = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            temp.add(data.getJSONObject(i));
        }
        data = new JSONArray();
        temp.remove(adapterPosition);
        for (int i = 0; i < temp.size(); i++) {
            data.put(temp.get(i));

        }

        notifyItemRemoved(adapterPosition);
        //notifyDataSetChanged();
        if (data.length() == 0) {
            ((Wishlist) context).toggleEmpty(View.VISIBLE);
        }

    }

    private void startProductDescription(JSONObject jsonObject) throws JSONException {
        Intent intent = new Intent(context, ProductDescriptionActivity.class);
        intent.putExtra("product_id", jsonObject.getInt("product_id"));
        context.startActivity(intent);

    }

    class MyViewHolder3 extends RecyclerView.ViewHolder {

        ImageView backgroundImage, share, wishList;
        TextView name, price, related;
        RatingBar ratingBar;
        View progressBar;
        Button addToCart;
        WishlistHorizontalAdapter horizontalAdapter;
        RecyclerView recyclerView;



        public MyViewHolder3(View itemView) {
            super(itemView);
            dialog = new ProgressDialog(context);
            dialog.setMessage("Adding item to Cart");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setInverseBackgroundForced(false);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            horizontalAdapter = new WishlistHorizontalAdapter(context);
            recyclerView.setAdapter(horizontalAdapter);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(40));
            backgroundImage = (ImageView) itemView.findViewById(R.id.imageView);
            share = (ImageView) itemView.findViewById(R.id.share);

            wishList = (ImageView) itemView.findViewById(R.id.wish_list);
            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            related = (TextView) itemView.findViewById(R.id.related);
            progressBar =itemView.findViewById(R.id.progress_bar);
            addToCart = (Button) itemView.findViewById(R.id.addToCart);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startProductDescription(data.getJSONObject(getLayoutPosition()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            wishList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = getLayoutPosition();
                        data.getJSONObject(position).put("isLoadingWish", true);
                        wishList.startAnimation(animation);
                        ToggleLikeOrWishList.toggle(context,
                                ToggleLikeOrWishList.WISH_LIST, getLayoutPosition(), data.getJSONObject(getLayoutPosition()),
                                new ToggleLikeOrWishList.ToggleListener() {
                                    @Override
                                    public void toggled(int which, int newStatus, int position) {
                                        try {
                                            data.getJSONObject(position).put("wishlist_status", newStatus);
                                            data.getJSONObject(position).put("isLoadingWish", false);
                                            removeItem(getAdapterPosition());
//                                            notifyItemChanged(position);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void toggleFailure(int position, int which) {
                                        try {
                                            data.getJSONObject(position).put("isLoadingWish", false);
                                            notifyItemChanged(position);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            addToCart.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        JSONObject object= data.getJSONObject(getAdapterPosition());
                        final int product_id = object.getInt("product_id");
                        if(object.getInt("cart_status")==1 || a==1)
                        {
                            context.startActivity(new Intent(context, CartActivity.class));
                            return;
                        }
                        if (object.getInt("readytowear") == 1) {
                            StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.getCities, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        final JSONObject object1 = new JSONObject(response);
                                        JSONArray cities = object1.getJSONArray("cities");
                                        final CharSequence[] items = new CharSequence[cities.length()];
                                        for (int i = 0; i < cities.length(); i++) {
                                            items[i] = cities.getString(i);
                                        }
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                                        builder.setTitle("Choose Location");
                                        builder.setItems(items, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int position) {
                                                WishlistPageAdapter.this.dialog.show();
                                                toggleAddToCart((String) items[position], String.valueOf(product_id), getAdapterPosition());
                                            }
                                        });
                                        builder.show();

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }


                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(context, "Connection Failure", Toast.LENGTH_SHORT).show();
                                }

                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {

                                    Map<String, String> map = new HashMap<>();
                                    map.put("designer_id", String.valueOf(Constants.designer_id));
                                    map.put("product_id", String.valueOf(product_id));

                                    return map;
                                }
                            };
                            VolleySingleton.getInstance().getRequestQueue().add(request);


                        } else {
                            WishlistPageAdapter.this.dialog.show();
                            toggleAddToCart("", String.valueOf(product_id), getAdapterPosition());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        public void bind(final JSONObject object, final int pos) throws JSONException {
            if (!object.has("isLoadingWish")) {
                object.put("isLoadingWish", false);
            }
            if (object.getJSONArray("related").length() > 0)

            {
                horizontalAdapter.setHorizontalData(object.getJSONArray("related"));
                recyclerView.setVisibility(View.VISIBLE);
                related.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
                related.setVisibility(View.GONE);
            }
            progressBar.setVisibility(View.VISIBLE);
            backgroundImage.setVisibility(View.INVISIBLE);

            name.setText(object.getString("product_name"));
            price.setText("₹" + object.getString("price"));
            if(object.getInt("cart_status")==1 || a==1)
                addToCart.setText("View in Cart");
            else
                addToCart.setText("ADD TO CART");
            Picasso.with(context).load(object.getJSONArray("images").getString(Constants.imageNumber)).into(backgroundImage, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    backgroundImage.setVisibility(View.VISIBLE);
                    Constants.loadedImageNumber = pos;

                }

                @Override
                public void onError() {
                    progressBar.setVisibility(View.GONE);
                    backgroundImage.setVisibility(View.VISIBLE);
                    backgroundImage.setImageResource(R.drawable.ic_error);
                }
            });
            if (object.getBoolean("isLoadingWish"))
                wishList.startAnimation(animation);
            else
            wishList.clearAnimation();






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
        private void toggleAddToCart(final String cities, final String product_id, final int adapterPosition) {
            StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleCartURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt("response") == 1) {
                            dialog.dismiss();
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            // alertDialog.setTitle("Warning");
                            alertDialog.setMessage("Product is Added To Cart.\nDo you want to remove it from wishlist");

                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Remove", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {


                                    togglewishList(product_id, adapterPosition);
                                }


                            });
                            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    addToCart.setText("View in Cart");
                                    a=1;
                                }
                            });
                            alertDialog.show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Connection Failure", Toast.LENGTH_SHORT).show();

                }

            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> map = new HashMap<>();
                    map.put("customer_id", customer_id);
                    map.put("product_id", product_id);
                    map.put("city", cities);
                    map.put("status", "1");
                    return map;
                }
            };
            VolleySingleton.getInstance().getRequestQueue().add(request);


        }

    }


}

