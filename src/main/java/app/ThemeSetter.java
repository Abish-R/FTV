package app;

import android.content.Context;
import android.content.SharedPreferences;

import com.ftv_fashionshop.helixtech_android.R;

/**
 * Created by harsu on 01-07-2016.
 */
public class ThemeSetter {
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";

    /**
     * Used to change the theme by dynamically selecting the theme from desginer's side and store
     * it in local database.Further it also used the {@link #setTheme(int)} to set the theme
     * dynamically by using the theme stored in the shared preference.
     *
     * @return theme stored in integer
     */
    public static int getDesiredTheme(Context context) {
        SharedPreferences editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = editor.getInt("colour", -1);
        int theme;
        switch (pos) {
            case 0:
                theme = R.style.AppTheme;
                break;
            case 1:
                theme = R.style.AppTheme2;
                break;
            case 2:
                theme = R.style.AppTheme3;
                break;
            case 3:
                theme = R.style.AppTheme4;
                break;
            case 4:
                theme = R.style.AppTheme5;
                break;
            case 5:
                theme = R.style.AppTheme6;
                break;
            case 6:
                theme = R.style.AppTheme7;
                break;

            case 7:
                theme = R.style.AppTheme8;
                break;
            default:
                theme = R.style.AppTheme;
                break;
        }
        return theme;

    }

    public static int getDesiredNoActionBarTheme(Context context) {
        int theme;
        SharedPreferences editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = editor.getInt("colour", -1);

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
}
