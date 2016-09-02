package accounts;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.util.HashMap;
import java.util.Map;

import app.UrlConstants;
import io.fabric.sdk.android.Fabric;

/**
 * Created by harsu on 22-06-2016.
 */
public class TwitterManager {
    private static final String TWITTER_KEY = "Xi5ZQE6v8GBMNyLUZI5wKRkhM";
    private static final String TWITTER_SECRET = "szNBTCOmCFTsvz5BoYEJsFUCEosua9YCnvZQaAdg8uAtDi5F7P";
    //https://support.twitter.com/forms/platform
//    private static final String TWITTER_KEY = "F6ymjVv2PghxxUMMvAgxKmkfR";
//    private static final String TWITTER_SECRET = "BhUF4xzV7e4zZb7ewJlNiHbMgKM0rZeYlnTfzhM6jOMsMW9z5W";
    Context context;
    AccountManager accountManager;
    View twitterButton;
    TwitterAuthClient client;

    public TwitterManager(Context context, AccountManager accountManager) {
        this.context = context;
        this.accountManager = accountManager;
        if (accountManager == null)
            throw new IllegalArgumentException("AccountManager cannot be null");
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(context, new Twitter(authConfig));

    }

    public static void ClearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public void setSignInButton(View v) {
        client = new TwitterAuthClient();
        twitterButton = v;
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.authorize((Activity) context, new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        final TwitterSession session = result.data;
                        requestEmailTwitter(session);

                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("TwitterKit", "Login with Twitter failure", exception);
                    }
                });
            }
        });

    }

    private void requestEmailTwitter(final TwitterSession session) {
        TwitterAuthClient authClient = new TwitterAuthClient();
        authClient.requestEmail(session, new Callback<String>() {
            @Override
            public void success(final Result<String> email) {
                // Do something with the result, which provides the email address
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
                twitterApiClient.getAccountService().verifyCredentials(false, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        Log.e("Response", result.toString());
                        if (accountManager.signInCallback != null) {
                            accountManager.signInCallback.sendingDataToServer();
                        }
                        twitterSignIn(result, email.data);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        if (accountManager.signInCallback != null)
                            accountManager.signInCallback.signInError(AccountManager.Error.NO_INTERNET_CONNECTION);
                    }
                });

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
//                Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Email required to continue or use some other Account to sign In")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                requestEmailTwitter(session);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        client.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * After successful twitter Sign In the function is called to store data, and update respective value.
     *
     * @param result the Result<User> object returned after requesting from twitter API Client
     * @param email  the email returned .
     */
    public void twitterSignIn(Result<User> result, String email) {
        Map<String, String> data = new HashMap<>();
        data.put("first_name", result.data.name);
        data.put("email", email);
        data.put("profilepic", result.data.profileImageUrl);
        data.put("user_name", email);
        accountManager.sendDataToServer(data, UrlConstants.TwitterSignUpURL, SocialProfileClasses.SocialAccount.TWITTER);
    }

    public void logout() {
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            ClearCookies(context.getApplicationContext());
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
        accountManager.clearData();
        if (accountManager.logoutCallback != null) {
            accountManager.logoutCallback.loggedOut();
        }
    }
}
