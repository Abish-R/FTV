package com.ftv_fashionshop.helixtech_android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HARSH GOYNKA on 7/1/2016.
 */
public class Share {

    private static final int STORAGE_PERMISSIONS = 121;
    public static String productName;
    Context context;


    public Share(Context context) {
        this.context = context;

    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void sharing(Context context, String prod_name, ImageView iv) {


        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "External Storage Not Available!", Toast.LENGTH_SHORT).show();
            return;
        }
        productName = prod_name;

        //ImageView image = (ImageView) findViewById(R.id.image);
        iv.buildDrawingCache();
        Bitmap bm = iv.getDrawingCache();
        OutputStream fOut = null;
        Uri outputFileUri;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
            f.mkdirs();
            if (f.exists())
                f.delete();

            f.createNewFile();

            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            // remember close de FileOutput
            fo.flush();
            fo.close();
        } catch (Exception e) {
            Toast.makeText(context, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpg");
        String imagePath = Environment.getExternalStorageDirectory() + File.separator + "temp.jpg";

        File imageFileToShare = new File(imagePath);

        Uri imageuri = Uri.fromFile(imageFileToShare);

        share.putExtra(Intent.EXTRA_TEXT, "Hey view this product " + productName);

        Intent openInChooser = Intent.createChooser(share, "Show off the Design!");

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(share, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("twitter") || packageName.contains("facebook")|| packageName.contains("android.gm")
                    || packageName.contains("hike") || packageName.contains("whatsapp")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                if (packageName.contains("twitter")) {
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, share.getStringExtra(Intent.EXTRA_TEXT));
                } else if (packageName.contains("facebook")) {
                    intent.setType("image/jpg");
                    intent.putExtra(Intent.EXTRA_TEXT, share.getStringExtra(Intent.EXTRA_TEXT));
                    intent.putExtra(Intent.EXTRA_STREAM, imageuri);
                } else if (packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    intent.setType("application/image");
                    intent.putExtra(Intent.EXTRA_SUBJECT, prod_name);
                    intent.putExtra(Intent.EXTRA_TEXT, share.getStringExtra(Intent.EXTRA_TEXT));
                    intent.putExtra(Intent.EXTRA_STREAM, imageuri);
                } else if (packageName.contains("whatsapp")) {
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_TEXT, share.getStringExtra(Intent.EXTRA_TEXT));
                    intent.putExtra(Intent.EXTRA_STREAM, imageuri);
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));

            }
        }
        Intent openInChooser2 = Intent.createChooser(intentList.get(0), "Show off the Design!");
        intentList.remove(0);
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
        openInChooser2.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        context.startActivity(openInChooser2);


    }

    public static File uploadImage(Context context, ImageView iv, String customer_id) {
        //String filePath=null;
        File sdImageMainDirectory = null;

        //ImageView image = (ImageView) findViewById(R.id.image);
        iv.buildDrawingCache();
        Bitmap bm = iv.getDrawingCache();
        OutputStream fOut = null;
        Uri outputFileUri;
        try {
            File root = new File(Environment.getExternalStorageDirectory()
                    + File.separator);
            root.mkdirs();
            sdImageMainDirectory = new File(root, customer_id + ".png");
            //filePath= sdImageMainDirectory.toString();
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);
        } catch (Exception e) {
            Toast.makeText(context, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
        return sdImageMainDirectory;
    }


}
