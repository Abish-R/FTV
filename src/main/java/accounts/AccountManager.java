package accounts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;

/**
 * Created by admin on 01-06-2016.
 */


public class AccountManager {

    private static final String MY_PREFS_NAME = "USER";
    private static final String current_Sign_In = "current_sign_in";
    String name2;
    Context context;

    SignInCallback signInCallback;
    LogoutCallback logoutCallback;

    public AccountManager(Context context) {
        this.context = context;

    }

    /**
     * @param context
     * @return Profile Pic Image URL of the signed in User.
     */
    public static String getImageURL(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("profile_pic", "");
    }

    /**
     * @param context
     * @return Name of the user signed in.
     */
    public static String getName(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("first_name", "") + " " + userDetails.getString("last_name", "");
    }

    /**
     * @param context
     * @return Email of the user signed in
     * Default returns an empty string
     */
    public static String getEmail(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("email", "");
    }

    /**
     * @param context
     * @return number of the user used to signed in. (String)
     * Default returns an empty String
     */
    public static String getNumber(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("mobile", "");
    }
    public static String getUserName(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("user_name", " ");
    }

    /**
     * @param context
     * @return the complete address separated by newLines. (street, city, state, country, pincode).
     * @see #getAddressComponents(Context)
     */
    public static String getAddress(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        String street = userDetails.getString("street", "");
        String city = userDetails.getString("city", "");
        String state = userDetails.getString("state", "");
        String country = userDetails.getString("country", "");
        String pincode = userDetails.getString("pincode", "");
        if (street.isEmpty()) {
            return "";
        }
        return street + "\n" + city + "\n" + state + "\n" + country + "\n" + pincode;
    }
	public void  storeCoverPic(String cover) throws JSONException{
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();

        editor.putString("cover",cover);
        editor.commit();

    }
    public static String getCoverURL(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("cover", "");
    }

    /**
     * @param context
     * @return mapped address components. The components being
     * 1. street
     * 2. city
     * 3. state
     * 4. country
     * 5. pincode
     * @see #getAddress(Context)
     */
    public static Map<String, String> getAddressComponents(Context context) {
        Map<String, String> address = new HashMap<>();
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        address.put("street", userDetails.getString("street", ""));
        address.put("city", userDetails.getString("city", ""));
        address.put("state", userDetails.getString("state", ""));
        address.put("country", userDetails.getString("country", ""));
        address.put("pincode", userDetails.getString("pincode", ""));
        return address;
    }

    /**
     * @param context
     * @return customer ID of the signed in user
     * Default as -1.
     */
    public static int getId(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getInt("id", -1);

    }

    /**
     * @param context
     * @return The mobile number of the designer.
     */
    public static String getDesignerMobile(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("designer_mobile", "9999999999");
    }

    /**
     * Used to store the new token in the local database.
     * It tries to send the new token to server automatically by calling {@link #sendToken(Context)}
     *
     * @param refreshedToken the new device token
     * @param context
     * @see #sendToken(Context)
     */

    public static void updateToken(String refreshedToken, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("Token", refreshedToken);
        editor.putBoolean("TokenSent", false);
        editor.apply();
    }

    /**
     * It updates the local database as the token has been successfully updated to the server.
     * Hence saving redundant calls later.
     *
     * @param context
     */
    private static void updateSentSuccessfully(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean("TokenSent", true);
        editor.apply();
    }

    /**
     * To update the token in the server. It sends the update request only if the token had not already be sent to the server.
     *
     * @param context
     * @see #sentSuccessFully(Context)
     * @see #updateSentSuccessfully(Context)
     */
    public static void sendToken(final Context context) {
        if (AccountManager.sentSuccessFully(context))
            return;
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.tokenServerUpdate, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getInt("response") == 1) {
                        AccountManager.updateSentSuccessfully(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                if (AccountManager.getId(context) == -1) {
                    return null;
                }
                if (AccountManager.getToken(context).isEmpty()) {
                    return null;
                }
                params.put("customer_id", String.valueOf(AccountManager.getId(context)));
                params.put("device_token_id", AccountManager.getToken(context));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);


    }

    /**
     * @param context
     * @return true if token has been successfully updated in the server
     */
    private static boolean sentSuccessFully(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getBoolean("TokenSent", false);

    }

    /**
     * @param context
     * @return device token
     */
    private static String getToken(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return userDetails.getString("Token", "");
    }

    /**
     * update the name in the local database
     *
     * @param s
     * @param context
     */
    public static void updateName(String s, Context context) {

        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("first_name", s);
        editor.putString("last_name", "");
        editor.commit();
    }

    /**
     * update email in the local database
     *
     * @param s
     * @param context
     */
    public static void updateMail(String s, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("email", s);
        editor.commit();
    }

    /**
     * update number in the local database
     *
     * @param s
     * @param context
     */
    public static void updateNumber(String s, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("mobile", s);
        editor.commit();
    }

    /**
     * Update Addess in the local database
     *
     * @param stname
     * @param city
     * @param country
     * @param pin
     * @param state
     * @param context
     */
    public static void updateAddress(String stname, String city, String country, String pin, String state, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("street", stname);
        editor.putString("city", city);
        editor.putString("country", country);
        editor.putString("pincode", pin);
        editor.putString("state", state);

        editor.commit();
    }

    /**
     * Set the {@link SignInCallback}
     *
     * @param signInCallback
     */
    public void setSignInCallback(SignInCallback signInCallback) {
        this.signInCallback = signInCallback;
    }

    /**
     * Set the {@link LogoutCallback}
     *
     * @param logoutCallback
     */
    public void setLogoutCallback(LogoutCallback logoutCallback) {
        this.logoutCallback = logoutCallback;
    }

    /**
     * @return If user is not signed in then {@link SocialProfileClasses.SocialAccount#NOT_SIGNED_IN} is returned.
     * If user is signed in but address is not specified then {@link Error#NO_ADDRESS} is returned.
     */
    public boolean isSignedIn() {
        if (getCurrentSignIn(context) == SocialProfileClasses.SocialAccount.NOT_SIGNED_IN) {
//            return SocialProfileClasses.SocialAccount.NOT_SIGNED_IN;
            return false;
        } else if (getCity().isEmpty()) {
//            return Error.NO_ADDRESS;
            return false;

        } else
//            return getCurrentSignIn();
            return true;
    }

    /**
     * To check whether city is stored in the local database
     *
     * @return city from local database
     */
    public String getCity() {
        SharedPreferences editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return editor.getString("city", "");

    }


    /**
     * To get the current social account signed in
     *
     * @return
     */
    public static int getCurrentSignIn(Context context) {

        SharedPreferences userDetails = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int aa = userDetails.getInt(current_Sign_In, SocialProfileClasses.SocialAccount.NOT_SIGNED_IN);
        return userDetails.getInt(current_Sign_In, SocialProfileClasses.SocialAccount.NOT_SIGNED_IN);
    }


    /**
     * Send data to the server by Post method.
     *
     * @param data    the param Map
     * @param url     to which post request is to be made
     * @param account which account is being used for Sign In. {@link SocialProfileClasses.SocialAccount#FACEBOOK},
     *                {@link SocialProfileClasses.SocialAccount#GOOGLE},
     *                {@link SocialProfileClasses.SocialAccount#TWITTER},
     *                {@link SocialProfileClasses.SocialAccount#NORMAL}
     */
    public void sendDataToServer(final Map<String, String> data, String url, final int account) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.e("Response", s);
                        try {
                            JSONObject object = new JSONObject(s);
                            if (object.getInt("response") == 1) {   //successful sign up/in
                                storeData(object, account);
                            } else {
                                if (signInCallback != null)
                                    signInCallback.signInError(Error.UNKNOWN_ERROR);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            signInCallback.signInError(Error.UNKNOWN_ERROR);
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (signInCallback != null)
                    signInCallback.signInError(Error.NO_INTERNET_CONNECTION);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                data.put("designer_id", String.valueOf(Constants.designer_id));
                return data;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    /**
     * Store the data once returned by the server.
     *
     * @param object  in response
     * @param account used to sign in.
     * @throws JSONException when one among the list of items is not present.
     */
    private void storeData(JSONObject object, int account) throws JSONException {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(current_Sign_In, account);
        editor.putInt("id", object.getInt("customer_id"));
        editor.putString("first_name", object.getString("first_name"));
        editor.putString("last_name", object.getString("last_name"));
//
        editor.putString("user_name", object.getString("user_name"));
        editor.putString("email", object.getString("email"));
        editor.putString("mobile", object.getString("mobile").equalsIgnoreCase("0") ? "" : object.getString("mobile"));
        editor.putString("gender", object.getString("gender"));
        editor.putString("dob", object.getString("dob"));
        editor.putString("street", object.getString("street"));
        editor.putString("city", object.getString("city"));
        editor.putString("state", object.getString("state"));
        editor.putString("country", object.getString("country"));
        editor.putString("pincode", object.getString("pincode"));
        editor.putString("profile_pic", object.getString("profilepic"));
        editor.commit();
        sendToken(context);
        if (object.getString("street").isEmpty()) {
            if (signInCallback != null) {
                signInCallback.signInError(Error.NO_ADDRESS);
            }
        } else if (signInCallback != null)
            signInCallback.signedInSuccessfully();
    }

    public static void saveImageChanged(Context context,String url){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("profile_pic", url);
        editor.commit();
    }

    /**
     * clears user's the local database
     */
    public void clearData() {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(current_Sign_In);
        editor.remove("id");
        editor.remove("first_name");
        editor.remove("last_name");
        editor.remove("user_name");
        editor.remove("email");
        editor.remove("mobile");
        editor.remove("gender");
        editor.remove("dob");
        editor.remove("street");
        editor.remove("city");
        editor.remove("state");
        editor.remove("country");
        editor.remove("pincode");
        editor.remove("profile_pic");
        editor.remove("TokenSent");
        editor.remove("cover");
        editor.commit();
    }

    /**
     * Logout the account using which it was signed in.
     */
    public void logout() {

        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.logout, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (getCurrentSignIn(context) == SocialProfileClasses.SocialAccount.NORMAL) {
                    clearData();
                    if (logoutCallback != null) {
                        logoutCallback.loggedOut();
                    }
                } else if (getCurrentSignIn(context) == SocialProfileClasses.SocialAccount.NOT_SIGNED_IN) {
                    //not signed in
                    if (logoutCallback != null) {
                        logoutCallback.logOutError(Error.NOT_SIGNED_IN);
                    }
                } else {
                    SocialProfileClasses socialProfileClasses = new SocialProfileClasses(AccountManager.this, context);
                    socialProfileClasses.logout(getCurrentSignIn(context));

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(logoutCallback!=null){
                    logoutCallback.logOutError(Error.NO_INTERNET_CONNECTION);
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("customer_id", String.valueOf(AccountManager.getId(context)));

                return map;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);



    }


   /* public void startTracking() {
        if (getCurrentSignIn() == SocialAccount.FACEBOOK) {

            facebookManager = new FacebookManager(this, context);
            facebookManager.startTracking();
        }
    }

    public void stopTracking() {
        if (getCurrentSignIn() == SocialAccount.FACEBOOK) {
            facebookManager.stopTracking();
        }
    }*/

    public void normalSignIn(final String username, final String password) {
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.signInURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    int response = object.getInt("response");
                    if (response == 1) {
                        String DesignerName = String.valueOf(Constants.designer_name);
                        String designer_mobile = getDesignerMobile(context);
                        name2 = getContactDisplayNameByNumber(designer_mobile);
                        if (name2.length() != 0) ;
                        else {
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

                            //------------------------------------------------------ Home Numbers
//                                        if (HomeNumber != null) {
//                                            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                                                    .withValue(ContactsContract.Data.MIMETYPE,
//                                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, HomeNumber)
//                                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
//                                                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
//                                                    .build());
//                                        }

                            //------------------------------------------------------ Work Numbers
//                                        if (WorkNumber != null) {
//                                            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                                                    .withValue(ContactsContract.Data.MIMETYPE,
//                                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, WorkNumber)
//                                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
//                                                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
//                                                    .build());
//                                        }
//
//                                        //------------------------------------------------------ Email
//                                        if (emailID != null) {
//                                            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                                                    .withValue(ContactsContract.Data.MIMETYPE,
//                                                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
//                                                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
//                                                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
//                                                    .build());
//                                        }
//
//                                        //------------------------------------------------------ Organization
//                                        if (!company.equals("") && !jobTitle.equals("")) {
//                                            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                                                    .withValue(ContactsContract.Data.MIMETYPE,
//                                                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
//                                                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
//                                                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
//                                                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
//                                                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
//                                                    .build());
//                                        }

                            // Asking the Contact provider to create a new contact
                            try {
                                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                            } catch (Exception e) {
                                e.printStackTrace();
                                signInCallback.signInError(Error.UNKNOWN_ERROR);
                                Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        putDesignerMobile(object.getString("designer mobile"));
                        storeData(object, SocialProfileClasses.SocialAccount.NORMAL);
                    } else if (signInCallback != null)
                        signInCallback.signInError(Error.USERNAME_PASSWORD_MISMATCH);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (signInCallback != null)
                        signInCallback.signInError(Error.UNKNOWN_ERROR);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (signInCallback != null)
                    signInCallback.signInError(Error.NO_INTERNET_CONNECTION);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("user_name", username);
                params.put("password", password);
                params.put("designer_id", String.valueOf(Constants.designer_id));

                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);

    }

    private void putDesignerMobile(String string) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("designer_mobile", string);
        editor.commit();
    }

    private String getContactDisplayNameByNumber(String number) {


        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "";

        ContentResolver contentResolver = context.getContentResolver();
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

    /**
     * The interface to listen to logout events
     */
    public interface LogoutCallback {
        /**
         * Called when logout is successfull
         */
        void loggedOut();

        /**
         * @param error
         */
        void logOutError(int error);
    }

    /*
    This callback can be used to check if the user data was successfully sent to the server, and retrieved
    */
    public interface SignInCallback {

        /**
         * called when data is received by the social profile and is being updated to the server.
         * We can show ProgressBar here.
         */
        void sendingDataToServer();

        /**
         * Called when data is successfully sent to the server and updated in the database.
         */
        void signedInSuccessfully();

        /**
         * Called when an Error has occured in Sign in.
         *
         * @param error one in {@link Error}
         */
        void signInError(int error);
    }

    /**
     * The errors that {@link AccountManager}can understand.
     */
    public class Error {
        public static final int UNKNOWN_ERROR = -1;
        public final static int NO_INTERNET_CONNECTION = 0;
        public final static int INVALID_USERNAME = 1;
        public final static int USERNAME_PASSWORD_MISMATCH = 2;
        public static final int NOT_SIGNED_IN = 3;
        public static final int NO_ADDRESS = 4;
    }


}
