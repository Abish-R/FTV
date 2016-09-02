package accounts;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

/**
 * Created by harsu on 22-06-2016.
 *
 * It manages instances of all the Social manager classes so if you don't want to use a Social account,
 * you can delete al its occurrence from this class.
 * It is used together with {@link AccountManager}
 *
 * @see AccountManager
 * @see GoogleManager
 * @see  TwitterManager
 * @see  FacebookManager
 */
public class SocialProfileClasses {
    Context context;
    AccountManager accountManager;

    /**The constructor to create the object for {@link SocialProfileClasses}
     * @param accountManager and instance of {@link AccountManager}
     * @param context
     */
    public SocialProfileClasses(AccountManager accountManager, Context context) {
        this.accountManager = accountManager;
        this.context = context;
    }

    /** to logout the desired {@link SocialAccount}
     * It differentiates and calls the respective Class logout functions.
     * @param currentSignIn
     */
    public void logout(int currentSignIn) {
        if (currentSignIn == SocialAccount.GOOGLE) {
            GoogleManager googleManager = new GoogleManager(accountManager, (FragmentActivity) context);
            googleManager.logout();
        } else if (currentSignIn == SocialAccount.TWITTER) {
            TwitterManager twitterManager = new TwitterManager(context, accountManager);
            twitterManager.logout();

        } else if (currentSignIn == SocialAccount.FACEBOOK) {
            FacebookManager facebookManager = new FacebookManager(accountManager, context);
            facebookManager.logout();
        }
    }


    /**
     * Stores the account using which Sign in has been done.
     */
    public interface SocialAccount {
        int NOT_SIGNED_IN = 0;
        int NORMAL = 1;
        int GOOGLE = 2;
        int FACEBOOK = 3;
        int TWITTER = 4;
    }
}
