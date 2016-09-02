package com.ftv_fashionshop.helixtech_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.ftv_fashionshop.helixtech_android.ProductDescriptionActivity;
import com.ftv_fashionshop.helixtech_android.R;
import com.ftv_fashionshop.helixtech_android.Share;
import com.ftv_fashionshop.helixtech_android.ViewAllActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import app.Constants;
import helper.HorizontalSpaceItemDecoration;
import helper.ToggleLikeOrWishList;


/**
 * Created by harsu on 02-06-2016.
 */
public class LandingPageAdapter extends RecyclerView.Adapter {
    JSONArray data;

    Context context;
    LayoutInflater inflater;
    Animation animation;


    public LandingPageAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new JSONArray();
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_around_center_point);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (data.getJSONObject(position).has("type"))
                return data.getJSONObject(position).getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 1;

    }

    public void setData(JSONArray data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1)
            return new MyViewHolder(inflater.inflate(R.layout.landing_card_view, parent, false));
        else
            return new MyViewHolder2(inflater.inflate(R.layout.landing_card_horizontal_container, parent, false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder)
            try {
                ((MyViewHolder) holder).bind(data.getJSONObject(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        else if (holder instanceof MyViewHolder2)
            try {
                ((MyViewHolder2) holder).bind(data.getJSONObject(position));
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
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            bmpUri = Uri.fromFile(file);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


    private void startProductDescription(JSONObject jsonObject) throws JSONException {
        Intent intent = new Intent(context, ProductDescriptionActivity.class);
        intent.putExtra("product_id", jsonObject.getInt("product_id"));
        context.startActivity(intent);

    }

    private Uri getImageURI(ImageView imageView) {
        Drawable mDrawable = imageView.getDrawable();
        Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                mBitmap, "Product Image", "");

        Uri uri = Uri.parse(path);
        return uri;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        Intent shareIntent;

        ImageView backgroundImage, share, like, wishList;
        TextView name, price, offerPrice;
        RatingBar ratingBar;
        View progressBar;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            offerPrice = (TextView) itemView.findViewById(R.id.offerPrice);
            backgroundImage = (ImageView) itemView.findViewById(R.id.imageView);
            share = (ImageView) itemView.findViewById(R.id.share);
            like = (ImageView) itemView.findViewById(R.id.like);
            wishList = (ImageView) itemView.findViewById(R.id.wish_list);
            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            progressBar =  itemView.findViewById(R.id.progress_bar);
            cardView = (CardView) itemView.findViewById(R.id.card);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = getLayoutPosition();
                        data.getJSONObject(position).put("isLoadingLike", true);
                        like.startAnimation(animation);
//                        notifyItemChanged(position);
                        ToggleLikeOrWishList.toggle(context, ToggleLikeOrWishList.LIKE, getLayoutPosition(), data.getJSONObject(getLayoutPosition()), new ToggleLikeOrWishList.ToggleListener() {
                            @Override
                            public void toggled(int which, int newStatus, int position) {
                                try {
                                    data.getJSONObject(position).put("likelist_status", newStatus);
                                    data.getJSONObject(position).put("isLoadingLike", false);
                                    notifyItemChanged(position);
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
                                            notifyItemChanged(position);
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startProductDescription(data.getJSONObject(getLayoutPosition()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                   /* if(clickListener!=null){
                        try {
                            clickListener.onItemClicked(v,data.getJSONObject(getLayoutPosition()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }*/
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
            int priceValue = object.getInt("price");
            Double offerPriceValue = priceValue * (100 - object.getDouble("offer_percentage")) / 100;
            if (offerPriceValue == priceValue) {
                offerPrice.setVisibility(View.GONE);
            } else {
                offerPrice.setVisibility(View.VISIBLE);
                offerPrice.setText("₹" + offerPriceValue.intValue());
            }
            Log.e(object.getString("product_name"),object.getJSONArray("images").getString(Constants.imageNumber));

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
                    } catch (Exception e) {
                        Toast.makeText(context, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
           /* share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //todo share image error
                    Uri bmpUri = getImageURI(backgroundImage);

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
            });*/


        }

        private void attachShareIntentAction(final JSONObject object) {
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey view this product " +
                                object.getString("product_name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Send to"));
                }
            });
        }

        public void prepareShareIntent(ImageView imageView) {
            // Fetch Bitmap Uri locally
            Uri bmpUri = getLocalBitmapUri(imageView); // see previous remote images section
            // Construct share intent as described above based on bitmap
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
        }
    }


    class MyViewHolder2 extends RecyclerView.ViewHolder {

        TextView categoryName;
        RecyclerView recyclerView;
        MyHorizontalAdapter horizontalAdapter;
        Button viewAllButton;

        public MyViewHolder2(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            horizontalAdapter = new MyHorizontalAdapter(context);
            recyclerView.setAdapter(horizontalAdapter);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(40));
            viewAllButton = (Button) itemView.findViewById(R.id.ViewAllButton);
            categoryName = (TextView) itemView.findViewById(R.id.categoryName);
            viewAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String category = data.getJSONObject(getLayoutPosition()).getJSONArray("data").getJSONObject(0).getString("product_categoryname");
                        String product_id = data.getJSONObject(getLayoutPosition()).getJSONArray("data").getJSONObject(0).getString("product_id");

                        startViewAllActivity(category, product_id);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void bind(JSONObject object) throws JSONException {
            horizontalAdapter.setHorizontalData(object.getJSONArray("data"));
            categoryName.setText(object.getJSONArray("data").getJSONObject(getAdapterPosition()).getString("product_categoryname"));

        }

        private void startViewAllActivity(String cat, String prod_id) {
            Intent intent = new Intent(context, ViewAllActivity.class);
            intent.putExtra("product_categoryname", cat);
            intent.putExtra("product_id", prod_id);
            context.startActivity(intent);
        }

    }

}
