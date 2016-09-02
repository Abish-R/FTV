package com.ftv_fashionshop.helixtech_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ftv_fashionshop.helixtech_android.R;
import com.ftv_fashionshop.helixtech_android.ReviewDetail;

import org.json.JSONArray;
import org.json.JSONException;

import helper.Review;

/**
 * Created by harsu on 09-06-2016.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    JSONArray data;
    Context context;
    LayoutInflater inflater;

    public ReviewAdapter(Context context) {
        this.context = context;
        data = new JSONArray();
        inflater = LayoutInflater.from(context);
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReviewViewHolder(inflater.inflate(R.layout.review_row, parent, false));
    }


    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        try {
            holder.bind(new Review(data.getJSONObject(position)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return data.length();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView name, title, description, date;
        RatingBar rating;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            date = (TextView) itemView.findViewById(R.id.time);
            rating = (RatingBar) itemView.findViewById(R.id.ratingBar);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ReviewDetail.class);
                    try {
                        intent.putExtra("review", data.getJSONObject(getLayoutPosition()).toString());
                        context.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void bind(Review item) {
            name.setText(item.getName());
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            rating.setRating(item.getRating());
            date.setText(item.getDate());
        }
    }


}
