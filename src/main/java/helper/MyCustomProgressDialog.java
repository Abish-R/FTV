package helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.ftv_fashionshop.helixtech_android.R;

import app.ThemeSetter;

/**
 * Created by harsu on 14-07-2016.
 */
public class MyCustomProgressDialog extends ProgressDialog {
    public MyCustomProgressDialog(Context context) {
        super(context, ThemeSetter.getDesiredTheme(context));


    }

    public MyCustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_custom_progress_dialog);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}
