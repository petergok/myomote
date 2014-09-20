package com.syde.myomote.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.syde.myomote.BootstrapApplication;
import com.syde.myomote.R;
import com.syde.myomote.core.Device;
import com.syde.myomote.core.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Adapter to display a list of traffic items
 */
public class DeviceListAdapter extends SingleTypeAdapter<Device> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd");

    /**
     * @param inflater
     * @param items
     */
    public DeviceListAdapter(final LayoutInflater inflater, final List<Device> items) {
        super(inflater, R.layout.user_list_item);

        setItems(items);
    }

    /**
     * @param inflater
     */
    public DeviceListAdapter(final LayoutInflater inflater) {
        this(inflater, null);

    }

    @Override
    public long getItemId(final int position) {
        return super.getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.iv_avatar, R.id.tv_name};
    }

    @Override
    protected void update(final int position, final Device device) {

        setText(1, String.format("%1$s %2$s", device.controls, device.name));

    }

}
