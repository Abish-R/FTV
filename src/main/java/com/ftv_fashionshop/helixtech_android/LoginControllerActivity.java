package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Arrays;

import accounts.AccountManager;
import accounts.FacebookManager;
import accounts.GoogleManager;
import accounts.SocialProfileClasses;
import accounts.TwitterManager;
import app.Constants;
import app.ThemeSetter;
import helper.MyCustomProgressDialog;

import static accounts.AccountManager.*;

public class LoginControllerActivity extends AppCompatActivity implements SignInCallback {


    private static final int GOOGLE_SIGN_IN = 29120;
    AccountManager accountManager;
    ProgressDialog dialog;
    EditText usernameEditText;
    EditText passEditText;
    FacebookManager facebookManager;
    GoogleManager googleManager;
    TwitterManager twitterManager;
    private ImageButton twitterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredNoActionBarTheme(this));
        super.onCreate(savedInstanceState);
        accountManager = new AccountManager(this);
        checkSignIn();
        facebookManager = new FacebookManager(accountManager, this);

        twitterManager = new TwitterManager(this, accountManager);

        setContentView(R.layout.activity_login_controller);


        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        //FB LOGIN initialization
        facebookManager.setLoginButton(findViewById(R.id.facebook_login_button), this, Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));


        //GOOGLE LOGIN INITIALIZATION

        googleManager = new GoogleManager(accountManager, this);
        googleManager.setSignInButton(findViewById(R.id.google_sign_in_button), GOOGLE_SIGN_IN);


        //TWITTER LOGIN INITIALIZATION

        twitterManager.setSignInButton(findViewById(R.id.twitter_login_button));


        accountManager.setSignInCallback(this);


    }

    public void goToLoginPage(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToSignUpPage(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();


    }


    private void checkSignIn() {

        if(accountManager.getCurrentSignIn(this)== SocialProfileClasses.SocialAccount.NOT_SIGNED_IN){
            return;
        }
        else{
            if(accountManager.getCity().isEmpty()){
                Intent act2 = new Intent(LoginControllerActivity.this, AndroidGPSTrackingActivity.class);
                act2.putExtra("calling-activity", Constants.ACTIVITY_2);
                // or ActivityConstants.ACTIVITY_3 if called form Activity3
                startActivity(act2);
                finish();
            }
            else{
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            googleManager.onActivityResult(requestCode, resultCode, data);

        } else {//twitter and facebook
            twitterManager.onActivityResult(requestCode, resultCode, data);
            facebookManager.onActivityResult(requestCode, resultCode, data);
//            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void sendingDataToServer() {
        dialog.show();
    }

    @Override
    public void signedInSuccessfully() {
        dialog.hide();
        checkSignIn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public void signInError(int error) {
        dialog.dismiss();
        if (error == AccountManager.Error.NO_INTERNET_CONNECTION) {
            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.UNKNOWN_ERROR) {
            Toast.makeText(this, "Unknown Error Occurred! Please Retry", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.USERNAME_PASSWORD_MISMATCH) {
            Toast.makeText(this, "Please check your credentials and try again", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.INVALID_USERNAME) {
            Toast.makeText(this, "invalid username", Toast.LENGTH_SHORT).show();
        } else if (error == AccountManager.Error.NO_ADDRESS) {

            Toast.makeText(this, "Please provide the address", Toast.LENGTH_SHORT).show();
            Intent act2 = new Intent(LoginControllerActivity.this, AndroidGPSTrackingActivity.class);
            act2.putExtra("calling-activity", Constants.ACTIVITY_2);
            // or ActivityConstants.ACTIVITY_3 if called form Activity3
            startActivity(act2);
            finish();


        }
    }


}

