package com.test.myomote.myomote_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.scanner.ScanActivity;


public class Myomote_Test extends Activity {

    private static final String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Context context = getBaseContext();
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Log.d(TAG, "Could not initialize the Hub.");
            finish();
            return;
        }

       hub.getInstance().pairWithAdjacentMyo();
    }

    private DeviceListener mListener = new AbstractDeviceListener() {
        Context mContext = getBaseContext(); //Pray to your based god that this will work.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            Toast.makeText(mContext, "Myo Connected!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            Toast.makeText(mContext, "Myo Disconnected!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Toast.makeText(mContext, "Pose: " + pose, Toast.LENGTH_SHORT).show();

            //TODO: Do something awesome.
        }
    };

}
