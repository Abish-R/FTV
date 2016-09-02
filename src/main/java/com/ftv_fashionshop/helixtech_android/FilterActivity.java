package com.ftv_fashionshop.helixtech_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edmodo.rangebar.RangeBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterActivity extends AppCompatActivity {

    LinearLayout container;
    String data = "{\"price\":{\"max\":30000},\"discount\":{\"max\":50},\"array\":[{\"name\":\"Category\",\"type\":1,\"data\":[{\"name\":\"Men\",\"type\":1,\"data\":[{\"name\":\"Jeans\",\"type\":2},{\"name\":\"T-shirt\",\"type\":2}]},{\"name\":\"Women\",\"type\":1,\"data\":[{\"name\":\"Jeans\",\"type\":2},{\"name\":\"T-shirt\",\"type\":2}]}]},{\"name\":\"Material\",\"type\":1,\"data\":[{\"name\":\"Cotton\",\"type\":2},{\"name\":\"Silk\",\"type\":2}]},{\"name\":\"Size\",\"type\":1,\"data\":[{\"name\":\"S\",\"type\":2},{\"name\":\"M\",\"type\":2},{\"name\":\"L\",\"type\":2},{\"name\":\"XL\",\"type\":2}]}]}";
    int maximumPrice = 0;
    int maximumDiscount = 0;
    int theme;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getDesiredTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
        setTitle("Filters");

        final TextView priceMin = (TextView) findViewById(R.id.priceMin);
        final TextView priceMax = (TextView) findViewById(R.id.priceMax);
        RangeBar priceRangeBar = (RangeBar) findViewById(R.id.priceSeekBar);
        priceRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                if (leftThumbIndex == 0) {
                    priceMin.setText("Min");
                } else {
                    priceMin.setText(calculateValue(leftThumbIndex, maximumPrice));
                }


                priceMax.setText(calculateValue(rightThumbIndex, maximumPrice));
            }
        });

        final TextView discountMin = (TextView) findViewById(R.id.discountMin);
        final TextView discountMax = (TextView) findViewById(R.id.discountMax);
        RangeBar discountRangeBar = (RangeBar) findViewById(R.id.discountSeekBar);
        discountRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                if (leftThumbIndex == 0) {
                    discountMin.setText("Min");
                } else {
                    discountMin.setText(calculateValue(leftThumbIndex, maximumDiscount));
                }


                discountMax.setText(calculateValue(rightThumbIndex, maximumDiscount));
            }
        });


        container = (LinearLayout) findViewById(R.id.container);
        try {
            fillContainer();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private int getDesiredTheme() {
        SharedPreferences editor = FilterActivity.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = editor.getInt("colour", -1);

        switch (pos) {
            case 0:
                theme = R.style.AppTheme;
                break;
            case 1:
                theme = R.style.AppTheme2;
                break;
            case 2:
                theme = R.style.AppTheme3;
                break;
            case 3:
                theme = R.style.AppTheme4;
                break;
            case 4:
                theme = R.style.AppTheme5;
                break;
            case 5:
                theme = R.style.AppTheme6;
                break;
            case 6:
                theme = R.style.AppTheme7;
                break;

            case 7:
                theme = R.style.AppTheme8;
                break;
            default:
                theme = R.style.AppTheme;
                break;
        }
        return theme;

    }

    private void fillContainer() throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        maximumPrice = jsonObject.getJSONObject("price").getInt("max");
        maximumDiscount = jsonObject.getJSONObject("discount").getInt("max");

        container.removeAllViews();
        JSONArray array = jsonObject.getJSONArray("array");
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            View v = createView(object, container);
            container.addView(v);
        }
    }

    private View createView(JSONObject object, LinearLayout parent) throws JSONException {
        int type = object.getInt("type");
        View v;
        TextView name;
        switch (type) {
            case 1:
                v = LayoutInflater.from(this).inflate(R.layout.category_collapsed, parent, false);
                name = (TextView) v.findViewById(R.id.name);
                name.setText(object.getString("name"));
                final TextView options = (TextView) v.findViewById(R.id.options);
                options.setText("");
                fillOptions(options, object.getJSONArray("data"));
                final ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
                final LinearLayout container = (LinearLayout) v.findViewById(R.id.container);
                JSONArray array = object.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    View v1 = createView(array.getJSONObject(i), container);
                    container.addView(v1);
                }
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (container.getVisibility() == View.VISIBLE) {
                            container.setVisibility(View.GONE);
                            imageView.setImageResource(R.drawable.ic_keyboard_arrow_right);
                            options.setVisibility(View.VISIBLE);
                        } else {
                            container.setVisibility(View.VISIBLE);
                            imageView.setImageResource(R.drawable.ic_keyboard_arrow_down);
                            options.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case 2:
                v = LayoutInflater.from(this).inflate(R.layout.check_box_layout, parent, false);

                name = (TextView) v.findViewById(R.id.name);
                name.setText(object.getString("name"));
                final CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);
                        } else
                            checkBox.setChecked(true);
                    }
                });
                break;

            default:
                v = null;

        }
        return v;

    }

    private void fillOptions(TextView options, JSONArray data) throws JSONException {
        String text = "";
        for (int i = 0; i < data.length(); i++) {
            text += (data.getJSONObject(i).getString("name"));
            if (i != data.length() - 1)
                text += ", ";
        }
        options.setText(text);

    }

    private String calculateValue(int leftThumbIndex, int max) {


        return String.valueOf(max * leftThumbIndex / 10);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.save) {
            //todo save in shared prefs or something
        }

        return super.onOptionsItemSelected(item);
    }
}
