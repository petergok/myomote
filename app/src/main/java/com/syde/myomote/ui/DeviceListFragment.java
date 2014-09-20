package com.syde.myomote.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.syde.myomote.Injector;
import com.syde.myomote.R;
import com.syde.myomote.authenticator.LogoutService;
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
public class DeviceListFragment extends ItemListFragment<Device> {

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
    public Loader<List<Device>> onCreateLoader(final int id, final Bundle args) {
        final List<Device> initialItems = items;
        return new ThrowableLoader<List<Device>>(getActivity(), items) {
            @Override
            public List<Device> loadData() throws Exception {

                try {
                    SharedPreferences sharedPref = Global.mainActivity.getPreferences(Context.MODE_PRIVATE);

                    List<Device> latest = new ArrayList<Device>();

                    int size = sharedPref.getInt(Global.NUM_DEVICES, 0);
                    for (int i = 0; i < size; i++) {
                        latest.add(Device.parseString(sharedPref.getString(Global.DEVICES + i, "")));
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
    protected SingleTypeAdapter<Device> createAdapter(final List<Device> items) {
        return new DeviceListAdapter(getActivity().getLayoutInflater(), items);
    }
}
