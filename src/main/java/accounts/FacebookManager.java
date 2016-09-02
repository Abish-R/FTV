package accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.UrlConstants;

/**
 * Created by harsu on 22-06-2016.
 *
 * This class is used to maintain FacebookLogin. It is to be used only with {@link AccountManager} and {@link SocialProfileClasses}.
 * Then it sends the data to the server using {@link AccountManager#sendDataToServer(Map, String, int)}.
 *
 * 1. create an instance of the class using {@link #FacebookManager(AccountManager, Context)}
 * 2. Set any view to invoke the login action on its click using {@link #setLoginButton(View, Activity, Collection)}.
 * 3. Invoke the function {@link #onActivityResult(int, int, Intent)} from the onActivity result of the SignInActivity in which these classes are being used.
 * 4. listen to callback from {@link AccountManager.SignInCallback} on success or failure
 * @see  AccountManager#setSignInCallback(AccountManager.SignInCallback)
 */
public class
FacebookManager {
    Context context;
    AccountManager accountManager;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    ProfileTracker profileTracker;

    /**
     * The constructor to invoke FacebookManager. It creates a LoginManager object and maintains all callbacks.
     *
     * @param accountManager needs an {@link AccountManager} object to refer to its callbacks and call its functions. This should be the same instance that is getting used up in your SignInActivity
     * @param context
     * @see #setLoginButton(View, Activity, Collection)
     * @see #onActivityResult(int, int, Intent)
     */
    public FacebookManager(final AccountManager accountManager, Context context) {
        this.accountManager = accountManager;
        this.context = context;
        if (accountManager == null)
            throw new IllegalArgumentException("AccountManager cannot be null");
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (accountManager.signInCallback != null)
                    accountManager.signInCallback.sendingDataToServer();
                facebookSignIn(loginResult);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                if (accountManager.signInCallback != null)
                    accountManager.signInCallback.signInError(AccountManager.Error.NO_INTERNET_CONNECTION);
            }
        });

    }

    /**
     * To pass the onActivityResult callBack from SignInActivity, so that it can be handled by CallBackManager.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * The view passed in this function will invoke FacebookLogin onClick. All the properties are handled by the function and {@link FacebookManager}
     * Don't forget to call {@link #onActivityResult(int, int, Intent)}
     *
     * @param view        on which FacebookLogin will get invoked when clicked.
     * @param activity    which will receive the onActivityResult callBack.
     * @param permissions the List of permissions.
     * @see #onActivityResult(int, int, Intent)
     */
    public void setLoginButton(View view, final Activity activity, final Collection<String> permissions) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
            }
        });
    }

    /**
     * It requests the required data from Facebook Graph API and stores them in a map to be sent
     * to the server. It uses {@link AccountManager#sendDataToServer(Map, String, int)} to send the
     * data to the server.
     * It is called after a successful sign in from LoginManagerCallback success.
     *
     * @param loginResult
     */
    private void facebookSignIn(LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.i("LoginActivity", response.toString());
                // Get facebook data from login
                String formattedDate = "";
                try {
                    String birthday = object.getString("birthday");
 String cover=object.getJSONObject("cover").getString("source");

                    DateFormat originalFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                    DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = originalFormat.parse(birthday);
                    formattedDate = targetFormat.format(date);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("first_name", object.getString("name"));
                    params.put("last_name",/* object.getString("last_name")*/"");
                    params.put("email", object.getString("email"));
                    params.put("gender", object.getString("gender"));
                    params.put("user_name", object.getString("email"));
                    params.put("profilepic", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                 params.put("coverpic", object.getJSONObject("cover").getString("source"));

                    params.put("dob", formattedDate);
                    String cover=object.getJSONObject("cover").getString("source");
                    accountManager.storeCoverPic(cover);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                accountManager.sendDataToServer(params, UrlConstants.GoogleSignUpURL, SocialProfileClasses.SocialAccount.FACEBOOK);


            }

        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, email,gender,birthday,picture.type(large),cover ");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * Logs out the facebook account by using LoginManager.
     * Provides a Logout success or failure callback to the MainClass.
     *
     * @see AccountManager.LogoutCallback
     */
    public void logout() {
        LoginManager.getInstance().logOut();
        accountManager.clearData();
        if (accountManager.logoutCallback != null) {
            accountManager.logoutCallback.loggedOut();
        }
    }

    /**
     * This function can be called to start tracking the profile change or access token change.
     * To call this function it must be explicitly checked if the signedInAccount is Facebook.
     *
     * @see #stopTracking()
     * @see AccountManager#getCurrentSignIn()
     * @see SocialProfileClasses.SocialAccount#FACEBOOK
     */
    public void startTracking() {
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                AccessToken.setCurrentAccessToken(currentAccessToken);
            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                Profile.setCurrentProfile(currentProfile);
            }
        };
        accessTokenTracker.startTracking();
    }


    /**
     * This function can be called to stop tracking the profile change or access token change.
     * It should be called only if the activity has called {@link #startTracking()}
     * To call this function it must be explicitly checked if the signedInAccount is Facebook.
     *
     * @see #startTracking()
     * @see AccountManager#getCurrentSignIn()
     * @see SocialProfileClasses.SocialAccount#FACEBOOK
     */
    public void stopTracking() {
        if (accessTokenTracker != null && profileTracker != null)
            accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }
}
