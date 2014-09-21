package com.syde.myomote.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.syde.myomote.BootstrapServiceProvider;
import com.syde.myomote.Injector;
import com.syde.myomote.R;
import com.syde.myomote.authenticator.LogoutService;
import com.syde.myomote.core.CheckIn;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.syde.myomote.core.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class CheckInsListFragment extends ItemListFragment<CheckIn> {

    @Inject protected BootstrapServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    protected void configureList(final Activity activity, final ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);

        getListAdapter()
                .addHeader(activity.getLayoutInflater()
                        .inflate(R.layout.checkins_list_item_labels, null));
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    public Loader<List<CheckIn>> onCreateLoader(final int id, final Bundle args) {
        final List<CheckIn> initialItems = items;
        return new ThrowableLoader<List<CheckIn>>(getActivity(), items) {

            @Override
            public List<CheckIn> loadData() throws Exception {

                    ArrayList<CheckIn> list = new ArrayList<CheckIn>();
                    CheckIn addMyo = new CheckIn();
                    addMyo.setName("Peter Gokhshteyn's Myo");
                    addMyo.randomData="TestID: ss89ddfg";
                    list.add(addMyo);
                    addMyo = new CheckIn();
                    addMyo.setName("Jared's Myo");
                    addMyo.randomData="TestID: fga9d8fs";
                    list.add(addMyo);
                    return list;

            }
        };
    }

    @Override
    protected SingleTypeAdapter<CheckIn> createAdapter(final List<CheckIn> items) {
        return new CheckInsListAdapter(getActivity().getLayoutInflater(), items);
    }

    @Override
    protected int getErrorMessage(final Exception exception) {
        return R.string.error_loading_checkins;
    }
}
