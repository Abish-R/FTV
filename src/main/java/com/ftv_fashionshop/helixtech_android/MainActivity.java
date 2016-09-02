package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import accounts.AccountManager;
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import de.hdodenhof.circleimageview.CircleImageView;
import helper.MyCustomProgressDialog;
import helper.ZoomOutPageTransformer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AccountManager.LogoutCallback {

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    private static final int STORAGE_PERMISSIONS = 12312;
    private static final int CONTACT_PERMISSIONS = 2323;
    ArrayList<String> productsNameList = new ArrayList<String>();
    ArrayList<String> productsIdList = new ArrayList<String>();
    AccountManager accountManager;
    ProgressDialog dialog;
    Toolbar toolbar;
    int theme;
    Menu menu;
    PopupWindow popupWindowDogs;
    boolean backPressed = false;
    ListView listViewDogs;
    ArrayAdapter<String> adapter;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    public static boolean deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {

        setTheme(ThemeSetter.getDesiredNoActionBarTheme(this));
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);



        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        accountManager = new AccountManager(this);
        accountManager.setLogoutCallback(this);

        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        initializeNavView();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new ZoomOutPageTransformer());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                Log.e("Density", "Low");
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                Log.e("Density", "Medium");
                break;
            case DisplayMetrics.DENSITY_HIGH:
                Log.e("Density", "HIGH");
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                Log.e("Density", "XHIGH");
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                Log.e("Density", "XXHIGH");
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                Log.e("Density", "XXXHIGH");
                break;
        }
        Log.e("Dimensions", metrics.widthPixels + " " + metrics.heightPixels);
        askForPermissions();
    }





    /**
     * Used to change the theme by dynamically selecting the theme from desginer's side and store it in local database.Further it also used the {@link #setTheme(int)} to set the theme dynamically by using the theme stored in the shared preference.
     *
     * @return theme stored in integer
     */
    private int getDesiredTheme() {
        SharedPreferences preferences = MainActivity.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = preferences.getInt("colour", -1);

        switch (pos) {
            case 0:
                theme = R.style.AppTheme_NoActionBar;
                break;
            case 1:
                theme = R.style.AppTheme2_NoActionBar;
                break;
            case 2:
                theme = R.style.AppTheme3_NoActionBar;
                break;
            case 3:
                theme = R.style.AppTheme4_NoActionBar;
                break;
            case 4:
                theme = R.style.AppTheme5_NoActionBar;
                break;
            case 5:
                theme = R.style.AppTheme6_NoActionBar;
                break;
            case 6:
                theme = R.style.AppTheme7_NoActionBar;
                break;

            case 7:
                theme = R.style.AppTheme8_NoActionBar;
                break;
            default:
                theme = R.style.AppTheme_NoActionBar;
                break;
        }
        return theme;

    }

    private void initializeNavView() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            }
        });
        final ImageView backgroundImage= (ImageView) headerView.findViewById(R.id.backgroundImage);
        if(accountManager.getCoverURL(this).isEmpty()){
            Picasso.with(MainActivity.this).load(R.drawable.nav_back).fit().centerCrop().into(backgroundImage);
        }
        else {
            Picasso.with(this).load(accountManager.getCoverURL(this)).fit().centerCrop().into(backgroundImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(MainActivity.this).load(R.drawable.nav_back).fit().centerCrop().into(backgroundImage);

                }
            });
        }

        TextView userName = (TextView) headerView.findViewById(R.id.userName);
        TextView email = (TextView) headerView.findViewById(R.id.email);
        CircleImageView profilePic = (CircleImageView) headerView.findViewById(R.id.profile_image);
        String imageUrl = AccountManager.getImageURL(this);
        if (!imageUrl.isEmpty()) {
            Picasso.with(this).load(imageUrl).into(profilePic);
        } else
            profilePic.setImageResource(R.drawable.account_circle);
        userName.setText(AccountManager.getName(this));
        email.setText(AccountManager.getEmail(this));
        getCartCount();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartNumber();
        getCartCount();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Constants.imageNumber = assignImageNumber(metrics.densityDpi);
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isMobile;
        if(activeNetwork==null){
            isMobile=true;
        }
        else
            isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        Log.e("Conectivity",isMobile+"");
        if(isMobile){
            Constants.imageNumberHigh=Constants.imageNumber;
        }
        else{
            Constants.imageNumberHigh=Constants.imageNumber+4;
        }
    }
    private int assignImageNumber(int densityDpi) {
        if (densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 0;
        } else if (densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 1;
        } else if (densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 2;
        } else
            return 3;
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    private void askForPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setMessage("Storage Permissions are Required For proper functioning of the app.");
            dialog.setPositiveButton("Ok",null);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setMessage("Contacts access Permissions are Required For proper functioning of the app.");
            dialog.setPositiveButton("Ok",null);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS}, CONTACT_PERMISSIONS);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                askForPermissions();
            } else {
                askForPermissions();
            }
        }
        if (requestCode == CONTACT_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                askForPermissions();
            }
        }
    }
    private void getCartCount() {
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.getCartCount, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    Constants.cartNumbers = object.getInt("data");
                    updateCartNumber();
                  /*  int colour = object.getInt("colour");
                    int reader = MainActivity.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).getInt("colour", -1);
                    if (colour != reader) {

                        SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
                        editor.putInt("colour", colour);
                        editor.commit();

                        restartActivity();

                    }
*/
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
                params.put("customer_id", String.valueOf(AccountManager.getId(MainActivity.this)));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void restartActivity() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(0, 0);
    }

    private void updateCartNumber() {
        if (menu == null) {
            return;
        }
        MenuItem item = menu.findItem(R.id.badge);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);
        TextView tv = (TextView) notifCount.findViewById(R.id.actionbar_notifcation_textview);
        tv.setText("" + Constants.cartNumbers);
        if (Constants.cartNumbers == 0) {
            tv.setVisibility(View.INVISIBLE);
        } else
            tv.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.badge);
        MenuItemCompat.setActionView(item, R.layout.badge_menu_item);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);
        TextView tv = (TextView) notifCount.findViewById(R.id.actionbar_notifcation_textview);
        tv.setText("0");
        tv.setVisibility(View.INVISIBLE);
        this.menu = menu;

        item.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constants.cartNumbers!=0)
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                else
                    Toast.makeText(MainActivity.this,"Cart is empty",Toast.LENGTH_SHORT).show();
            }
        });
        final MenuItem myActionMenuItem = menu.findItem(R.id.search_button);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //  Toast.makeText(getApplicationContext(),"Ok"+query,Toast.LENGTH_SHORT).show();
                //UserFeedback.show( "SearchOnQueryTextSubmit: " + query);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                if (s.length() != 0) {
                    getProductNames(s, searchView);
                    return false;
                }
                return false;
            }
        });

        return true;
    }

    public PopupWindow popupWindowDogs() {

        // initialize a pop up window type
        PopupWindow popupWindow = new PopupWindow(this);
        listViewDogs = new ListView(this);
        // popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        // set the list view as pop up window content
        popupWindow.setContentView(listViewDogs);
        return popupWindow;
    }

    private ArrayAdapter<String> dogsAdapter(List<String> lt) {
        //itemsStatus=new boolean[lt.size()];
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lt) {
            //CheckBox listItem1;
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                final String item = getItem(position);
                String[] itemArr = item.split("::");
                String text = itemArr[0];
                String id = itemArr[0];

                // visual settings for the list item
                final TextView listItem1 = new TextView(MainActivity.this);
                listItem1.setText(text);
                listItem1.setTag(id);
                listItem1.setTextSize(22);
                listItem1.setPadding(10, 10, 10, 10);
                listItem1.setTextColor(Color.WHITE);

                listItem1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int vv = Integer.parseInt(productsIdList.get(position).toString());
                        Intent intent = new Intent(getApplicationContext(), ProductDescriptionActivity.class);
                        intent.putExtra("product_id", vv);
                        startActivity(intent);
                    }
                });
                return listItem1;
            }
        };
        return adapter;
    }

    private void gh(View v) {
        popupWindowDogs = popupWindowDogs();
        popupWindowDogs.showAsDropDown(v, -5, 150);
        //popupWindowDogs.dismiss();
    }

    private void getProductNames(final String s, final View search) {
        if (s.length() != 0) {
            StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.
                    productSearchURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Log.e("Response", s);
                    //mSwipeRefreshLayout.setRefreshing(false);
                    try {
                        JSONObject object = new JSONObject(s);
                        JSONArray temp = object.getJSONArray("data");
                        if (temp.length() == 0) {
                            dialog.hide();
                        } else {
                            productsNameList.clear();
                            productsIdList.clear();
                            for (int i = 0; i < temp.length(); i++) {
                                productsNameList.add(temp.getJSONObject(i).getString("product_name"));
                                productsIdList.add(temp.getJSONObject(i).getString("product_id"));
                                //adapter.notifyDataSetChanged();
                            }
                            if (productsNameList.size() != 0) {
                                gh(search);
                                listViewDogs.setAdapter(dogsAdapter(productsNameList));
                            } else
                                Toast.makeText(getApplicationContext(), "Empty!", Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    dialog.hide();
                    //todo show onscreen tap
//                        Snackbar.make(findViewById(R.id.sear), "No internet connection", Snackbar.LENGTH_INDEFINITE)
//                                .setAction("RETRY", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        requestProducts();
//                                    }
//                                }).show();
////                Toast.makeText(MainActivity.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("designer_id", String.valueOf(Constants.designer_id));
                    params.put("pageno", "0");
                    params.put("product_name", s);
                    return params;

                }
            };
            VolleySingleton.getInstance().getRequestQueue().add(request);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.badge) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loggedOut() {
        dialog.hide();
        startActivity(new Intent(this, LoginControllerActivity.class));
        finish();
    }

    @Override
    public void logOutError(int error) {
        dialog.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
        try {
            trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                dialog.setMessage("Logging out...");
                dialog.show();
                deleteContact(this, accountManager.getDesignerMobile(this), String.valueOf(Constants.designer_name));
                accountManager.logout();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.order_history:
                startActivity(new Intent(this, MyOrders.class));
                break;
            case R.id.wish_list:
                startActivity(new Intent(this, Wishlist.class));
                break;
            case R.id.like_list:
                startActivity(new Intent(this, LikeList.class));
                break;
            case R.id.review:
                startActivity(new Intent(this, AllReview.class));
                break;
            case R.id.my_offers:
                startActivity(new Intent(this, MyOffers.class));
                break;


            default:
                Toast.makeText(this, "This part of the app is under progress", Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!backPressed) {
            backPressed = true;
            Toast.makeText(MainActivity.this, "Press Back again to exit!", Toast.LENGTH_SHORT).show();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    backPressed = false;
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(r, 2000);
        } else {
            super.onBackPressed();
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return LandingFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "What's New";
                case 1:
                    return "Ready To Wear";
                case 2:
                    return "Couture";
            }
            return null;
        }
    }
}
