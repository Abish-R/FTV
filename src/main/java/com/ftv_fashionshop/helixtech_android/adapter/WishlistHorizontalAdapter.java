package com.ftv_fashionshop.helixtech_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ftv_fashionshop.helixtech_android.ProductDescriptionActivity;
import com.ftv_fashionshop.helixtech_android.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aman Agrawal on 20/06/2016.
 */
public class WishlistHorizontalAdapter extends RecyclerView.Adapter<WishlistHorizontalAdapter.MyViewHolder3> {

    JSONArray horizontalData;
    LayoutInflater inflater;
    Context context;
    Animation animation;

    public WishlistHorizontalAdapter(Context context) {
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
        return new MyViewHolder3(inflater.inflate(R.layout.wishlist_card_horizontal, parent, false));
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

    class MyViewHolder3 extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name,price;

        ProgressBar progressBar;

        public MyViewHolder3(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);

            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);

            name.setSelected(true);
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
        public void bind(final JSONObject object) throws JSONException{
            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            name.setText(object.getString("product_name"));
            price.setText("Price: "+ object.getString("price"));

            Picasso.with(context).load(object.getString("image1")).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageResource(R.drawable.ic_error);
                }
            });
        }
    }


}
