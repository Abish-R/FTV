package com.ftv_fashionshop.helixtech_android.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import accounts.AccountManager;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "INSTANCE ID";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        AccountManager.updateToken(refreshedToken, MyInstanceIDListenerService.this);
        AccountManager.sendToken(this);
        // TODO: Implement this method to send any registration to your app's servers.
    }


}