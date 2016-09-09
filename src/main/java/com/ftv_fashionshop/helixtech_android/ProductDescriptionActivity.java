package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.MyHorizontalAdapter;
import com.ftv_fashionshop.helixtech_android.adapter.ReviewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import accounts.AccountManager;
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.DividerDecoration;
import helper.MyCustomProgressDialog;
import helper.ToggleLikeOrWishList;


public class ProductDescriptionActivity extends AppCompatActivity {

    ImageView productImage;
    TextView description, sizes, price, offerPrice,availableSize;
    RelativeLayout bottomBar;
    //    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    ProgressDialog dialog;
    int theme;

    int product_id;
    RecyclerView relatedRecyclerView, reviewsRecyclerView;
    JSONObject data;
    MyHorizontalAdapter relatedAdapter;
    ReviewAdapter reviewAdapter;
    Button buyNow, addToCart;
    String image_urlProduct;
    Button viewAllReview;
    ImageView wishListed;
    CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredNoActionBarTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_description);

        product_id = getIntent().getIntExtra("product_id", -1);
        if (product_id == -1) {
            finish();
            return;
        }

        findViewById(R.id.descriptionView).setVisibility(View.INVISIBLE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        productImage = (ImageView) findViewById(R.id.imageViewProduct);
        description = (TextView) findViewById(R.id.description);
        sizes = (TextView) findViewById(R.id.sizes);
        price = (TextView) findViewById(R.id.price);
        offerPrice = (TextView) findViewById(R.id.offerPrice);
        availableSize = (TextView) findViewById(R.id.availableSize);
        wishListed = (ImageView) findViewById(R.id.wish_listed);
        //   collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsibleToolbar);
        reviewsRecyclerView = (RecyclerView) findViewById(R.id.review);
        relatedRecyclerView = (RecyclerView) findViewById(R.id.related);
        buyNow = (Button) findViewById(R.id.buyNow);
        addToCart = (Button) findViewById(R.id.addToCart);
        bottomBar = (RelativeLayout) findViewById(R.id.bottomBar);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        collapsingToolbarLayout.setVisibility(View.INVISIBLE);

        sendRequest();
    }

    public void goToSingleImage(View view) {
//        productImage.buildDrawingCache();
//        Bitmap bitmap = productImage.getDrawingCache();
        Intent intent = new Intent(this, SingleImage.class);
        Bundle bundle = new Bundle();
        bundle.putString("image_url", image_urlProduct);
        intent.putExtras(bundle);
        startActivity(intent);
        //setContentView(R.layout.activity_single_image);
//        NestedScrollView nest_scroll = (NestedScrollView)findViewById(R.id.nest_scroll);
//        AppBarLayout tool_main = (AppBarLayout)findViewById(R.id.tool_main);
//        nest_scroll.setVisibility(View.GONE);
//        tool_main.setVisibility(View.GONE);
//        TouchImageView mImageView = (TouchImageView)findViewById(R.id.customImageVIew1);
//        mImageView.setImageBitmap(bitmap);
//        mImageView.setVisibility(View.VISIBLE);
//
//        bottomBar.setVisibility(View.GONE);

        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        // (not needed unless you are going to change the drawable later)
//        PhotoViewAttacher mAttacher = new PhotoViewAttacher(mImageView);
//        mAttacher.update();

//        Display display = getWindowManager().getDefaultDisplay();
//
//// display size in pixels
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendRequest() {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.singlePageURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {
                    data = new JSONObject(s);
                    updateUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendRequest();
                            }
                        }).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("product_id", String.valueOf(product_id));
                params.put("customer_id", String.valueOf(AccountManager.getId(ProductDescriptionActivity.this)));
//                params.put("designer_id", String.valueOf(Constants.designer_id));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void updateUI() throws JSONException {
        findViewById(R.id.descriptionView).setVisibility(View.VISIBLE);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
//        collapsingToolbarLayout.setVisibility(View.VISIBLE);
        Picasso.with(this).load(data.getJSONArray("images").getString(Constants.imageNumberHigh))
                .priority(Picasso.Priority.HIGH)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(productImage, new Callback() {

            @Override
            public void onSuccess() {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
            }
            @Override
            public void onError() {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                productImage.setImageResource(R.drawable.ic_error);
            }
        });
        image_urlProduct = data.getJSONArray("images").getString(Constants.imageNumberHigh);
        description.setText(data.getString("product_description"));
        int priceValue = data.getInt("price");
        Double offerPriceValue = priceValue * (100 - data.getDouble("offer_percentage")) / 100;
        price.setText("₹" + priceValue);
        offerPrice.setText("₹" + offerPriceValue);
//        collapsingToolbarLayout.setTitle(data.getString("product_name"));
        setTitle(data.getString("product_name"));

        JSONArray sizes = data.getJSONArray("sizes");
        String size="";
        for(int i=0;i<sizes.length();i++) {
            if(sizes.length()==i+1)
                size += sizes.getString(i);
            else
                size += sizes.getString(i)+", ";
        }
        availableSize.setText(size);

        if (data.getInt("readytowear") == 0) {
            ((TextView) findViewById(R.id.availableTV)).setText("The product is not deliverable. Pay advance payment and make your booking confirm");
            findViewById(R.id.cities).setVisibility(View.GONE);
        } else {
            JSONArray cities = data.getJSONArray("cities");
            cityAdapter = new CityAdapter(this, cities);
            if (cities.length() == 0) {
                ((TextView) findViewById(R.id.availableTV)).setText("Not Deliverable in any city");
                findViewById(R.id.cities).setVisibility(View.GONE);
                findViewById(R.id.availableIV).setVisibility(View.GONE);
            } else {
                RecyclerView recyclerViewCities = (RecyclerView) findViewById(R.id.cities);
                recyclerViewCities.setAdapter(cityAdapter);
                recyclerViewCities.setLayoutManager(new LinearLayoutManager(this));
                findViewById(R.id.availableCard).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view =
                                findViewById(R.id.cities);
                        ImageView imageView = (ImageView) findViewById(R.id.availableIV);
                        if (view.getVisibility() == View.VISIBLE) {
                            imageView.setImageResource(R.drawable.ic_keyboard_arrow_down);
                            view.setVisibility(View.GONE);
                        } else {
                            imageView.setImageDrawable(null);
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
        if (data.getInt("cart_status") == 1) {
            addToCart.setText("View in Cart");
        } else {
            addToCart.setText("Add to Cart");
        }


        // sizes.setText(data.getString("sizes"));

//        relatedAdapter = new MyHorizontalAdapter(this);
//        relatedAdapter.setHorizontalData(data.getJSONArray("related"));
//        relatedRecyclerView.setAdapter(relatedAdapter);
//        relatedRecyclerView.setNestedScrollingEnabled(false);
//        relatedRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(40));
//        relatedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        if (relatedAdapter.getData().length() == 0) {
        findViewById(R.id.relatedCard).setVisibility(View.GONE);
//        }

        reviewAdapter = new ReviewAdapter(this);
        reviewsRecyclerView.setNestedScrollingEnabled(false);
        reviewAdapter.setData(data.getJSONArray("reviews"));
        reviewsRecyclerView.setAdapter(reviewAdapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.addItemDecoration(new DividerDecoration(this, DividerDecoration.VERTICAL_LIST));
        if (reviewAdapter.getData().length() == 0) {
            findViewById(R.id.reviewCard).setVisibility(View.GONE);
        }
        viewAllReview = (Button) findViewById(R.id.viewAllReview);
        viewAllReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProductDescriptionActivity.this, AllReview.class));
            }
        });

        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 09-06-2016 remove this when done
                Toast.makeText(ProductDescriptionActivity.this, "This part is not ready yet!", Toast.LENGTH_SHORT).show();
            }
        });
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (data.getInt("cart_status") == 1) {
                        startActivity(new Intent(ProductDescriptionActivity.this, CartActivity.class));
                    } else {
                        addToCart("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        setWishlistUI(data);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center_point);
        wishListed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wishListed.startAnimation(animation);
                ToggleLikeOrWishList.toggle(ProductDescriptionActivity.this,
                        ToggleLikeOrWishList.WISH_LIST, 0, data,
                        new ToggleLikeOrWishList.ToggleListener() {
                            @Override
                            public void toggled(int which, int newStatus, int position) {
                                try {
                                    data.put("wishlist_status", newStatus);
                                    wishListed.clearAnimation();
                                    setWishlistUI(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void toggleFailure(int position, int which) {

                                wishListed.clearAnimation();

                            }
                        });
            }
        });
        LayoutInflater inflater = LayoutInflater.from(this);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dynamicContainer);
        JSONObject dynamicData = data.getJSONObject("dynamic");
        Iterator<String> iter = dynamicData.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                String value = dynamicData.getString(key);
                View v = inflater.inflate(R.layout.dynamic_row, linearLayout, false);
                ((TextView) v.findViewById(R.id.title)).setText(key);
                ((TextView) v.findViewById(R.id.text)).setText(value);

                linearLayout.addView(v);
            } catch (JSONException e) {
                // Something went wrong!
            }
        }


    }

    private void chooseLocation() {
        try {
            JSONArray cities = data.getJSONArray("cities");
            final CharSequence[] items = new CharSequence[cities.length()];
            for (int i = 0; i < cities.length(); i++) {
                items[i] = cities.getString(i);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ProductDescriptionActivity.this);

            builder.setTitle("Choose Location");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    addToCart((String) items[position]);
                }
            });
            builder.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void setWishlistUI(JSONObject data) throws JSONException {
        if (data.getInt("wishlist_status") == 0) {
            wishListed.setImageResource(R.drawable.heart_outline);
        } else {
            wishListed.setImageResource(R.drawable.heart);
        }
    }

    private void addToCart(final String city) {


        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleCartURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {
                    JSONObject response = new JSONObject(s);
                    if (response.getInt("response") == 1) {
                        Toast.makeText(ProductDescriptionActivity.this, "Product added to cart", Toast.LENGTH_SHORT).show();
                        data.put("cart_status", 1);
                        addToCart.setText("view in cart");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                Toast.makeText(ProductDescriptionActivity.this, "Please Check your Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("customer_id", String.valueOf(AccountManager.getId(ProductDescriptionActivity.this)));
                params.put("product_id", String.valueOf(product_id));
                params.put("city", city);
                params.put("status", "1");
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

        Context context;
        JSONArray cities;

        public CityAdapter(Context context, JSONArray cities) {
            this.context = context;
            this.cities = cities;
        }

        @Override
        public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CityViewHolder(new TextView(context));
        }

        @Override
        public void onBindViewHolder(CityViewHolder holder, final int position) {
            try {
                holder.textView.setText(cities.getString(position));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return cities.length();
        }

        public class CityViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public CityViewHolder(final TextView textView) {
                super(textView);
                this.textView = textView;
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            }
        }
    }


}
