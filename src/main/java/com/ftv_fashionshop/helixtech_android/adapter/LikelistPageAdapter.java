package com.ftv_fashionshop.helixtech_android.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
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
import com.ftv_fashionshop.helixtech_android.LikeList;
import com.ftv_fashionshop.helixtech_android.ProductDescriptionActivity;
import com.ftv_fashionshop.helixtech_android.R;
import com.ftv_fashionshop.helixtech_android.Share;
import com.ftv_fashionshop.helixtech_android.SignUpActivity;
import com.ftv_fashionshop.helixtech_android.Wishlist;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
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

import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;
import accounts.AccountManager;
import helper.ToggleLikeOrWishList;


/**
 * Created by Harsh Goynka on 07/06/2016.
 */
public class LikelistPageAdapter extends RecyclerView.Adapter {
    int likeListStatus;
    JSONArray data;
    String likelist_status;
    Context context;
    int a=0;
    AccountManager accountManager;
    int product_id;
    ProgressDialog dialog;
    String customer_id;
    LayoutInflater inflater;
    Animation animation;
    public LikelistPageAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new JSONArray();
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_around_center_point);
    }


    public void setData(JSONArray data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder3(inflater.inflate(R.layout.likelist_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder3)
            try {
                ((MyViewHolder3) holder).bind(data.getJSONObject(position));
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

    class MyViewHolder3 extends RecyclerView.ViewHolder {

        ImageView backgroundImage, share, like, like_list;
        TextView name, price;
        RatingBar ratingBar;
        View progressBar;
        Button addToCart;

        public MyViewHolder3(View itemView) {
            super(itemView);
            dialog = new ProgressDialog(context);
            dialog.setMessage("Adding item to Cart");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setInverseBackgroundForced(false);

            backgroundImage = (ImageView) itemView.findViewById(R.id.imageView);
            share = (ImageView) itemView.findViewById(R.id.share);
            like = (ImageView) itemView.findViewById(R.id.like);
            like_list = (ImageView) itemView.findViewById(R.id.like_list);
            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);

            progressBar = itemView.findViewById(R.id.progress_bar);
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
            like_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = getLayoutPosition();
                        data.getJSONObject(position).put("isLoadingLike", true);
                        like_list.startAnimation(animation);
                        ToggleLikeOrWishList.toggle(context, ToggleLikeOrWishList.LIKE, getLayoutPosition(), data.getJSONObject(getLayoutPosition()), new ToggleLikeOrWishList.ToggleListener() {
                            @Override
                            public void toggled(int which, int newStatus, int position) {
                                try {
                                    data.getJSONObject(position).put("likelist_status", newStatus);
                                    data.getJSONObject(position).put("isLoadingLike", false);
                                    removeItem(getAdapterPosition());
                           //         notifyItemRemoved(getAdapterPosition());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void toggleFailure(int position, int which) {
                                try {
                                    data.getJSONObject(position).put("isLoadingLike", false);
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
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//                                        builder.setTitle("Choose Location");
//                                        builder.setItems(items, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int position) {
//                                                LikelistPageAdapter.this.dialog.show();
//                                                toggleCart( String.valueOf(product_id), getAdapterPosition());
//                                            }
//                                        });
//                                        builder.show();
                                        toggleCart(String.valueOf(product_id), getAdapterPosition());


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
                            LikelistPageAdapter.this.dialog.show();
                            toggleCart(String.valueOf(product_id), getAdapterPosition());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        public void bind(final JSONObject object) throws JSONException {
            if (!object.has("isLoadingLike")) {
                object.put("isLoadingLike", false);
            }
            object.put("likelist_status",1);


            progressBar.setVisibility(View.VISIBLE);
            backgroundImage.setVisibility(View.INVISIBLE);
            like_list.setImageResource(R.drawable.thumb_up);
            name.setText(object.getString("product_name"));
            price.setText("₹" + object.getString("price"));
            if(object.getInt("cart_status")==1 || a==1)
                addToCart.setText("View in Cart");
            else
                addToCart.setText("ADD TO CART");
            Picasso.with(context).load(object.getJSONArray("images").getString(Constants.imageNumber))
                    .into(backgroundImage, new Callback() {
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
            if (object.getBoolean("isLoadingLike"))
                like_list.startAnimation(animation);
            else
                like_list.clearAnimation();

            /*if (object.getInt("likelist_status") == 1) {
                like.setColorFilter(Color.parseColor("#3F51B5"));
            } else {

                like.setColorFilter(Color.parseColor("#ffffff"));

            }*/
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

//            like_list.setOnClickListener(new View.OnClickListener() {
//
//                public void onClick(View v) {
//                    try {
//                        product_id = object.getInt("product_id");
//                        toggleLikeList(product_id, getAdapterPosition());
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }



                    /* //todo share image error
                    Uri bmpUri = null;
                    try {
                        bmpUri = Uri.parse(object.getString("image_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //getLocalBitmapUri(backgroundImage);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    try {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey view this product " +
                                object.getString("product_name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (bmpUri != null) {
                        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                        shareIntent.setType("image/png");
                    }
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(shareIntent, "Send to"));  */
//                }
//            });

        }
        private void toggleCart(final String product_id, final int adapterPosition) {
            final ProgressDialog progress = new ProgressDialog(context);
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading...");
            // progress.show();
            StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleCartURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt("response") == 1) {
                            //  progress.dismiss();
                            dialog.dismiss();
                            //    Toast.makeText(context, "Successfully added in your cart", Toast.LENGTH_SHORT).show();
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            //    alertDialog.setTitle("Warning");
                            alertDialog.setMessage("Product is Added To Cart.\nDo you want to remove it from likelist");

                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Remove", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    toggleLikeList(product_id, adapterPosition);
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
                        else
                            Toast.makeText(context, "Some unknown error occurred ", Toast.LENGTH_SHORT).show();
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
                    map.put("customer_id", String.valueOf(accountManager.getId(context)));
                    map.put("product_id", String.valueOf(product_id));
                    map.put("status", "1");
                    map.put("quantity", "1");
                    //   map.put("city", cities);
                    return map;
                }
            };
            VolleySingleton.getInstance().getRequestQueue().add(request);


        }
    }





    private void toggleLikeList(final String product_id, final int adapterPosition) {
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleLike, new Response.Listener<String>() {
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
                map.put("customer_id", String.valueOf(accountManager.getId(context)));
                map.put("product_id", String.valueOf(product_id));
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
        if (data.length() == 0) {
            ((LikeList) context).toggleEmpty(View.VISIBLE);
        }


    }
    private void startProductDescription(JSONObject jsonObject) throws JSONException {
        Intent intent = new Intent(context, ProductDescriptionActivity.class);
        intent.putExtra("product_id", jsonObject.getInt("product_id"));
        context.startActivity(intent);

    }


}




