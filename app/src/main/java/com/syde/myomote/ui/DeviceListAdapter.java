package com.syde.myomote.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.syde.myomote.BootstrapApplication;
import com.syde.myomote.R;
import com.syde.myomote.core.Control;
import com.syde.myomote.core.Device;
import com.syde.myomote.core.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.squareup.picasso.Picasso;
import com.thalmic.myo.Pose;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Adapter to display a list of traffic items
 */
public class DeviceListAdapter extends SingleTypeAdapter<Control> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd");

    /**
     * @param inflater
     * @param items
     */
    public DeviceListAdapter(final LayoutInflater inflater, final List<Control> items) {
        super(inflater, R.layout.device_list_item);

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
        return new int[]{R.id.iv_avatar, R.id.signal, R.id.control_name, R.id.device_name, R.id.gesture_name};
    }

    @Override
    protected void update(final int position, final Control control) {

        if ((control.setPose == null && control.customPose != null && !control.deviceName.isEmpty()) || control.setPose.equals(Pose.FINGERS_SPREAD))
            Picasso.with(BootstrapApplication.getInstance()).load(R.drawable.solid_blue_spread_fingers).into(imageView(0));
        else if (control.setPose.equals(Pose.WAVE_IN))
            Picasso.with(BootstrapApplication.getInstance()).load(R.drawable.solid_blue_wave_left).into(imageView(0));
        else if (control.setPose.equals(Pose.WAVE_OUT))
            Picasso.with(BootstrapApplication.getInstance()).load(R.drawable.solid_blue_wave_right).into(imageView(0));
        else if (control.setPose.equals(Pose.FIST))
            Picasso.with(BootstrapApplication.getInstance()).load(R.drawable.solid_blue_fist).into(imageView(0));
        else if (control.setPose.equals(Pose.THUMB_TO_PINKY))
            Picasso.with(BootstrapApplication.getInstance()).load(R.drawable.solid_blue_pinky_thumb).into(imageView(0));

        String gestureName = "";
        if (control.customPose != null && !control.customPose.isEmpty()) {
            gestureName = control.customPose;
            gestureName = gestureName.replaceAll("_", " ");
        } else {
            gestureName = control.setPose.name();
            gestureName = gestureName.replaceAll("_", " ");
        }

        setText(4, gestureName);

        String name = control.name;
        name = name.replaceAll("_", " ");

        setText(2, name);

        setText(1, control.signal);

        String deviceName = control.deviceName;
        deviceName = deviceName.replaceAll("_", " ");
        setText(3, deviceName);

    }

}
