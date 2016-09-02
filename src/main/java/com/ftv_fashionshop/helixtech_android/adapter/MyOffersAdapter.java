package com.ftv_fashionshop.helixtech_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ftv_fashionshop.helixtech_android.OfferProducts;
import com.ftv_fashionshop.helixtech_android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Created by Aman Agrawal on 16/06/2016.
 */
public class MyOffersAdapter extends RecyclerView.Adapter {

    JSONArray data;
    Context context;
    LayoutInflater inflater;
    SimpleDateFormat sdfparse = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat sdfformat = new SimpleDateFormat("dd MMMM", Locale.getDefault());

    public MyOffersAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new JSONArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder5(inflater.inflate(R.layout.offer_card_view, parent, false));
    }

    /**
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder5)
            try {
                ((MyViewHolder5) holder).bind(data.getJSONObject(position));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }


    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    class MyViewHolder5 extends RecyclerView.ViewHolder {


        TextView title, percentage, description, dateTV;


        public MyViewHolder5(View itemView) {
            super(itemView);


            title = (TextView) itemView.findViewById(R.id.title);
            percentage = (TextView) itemView.findViewById(R.id.percentage);
            description = (TextView) itemView.findViewById(R.id.description);
            dateTV = (TextView) itemView.findViewById(R.id.date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, OfferProducts.class);
                    try {
                        intent.putExtra("offer_id",data.getJSONObject(getLayoutPosition()).getInt("offer_id"));
                        intent.putExtra("offer_name",data.getJSONObject(getLayoutPosition()).getString("offer_title"));
                        context.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


        }

        public void bind(final JSONObject object) throws JSONException, ParseException {

            title.setText(object.getString("offer_title"));
            if (object.getInt("offer_percentage") == 0) {
                percentage.setVisibility(View.GONE);
            } else {
                percentage.setVisibility(View.VISIBLE);
                percentage.setText(object.getString("offer_percentage") + "% off");
            }


            description.setText(object.getString("offer_description"));
            String startDate = sdfformat.format(sdfparse.parse(object.getString("offer_applicable_from")));
            String endDate = sdfformat.format(sdfparse.parse(object.getString("offer_applicable_till")));
            String date=startDate + " - " + endDate;
            dateTV.setText(date);


        }
    }

}


