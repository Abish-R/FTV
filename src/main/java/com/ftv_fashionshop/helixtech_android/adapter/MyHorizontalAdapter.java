package com.ftv_fashionshop.helixtech_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ftv_fashionshop.helixtech_android.ProductDescriptionActivity;
import com.ftv_fashionshop.helixtech_android.R;
import com.ftv_fashionshop.helixtech_android.Share;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.Constants;
import helper.ToggleLikeOrWishList;

public class MyHorizontalAdapter extends RecyclerView.Adapter<MyHorizontalAdapter.MyViewHolder3> {

    JSONArray horizontalData;
    LayoutInflater inflater;
    Context context;
    Animation animation;

    public MyHorizontalAdapter(Context context) {
        this.context = context;
        horizontalData = new JSONArray();
        inflater = LayoutInflater.from(context);
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_around_center_point);
    }

    public void setHorizontalData(JSONArray horizontalData) {
        this.horizontalData = horizontalData;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder3 onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder3(inflater.inflate(R.layout.landing_card_horiontal_view, parent, false));
    }


    @Override
    public void onBindViewHolder(MyViewHolder3 holder, int position) {
        try {
            holder.bind(horizontalData.getJSONObject(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return horizontalData.length();
    }

    private void startProductDescription(JSONObject jsonObject) throws JSONException {
        Intent intent = new Intent(context, ProductDescriptionActivity.class);
        intent.putExtra("product_id", jsonObject.getInt("product_id"));
        context.startActivity(intent);

    }

    public JSONArray getData() {
        return horizontalData
                ;
    }

    class MyViewHolder3 extends RecyclerView.ViewHolder {


        ImageView backgroundImage, share, like, wishList;
        TextView name, price;
        RatingBar ratingBar;
        View progressBar;

        public MyViewHolder3(View itemView) {
            super(itemView);
            backgroundImage = (ImageView) itemView.findViewById(R.id.imageView);
            share = (ImageView) itemView.findViewById(R.id.share);
            like = (ImageView) itemView.findViewById(R.id.like);
            wishList = (ImageView) itemView.findViewById(R.id.wish_list);
            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            progressBar =  itemView.findViewById(R.id.progress_bar);

            name.setSelected(true);

            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = getLayoutPosition();
                        horizontalData.getJSONObject(position).put("isLoadingLike", true);
                        notifyItemChanged(position);
                        ToggleLikeOrWishList.toggle(context, ToggleLikeOrWishList.LIKE, position, horizontalData.getJSONObject(position), new ToggleLikeOrWishList.ToggleListener() {
                            @Override
                            public void toggled(int which, int newStatus, int position) {
                                try {
                                    horizontalData.getJSONObject(position).put("likelist_status", newStatus);
                                    horizontalData.getJSONObject(position).put("isLoadingLike", false);
                                    notifyItemChanged(position);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void toggleFailure(int position, int which) {
                                try {
                                    horizontalData.getJSONObject(position).put("isLoadingLike", false);
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
            wishList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = getLayoutPosition();
                        horizontalData.getJSONObject(position).put("isLoadingWish", true);
                        wishList.startAnimation(animation);
                        ToggleLikeOrWishList.toggle(context,
                                ToggleLikeOrWishList.WISH_LIST, position, horizontalData.getJSONObject(position),
                                new ToggleLikeOrWishList.ToggleListener() {
                                    @Override
                                    public void toggled(int which, int newStatus, int position) {
                                        try {
                                            horizontalData.getJSONObject(position).put("wishlist_status", newStatus);
                                            horizontalData.getJSONObject(position).put("isLoadingWish", false);
                                            notifyItemChanged(position);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void toggleFailure(int position, int which) {
                                        try {
                                            horizontalData.getJSONObject(position).put("isLoadingWish", false);
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startProductDescription(horizontalData.getJSONObject(getLayoutPosition()));
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
            if (!object.has("isLoadingWish")) {
                object.put("isLoadingWish", false);
            }
            progressBar.setVisibility(View.VISIBLE);
            backgroundImage.setVisibility(View.INVISIBLE);
            name.setText(object.getString("product_name"));
            price.setText("₹" + object.getString("price"));
            Picasso.with(context).load(object.getJSONArray("images").getString(Constants.imageNumber)).into(backgroundImage, new Callback() {
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
            if (object.getInt("wishlist_status") == 1) {
                wishList.setImageResource(R.drawable.heart);

            } else {
                wishList.setImageResource(R.drawable.heart_outline);

            }
            if (object.getInt("likelist_status") == 1) {
                like.setImageResource(R.drawable.thumb_up);

            } else {
                like.setImageResource(R.drawable.thumb_up_outline);
            }

            if (object.getBoolean("isLoadingLike"))
                like.startAnimation(animation);
            else
                like.clearAnimation();

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

            /*share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //todo share image error
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
                    context.startActivity(Intent.createChooser(shareIntent, "Send to"));
                }
            });
            share.setVisibility(View.INVISIBLE);*/

        }
    }

}