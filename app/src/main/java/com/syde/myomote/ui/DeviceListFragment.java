package com.syde.myomote.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.syde.myomote.Injector;
import com.syde.myomote.R;
import com.syde.myomote.authenticator.LogoutService;
import com.syde.myomote.core.Control;
import com.syde.myomote.core.Device;
import com.syde.myomote.core.Global;
import com.syde.myomote.core.News;
import com.syde.myomote.core.User;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import static com.syde.myomote.core.Constants.Extra.NEWS_ITEM;

/**
 * Created by pgokhshteyn on 9/20/14.
 */
public class DeviceListFragment extends ItemListFragment<Control> {

    @Inject
    protected LogoutService logoutService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_users);
    }

    @Override
    public Loader<List<Control>> onCreateLoader(final int id, final Bundle args) {
        final List<Control> initialItems = items;
        return new ThrowableLoader<List<Control>>(getActivity(), items) {
            @Override
            public List<Control> loadData() throws Exception {

                try {
                    SharedPreferences sharedPref = Global.mainActivity.getPreferences(Context.MODE_PRIVATE);

                    List<Control> latest = new ArrayList<Control>();

                    int size = sharedPref.getInt(Global.NUM_DEVICES, 0);
                    for (int i = 0; i < size; i++) {
                        Device addDevice = Device.parseString(sharedPref.getString(Global.DEVICES + i, ""));
                        for (Control control : addDevice.controls) {
                            control.deviceName = addDevice.name;
                            latest.add(control);
                        }
                    }

                    if (latest != null) {
                        return latest;
                    } else {
                        return Collections.emptyList();
                    }
                } catch (final Exception e) {
                    final Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                    return initialItems;
                }
            }
        };

    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return 0;
    }

    @Override
    protected SingleTypeAdapter<Control> createAdapter(final List<Control> items) {
        return new DeviceListAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(ListView l, View v, final int position, long id) {
        // Inflate the edit text view and add it to the dialog
        final ItemListFragment fragment = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
        AlertDialog otherDialog = builder.setCancelable(false)
                .setTitle("Would you like to delete this control?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for (Device device : Global.currentDevices) {
                            Control control = ((Control)fragment.getListView().getItemAtPosition(position));
                            Control removeControl = null;
                            if ((removeControl = device.getControl(control.setPose, control.customPose)) != null) {
                                device.controls.remove(removeControl);
                                Global.mainActivity.updateDevice(device, device.id);
                            }
                        }
                    }
                }).setNegativeButton("No", null)
                .create();
        otherDialog.show();
    }
}
