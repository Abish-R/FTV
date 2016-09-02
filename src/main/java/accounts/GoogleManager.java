package accounts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.HashMap;
import java.util.Map;

import app.UrlConstants;

/**
 * Created by harsu on 22-06-2016.
 *
 * This class is used to maintain GoogleSignIn. It takes care of invoking required objects. And
 * passes the data to server using {@link AccountManager#sendDataToServer(Map, String, int)} on successful Sign In.
 *
 * 1. Create an Instance of the class using {@link #GoogleManager(AccountManager, FragmentActivity)}
 * 2. set up the Google Sign in button using {@link #googleSignIn(GoogleSignInAccount, Person)}
 * 3. pass the result from onActivityResult using {@link #onActivityResult(int, int, Intent)}
 * 4. listen to callback from {@link AccountManager.SignInCallback} on success or failure
 * @see  AccountManager#setSignInCallback(AccountManager.SignInCallback)
 */
public class GoogleManager implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    Context context;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    SignInButton signInButton;
    FragmentActivity activity;
    AccountManager accountManager;

    /**Constructor used to invoke the class. It Builds the {@link GoogleSignInOptions} required for further login.
     * @param accountManager the instance of {@link AccountManager} class that is being used in the SignInActivity.
     * @param activity the activity in which Google login button is present
     *
     * @see #setSignInButton(View, int)
     * @see #onActivityResult(int, int, Intent)
     */
    public GoogleManager(AccountManager accountManager, FragmentActivity activity) {
        this.context = activity;
        this.activity=activity;
        this.accountManager = accountManager;
        if(accountManager==null)
            throw new IllegalArgumentException("AccountManager cannot be null");
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (accountManager.signInCallback != null)
            accountManager.signInCallback.signInError(AccountManager.Error.NO_INTERNET_CONNECTION);
    }

    public void setSignInButton(View view, final int GOOGLE_SIGN_IN) {

        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .enableAutoManage(activity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        signInButton = (SignInButton) view;

        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });
    }


    /**To be invoked onActivityResult only if the requestCode matches.
     * @param requestCode
     * @param resultCode
     * @param data
     *
     * @see #setSignInButton(View, int)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        handleSignInResult(result);
    }

    /**It handles the result after successful sign in is done. It then passes the information to {@link #googleSignIn(GoogleSignInAccount, Person)}
     * for further processing.
     * called from {@link #onActivityResult(int, int, Intent)}
     * @param result
     */
    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            if (accountManager.signInCallback != null)
                accountManager.signInCallback.sendingDataToServer();
            GoogleSignInAccount acct = result.getSignInAccount();
            Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            googleSignIn(acct, person);
        } else {
            // Signed out, show unauthenticated UI.

        }
    }

    /**
     * After successful google Sign In the function is called to store data, and update respective value.
     * It uses {@link AccountManager#sendDataToServer(Map, String, int)} to send the required fields by creating a map.
     *
     * @param account the GoogleSignInAccount object returned after successful Google Sign In
     * @param person  the Person obtained by the Google+ sign in
     */

    public void googleSignIn(GoogleSignInAccount account, Person person) {
        Map<String, String> data = new HashMap<>();
        if(person==null) {
            data.put("first_name", account.getDisplayName());
            data.put("user_name", account.getEmail());
            data.put("email", account.getEmail());
            data.put("gender", "null");
            data.put("profilepic", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
        }else{
            data.put("first_name", person.getDisplayName());
            data.put("user_name", account.getEmail());
            data.put("email", account.getEmail());
            data.put("gender", assignGender(person.getGender()));
            data.put("profilepic", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
        }

        accountManager.sendDataToServer(data, UrlConstants.GoogleSignUpURL, SocialProfileClasses.SocialAccount.GOOGLE);
    }// 8007820860

    /**used to assign a String gender from {@link Person.Gender}
     * @param gender
     * @return
     */
    private String assignGender(int gender) {
        if (gender == Person.Gender.MALE) {
            return "male";
        } else if (gender == Person.Gender.FEMALE) {
            return "female";
        } else
            return "other";

    }

    /**Used to logout the Google Account.
     *
     */
    public void logout() {
        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(Plus.API)
                .build();
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        accountManager.clearData();
                        if (accountManager.logoutCallback != null) {
                            accountManager.logoutCallback.loggedOut();
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (accountManager.logoutCallback != null)
            accountManager.logoutCallback.logOutError(AccountManager.Error.NO_INTERNET_CONNECTION);
    }
}
