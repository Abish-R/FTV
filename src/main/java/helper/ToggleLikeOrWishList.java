package helper;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import accounts.AccountManager;
import app.UrlConstants;
import app.VolleySingleton;


/**
 * Created by admin on 08-06-2016.
 */
public class ToggleLikeOrWishList {
    public static final int LIKE = 1;
    public static final int WISH_LIST = 2;


    public static void toggle(final Context context, final int which, final int position, final JSONObject object, final ToggleListener toggleListener) {

        String url = "";
        if (which == LIKE) {
            url = UrlConstants.toggleLike;
        } else
            url = UrlConstants.toggleWishList;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject temp = new JSONObject(s);


                    if (temp.getInt("response") == 1) {
                        toggleListener.toggled(which, temp.getInt("status"), position);
                    }
                    else
                        toggleListener.toggleFailure(position,which);

                } catch (JSONException e) {
                    toggleListener.toggleFailure(position,which);
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                toggleListener.toggleFailure(position, which);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                try {
                    params.put("product_id", object.getString("product_id"));
                    params.put("customer_id", String.valueOf(AccountManager.getId(context)));


                    if (which == LIKE) {
                        int likeStatus = object.getInt("likelist_status");
                        params.put("status",likeStatus == 1 ? "0" : "1");
                    } else {
                        int wishlistStatus = object.getInt("wishlist_status");
                        params.put("status",wishlistStatus == 1 ? "0" : "1");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    public interface ToggleListener {
        public void toggled(int which, int newStatus, int position);

        public void toggleFailure(int position, int which);
    }
}

