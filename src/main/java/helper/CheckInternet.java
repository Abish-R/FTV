package helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by HARSH GOYNKA on 6/6/2016.
 */
public class CheckInternet {
Context c;
    public CheckInternet(Context context){
        c=context;
    }

    public int internetCheck() {
        int a=0;
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                a=1;
                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                    a=1;
            return 1;
        } else{
            a=0;
        return 0;

    }

}}
