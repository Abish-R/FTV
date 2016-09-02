package accounts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import helper.PermissionsInterface;

/**
 * Created by HARSH GOYNKA on 6/30/2016.
 * This class is used to get the respective permissions for androids above lollipop.
 */
public class Permissions {
    private static final int PERMISSION_REQUEST_CONTACT = 123 ;
    int a=0;
//    Context context;
//
//    public Permissions(Context context) {
//        this.context = context;
//
//    }


    /**
     * @param context
     * @return
     * This function gets the permissions for accessing the contacts of the user.
     */
    public int askForContactPermission(final Activity context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission( context,android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission( context, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(context ,
                        android.Manifest.permission.READ_CONTACTS) && ActivityCompat.shouldShowRequestPermissionRationale(context,
                        android.Manifest.permission.WRITE_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Contacts access needed");

                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            a = 1;
                            if(permissionsInterface!=null){
                                permissionsInterface.passvalue(a);
                            }
                        }
                    });

              //      builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                           ActivityCompat.requestPermissions( context,
                                    new String[]
                                            {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS}
                                    , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
//                    if(a==1)
//                    {
//                        return 1;
//                    }
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(context   ,
                            new String[]{android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else{
               /* Toast.makeText(context, "Working!", Toast.LENGTH_SHORT).show();*/
                return 1;
            }
            return 0;
        }
        else{
         //   Toast.makeText(context, "Compatible android version", Toast.LENGTH_SHORT).show();
            return 1;
        }
    }

PermissionsInterface permissionsInterface;
    public void setListener(PermissionsInterface permissionsInterface) {
        this.permissionsInterface=permissionsInterface;
    }
}
