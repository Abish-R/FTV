package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import accounts.AccountManager;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;
import helper.ProductRemovedListener;
import helper.QuantityChangeListener;

public class CartActivity extends AppCompatActivity implements QuantityChangeListener, ProductRemovedListener {

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    JSONArray data;
    RecyclerView mRecyclerView;
    CartAdapter mAdapter;
    ProgressDialog dialog;
    int theme;
    double totalPrice, advancePrice;
    Button buyNow2;
    int product_id;
    String product_name;
    String designer_mobile;
    AccountManager accountManager;
    String name2;

    TextView totalPriceTV;
    DecimalFormat df = new DecimalFormat("0");
    View totAmountLayout;
    int finalQty = 0;

    @Override
    public void quantityChanged(final int position, final int changeQuantity) {


        try {

            JSONObject object = data.getJSONObject(position);
            finalQty = object.getInt("qty") + changeQuantity;
            product_id = object.getInt("product_id");
            product_name = object.getString("product_name");
            if (finalQty <= 0 || finalQty > 5) {
                return;
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }


        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleCartURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.getInt("response") == 1) {
                        data.getJSONObject(position).put("qty", finalQty);
                        mAdapter.notifyItemChanged(position);
                        updateTotalPrice();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CartActivity.this, "Unknown Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                Toast.makeText(CartActivity.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                try {
                    params.put("customer_id", String.valueOf(AccountManager.getId(CartActivity.this)));
                    params.put("product_id", data.getJSONObject(position).getString("product_id"));
                    params.put("qty", String.valueOf(finalQty));
                    //to insert/update
                    params.put("status", "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        data = new JSONArray();
        totalPrice = 0;

        totAmountLayout = findViewById(R.id.totalAmountLayout);
        totalPriceTV = (TextView) findViewById(R.id.total_price);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new CartAdapter(this);
        mAdapter.setQuantityChangeListener(this);
        mAdapter.setProductRemovedListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        totAmountLayout.setVisibility(View.GONE);
        buyNow2 = (Button) findViewById(R.id.buyNow2);

        buyNow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(CartActivity.this);
                dialog.setMessage("You have to pay advance of ₹" + advancePrice);
                dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(CartActivity.this, "This part of the app is under progress!", Toast.LENGTH_SHORT).show();
                        paymentGateway(advancePrice);
                    }
                });
                dialog.setNegativeButton("Cancel", null);
                dialog.show();
            }
        });

        sendRequest();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void paymentGateway(double advancePrice) {


       /* PaymentParams mPaymentParams = new PaymentParams();
        mPaymentParams.setKey("gtKFFx");
        mPaymentParams.setAmount(Double.toString(advancePrice));
        mPaymentParams.setProductInfo(getResources().getString(R.string.app_name));
        mPaymentParams.setFirstName(AccountManager.getName(this));
        mPaymentParams.setEmail(AccountManager.getEmail(this));
        mPaymentParams.setTxnId("0123456789");
        mPaymentParams.setSurl("httpss://payu.herokuapp.com/success");
        mPaymentParams.setFurl("httpss://payu.herokuapp.com/failure");
        PayuHashes payuHashes = new PayuHashes();
        mPaymentParams.setHash(payuHashes.getPaymentHash());*/
        Intent intent = new Intent(this, PayUMoneyActivity.class);
        intent.putExtra("amount", advancePrice);
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    private void sendRequest() {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.cartListURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {

                    JSONObject object = new JSONObject(s);
                    data = object.getJSONArray("data");
                    updateTotalPrice();
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                Snackbar.make(findViewById(R.id.cartContainer), "No internet connection", Snackbar.LENGTH_INDEFINITE)
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

                params.put("customer_id", String.valueOf(AccountManager.getId(CartActivity.this)));

                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void updateTotalPrice() {
        totalPrice = 0;
advancePrice=0;
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                int priceValue = object.getInt("price") * object.getInt("qty");
                Double offerPriceValue = priceValue * (100 - object.getDouble("offer_percentage")) / 100;
                totalPrice += offerPriceValue.intValue();
                Double advancePayment = offerPriceValue * (object.getInt("advance_amount_percentage")) / 100;
                advancePrice += advancePayment.intValue();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if (data.length() > 0) {
            totAmountLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.cartEmpty).setVisibility(View.GONE);
        } else {
            totAmountLayout.setVisibility(View.GONE);
            findViewById(R.id.cartEmpty).setVisibility(View.VISIBLE);
        }
        totalPriceTV.setText("₹" + (totalPrice));
        /*if(totalPrice==advancePrice){
            findViewById(R.id.advanceContainer).setVisibility(View.GONE);
        }*/
        /*else{
            findViewById(R.id.advanceContainer).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.total_advance)).setText("₹"+advancePrice);
        }*/
    }


    @Override
    public void onProductRemoved(final int position) throws JSONException {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Are you sure you want to delete " + data.getJSONObject(position).getString("product_name") + "?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendProductRemoveRequest(position);
            }
        });
        alertDialog.setNegativeButton("Cancel", null);
        alertDialog.show();


    }

    public void remove(int position) throws JSONException {
        List<JSONObject> temp = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            temp.add(data.getJSONObject(i));
        }
        data = new JSONArray();
        temp.remove(position);
        for (int i = 0; i < temp.size(); i++) {
            data.put(temp.get(i));
        }

    }

    private void sendProductRemoveRequest(final int position) {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.toggleCartURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.getInt("response") == 1) {
                        remove(position);
                        mAdapter.notifyItemRemoved(position);
                        updateTotalPrice();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CartActivity.this, "Unknown Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                Toast.makeText(CartActivity.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                try {
                    params.put("customer_id", String.valueOf(AccountManager.getId(CartActivity.this)));
                    params.put("product_id", data.getJSONObject(position).getString("product_id"));

                    //to delete
                    params.put("status", "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getContactDisplayNameByNumber(String number) {


        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return name;
    }

    void openWhatsappContact(String designer_mobile) {
        /*Uri uri = Uri.parse("smsto:+918148172510");
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(i, ""));*/
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);

        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "I'm the body.");
        startActivity(Intent.createChooser(i, ""));

    }

    private void startProductDescription(JSONObject jsonObject) throws JSONException {
        Intent intent = new Intent(this, ProductDescriptionActivity.class);
        intent.putExtra("product_id", jsonObject.getInt("product_id"));
        startActivity(intent);

    }

    class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        QuantityChangeListener quantityChangeListener;
        Context context;
        LayoutInflater inflater;
        ProductRemovedListener productRemovedListener;


        public CartAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            data = new JSONArray();
        }

        public void setQuantityChangeListener(QuantityChangeListener quantityChangeListener) {
            this.quantityChangeListener = quantityChangeListener;
        }


        @Override
        public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CartViewHolder(inflater.inflate(R.layout.cart_card_view, parent, false));
        }

        @Override
        public void onBindViewHolder(CartViewHolder holder, int position) {
            try {
                holder.bind(data.getJSONObject(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setProductRemovedListener(ProductRemovedListener productRemovedListener) {
            this.productRemovedListener = productRemovedListener;
        }

        @Override
        public int getItemCount() {
            return data.length();
        }


        class CartViewHolder extends RecyclerView.ViewHolder {
            TextView name, price, offerPrice;
            ImageView image, remove;
            //   Spinner quantity;
            Button plus;
            Button minus;
            TextView qty, advance;
            //  boolean firstSelection;
            Button buyNow;
            View progressBar;

            public CartViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                price = (TextView) itemView.findViewById(R.id.price);
                image = (ImageView) itemView.findViewById(R.id.imageView);
                plus = (Button) itemView.findViewById(R.id.plusQty);
                minus = (Button) itemView.findViewById(R.id.minusQty);
                qty = (TextView) itemView.findViewById(R.id.qty);
                advance = (TextView) itemView.findViewById(R.id.advance);
                progressBar= itemView.findViewById(R.id.progress_bar);
                // quantity = (Spinner) itemView.findViewById(R.id.spinnerQuantity);
                offerPrice = (TextView) itemView.findViewById(R.id.offerPrice);
                remove = (ImageView) itemView.findViewById(R.id.remove);
                List<String> categories = new ArrayList<>();
                for (int i = 1; i <= 10; i++)
                    categories.add(String.valueOf(i));

                buyNow = (Button) itemView.findViewById(R.id.buyNow);
               /* ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categories);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
                // quantity.setAdapter(dataAdapter);
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (quantityChangeListener != null) {
                            quantityChangeListener.quantityChanged(getAdapterPosition(), 1);
                        }
                    }
                });
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (quantityChangeListener != null) {
                            quantityChangeListener.quantityChanged(getAdapterPosition(), -1);
                        }
                    }
                });

              /*  plus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (quantityChangeListener != null) {
                            quantityChangeListener.quantityChanged(getAdapterPosition(), position + 1);
                        }
                        firstSelection = false;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });*/
                buyNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.ChatNowURL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    product_name = data.getJSONObject(getLayoutPosition()).getString("product_name");
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getInt("response") == 1) {
                                     //   Toast.makeText(getApplicationContext(), getAdapterPosition() + "", Toast.LENGTH_SHORT).show();
                                        try {
                                            designer_mobile = jsonObject.getString("designer_mobile");
                                            name2 = getContactDisplayNameByNumber("designer_mobile");
                                            if (name2.length() != 0) ;
                                            else {
                                                String DesignerName = String.valueOf(app.Constants.designer_name);
                                                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                                                ops.add(ContentProviderOperation.newInsert(
                                                        ContactsContract.RawContacts.CONTENT_URI)
                                                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                                        .build());

                                                //------------------------------------------------------ Names
                                                if (DesignerName != null) {
                                                    ops.add(ContentProviderOperation.newInsert(
                                                            ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                                            .withValue(
                                                                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                                                    DesignerName).build());
                                                }

                                                //------------------------------------------------------ Mobile Number
                                                if (designer_mobile != null) {
                                                    ops.add(ContentProviderOperation.
                                                            newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, designer_mobile)
                                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                                            .build());
                                                }


                                                // Asking the Contact provider to create a new contact
                                                try {
                                                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            JSONObject object = data.getJSONObject(getAdapterPosition());
                                            product_id = object.getInt("product_id");
                                            if (name2.length() != 0)
                                                showAlert(name2);
                                            else
                                                showAlert(String.valueOf(app.Constants.designer_name));

                                            //    JSONObject object = data.getJSONObject(getAdapterPosition());
                                            //    product_id = object.getInt("product_id");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                        //  Toast.makeText(CartActivity.this,"This part of the app is under progress!",Toast.LENGTH_SHORT).show();
//                                        Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
//                                        contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
//
//                                        contactIntent
//                                                .putExtra(ContactsContract.Intents.Insert.NAME, "Designer")
//                                                .putExtra(ContactsContract.Intents.Insert.PHONE, String.valueOf(app.Constants.designer_id))
//                                        ;
//                                        startActivityForResult(contactIntent, 1);
                                        //                       openWhatsappContact(jsonObject.getInt("designer_mobile"));
                                        //                         String a = "8148172510";
                                        //                         openWhatsappContact(a);

//                                       Intent intent = new Intent(Intent.ACTION_SEND);
//                                       intent.setType("text/plain");
//                                       String one = String.valueOf(product_id);
//                                        intent.putExtra(Intent.EXTRA_TEXT, one);
//                                        intent.setPackage("com.whatsapp");
////                                        if(intent.resolveActivity(getPackageManager()) != null){
//                                           startActivity(intent);
////                                        }
//                                      //  Toast.makeText(context, "Successfully added in your cart", Toast.LENGTH_SHORT).show();
                                    } else {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
                                        alertDialog.setMessage("Designer is not available at present");
                                        alertDialog.show();
                                    }

                                    //     Toast.makeText(context, "Some unknown error occurred ", Toast.LENGTH_SHORT).show();
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
                                map.put("designer_id", String.valueOf(app.Constants.designer_id));
                                return map;
                            }
                        };
                        VolleySingleton.getInstance().getRequestQueue().add(request);


                    }
                });




                /*CardView card = (CardView)itemView.findViewById(R.id.card);
                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context,"Hi.."+getAdapterPosition(),Toast.LENGTH_SHORT).show();
                    }
                });*/

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (productRemovedListener != null) {
                            try {
                                productRemovedListener.onProductRemoved(getAdapterPosition());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            startProductDescription(data.getJSONObject(getLayoutPosition()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }

            private void showAlert(String name) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
                alertDialog.setMessage("Designer's contact no. has been added in your contact list named " + name + ".\n\nPress yes to chat on whatsapp.");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.setPackage("com.whatsapp");
                        String one = String.valueOf(product_id);
                        intent.putExtra(Intent.EXTRA_TEXT, "Hi! I am " + accountManager.getName(context) + "\nI wish to discuss with you about the product with ID " + one + " & name " + product_name);
                        if (intent.resolveActivity(getPackageManager()) != null)
                            startActivity(intent);
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }

            public void bind(JSONObject jsonObject) throws JSONException {
                //   firstSelection = true;
                progressBar.setVisibility(View.VISIBLE);
                qty.setText(jsonObject.getInt("qty") + "");
                if (jsonObject.getInt("qty") > 5) {
                    if (quantityChangeListener != null) {
                        quantityChangeListener.quantityChanged(getAdapterPosition(), 5 - jsonObject.getInt("qty"));
                    }
                }
                int priceValue = jsonObject.getInt("price") * jsonObject.getInt("qty");
                Double offerPriceValue = priceValue * (100 - jsonObject.getDouble("offer_percentage")) / 100;
                if (offerPriceValue == priceValue) {
                    price.setVisibility(View.INVISIBLE);
                    itemView.findViewById(R.id.linearLayout).setVisibility(View.INVISIBLE);
                } else {
                    price.setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                }
                Double advancePayment = offerPriceValue * (jsonObject.getInt("advance_amount_percentage")) / 100;
                if (advancePayment == offerPriceValue) {
                    advance.setVisibility(View.INVISIBLE);
                } else {
                    advance.setVisibility(View.VISIBLE);
                    advance.setText("Advance Payment: ₹" + advancePayment.intValue());
                }
                name.setText(jsonObject.getString("product_name"));
                price.setText("₹" + priceValue);
                offerPrice.setText("₹" + offerPriceValue.intValue());
                Picasso.with(context).load(jsonObject.getString("image1")).fit().centerInside().into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                        image.setImageResource(R.drawable.ic_error);
                    }
                });
                // quantity.setSelection(jsonObject.getInt("qty") - 1);
            }
        }
    }
}
