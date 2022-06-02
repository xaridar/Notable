package com.xaridar.notable.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static final int GEOFIRE_QUERY_RADIUS = 15;
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/uu");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    public static final List<Activity> toFinish = new ArrayList<>();

    public static void addActivityToFinish(Activity a) {
        toFinish.add(a);
    }

    public static void finishAll() {
        for (int i = 0; i < toFinish.size();) {
            toFinish.get(i).finish();
        }
    }

    public static void finishActivity(Activity a) {
        toFinish.remove(a);
    }

    public static String formatAddr(Address addr) {
        String ret = "";
        boolean first = true;
        if (addr.getLocality() != null) {
            ret += addr.getLocality();
            first = false;
        }
        if (addr.getAdminArea() != null) {
            ret += (!first ? ", " : "") + addr.getAdminArea();
            first = false;
        }
        if (addr.getCountryCode() != null) {
            ret += (!first ? ", " : "") + addr.getCountryCode();
        }
        return ret;
    }

    public static BitmapDescriptor getBitmapDescriptorFromDrawable(Drawable drawable) {
        return BitmapDescriptorFactory.fromBitmap(getBitmapDrawable(drawable));
    }

    public static Bitmap getBitmapDrawable(Drawable drawable) {
        Canvas c = new Canvas();
        Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        c.setBitmap(bmp);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(c);
        return bmp;
    }

}
