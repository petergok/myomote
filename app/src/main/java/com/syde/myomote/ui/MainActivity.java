

package com.syde.myomote.ui;

import android.accounts.OperationCanceledException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.syde.myomote.BootstrapServiceProvider;
import com.syde.myomote.R;
import com.syde.myomote.core.BootstrapService;
import com.syde.myomote.core.Control;
import com.syde.myomote.core.Device;
import com.syde.myomote.core.Global;
import com.syde.myomote.events.NavItemSelectedEvent;
import com.syde.myomote.util.Ln;
import com.syde.myomote.util.SafeAsyncTask;
import com.syde.myomote.util.UIUtils;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.Views;

/**
 * Initial activity for the application.
 * <p/>
 * If you need to remove the authentication from the application please see
 * {@link com.syde.myomote.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends BootstrapFragmentActivity {

    private static final String TAG = "Myo";

    private static int REQUEST_ENABLE_BT = 1;

    private CarouselFragment carouselFragment;

    public static ArrayList<Device> currentDevices;

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public boolean gunShotPose = false;

    // BTLE state
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000000;

    private volatile boolean ismScanning;

    private BluetoothAdapter mBluetoothAdapter;

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    private boolean userHasAuthenticated = false;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;
    private NavigationDrawerFragment navigationDrawerFragment;

    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeLine("connected");
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeLine("failed to start discovering services!");
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeLine("disconnected!");
                ismScanning = true;
            }
            else {
                writeLine("connection state changed, new state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("service discovery completed!");
            }
            else {
                writeLine("service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                }
            }
            else {
                writeLine("Couldn't get RX client descriptor!");
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            writeLine("Received: " + characteristic.getStringValue(0));
        }
    };

    public void sendMessage(String message) {
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (gatt.writeCharacteristic(tx)) {
            writeLine("Sent: " + message);
        }
        else {
            writeLine("Couldn't write TX characteristic!");
        }
    }

    // BTLE device scanning callback.
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            String address = bluetoothDevice.getAddress();
            // Check if the device has the UART service.
            if (bluetoothDevice.getAddress().startsWith("D7:83:D7:1D:A1:D9")) {
                // Found a device, stop the scan.
                ismScanning = false;
                writeLine("Found UART service!");
                // Connect to the device.
                // Control flow will now go to the callback functions when BTLE events occur.
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
                mBluetoothAdapter.stopLeScan(scanCallback);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        ismScanning = true;
        mBluetoothAdapter.startLeScan(scanCallback);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        Global.mainActivity = this;

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        currentDevices = new ArrayList<Device>();

        int size = sharedPref.getInt(Global.NUM_DEVICES, 0);
        for (int i = 0; i < size; i++) {
            currentDevices.add(Device.parseString(sharedPref.getString(Global.DEVICES + i, "")));
        }

        if (currentDevices.isEmpty()) {

            Device newDevice = new Device();
            newDevice.controls = new ArrayList<Control>();

            Control newControl = new Control();
            newControl.name = "Power";
            newControl.customPose = Control.customPoses[0];
            newControl.setPose = null;
            newControl.signal = "0";
            newDevice.controls.add(newControl);

            newControl = new Control();
            newControl.name = "Channel_Up";
            newControl.customPose = "";
            newControl.setPose = Pose.WAVE_OUT;
            newControl.signal = "1";
            newDevice.controls.add(newControl);

            newControl = new Control();
            newControl.name = "Channel_Down";
            newControl.customPose = "";
            newControl.setPose = Pose.WAVE_IN;
            newControl.signal = "2";
            newDevice.controls.add(newControl);

            newDevice.name = "Dynex_TV";

            addDevice(newDevice, 0);
        }

        if (isTablet()) {
            setContentView(R.layout.main_activity_tablet);
        } else {
            setContentView(R.layout.main_activity);
        }

        // View injection with Butterknife
        Views.inject(this);

        // Set up navigation drawer
        title = drawerTitle = getTitle();

        if(!isTablet()) {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerToggle = new ActionBarDrawerToggle(
                    this,                    /* Host activity */
                    drawerLayout,           /* DrawerLayout object */
                    R.drawable.ic_drawer,    /* nav drawer icon to replace 'Up' caret */
                    R.string.navigation_drawer_open,    /* "open drawer" description */
                    R.string.navigation_drawer_close) { /* "close drawer" description */

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(title);
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(drawerTitle);
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            // Set the drawer toggle as the DrawerListener
            drawerLayout.setDrawerListener(drawerToggle);

            navigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

            // Set up the drawer.
            navigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));

            drawerLayout.closeDrawer(Gravity.LEFT);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        ismScanning = true;
        mBluetoothAdapter.startLeScan(scanCallback);

        writeLine("About to init Hub.");
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            writeLine("Could not initialize the Hub.");
            finish();
            return;
        }
        writeLine("Hub initialized!.");


        // Use this instead to connect with a Myo that is very near (ie. almost touching) the device
        Hub.getInstance().pairWithAdjacentMyo();

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        userHasAuthenticated = true;
        initScreen();
    }

    private boolean isTablet() {
        return UIUtils.isTablet(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!isTablet()) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isTablet()) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /* Listen to the data from the Myo */
    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        private long timestampOld;
        private Arm mArm = Arm.UNKNOWN;
        private XDirection mXDirection = XDirection.UNKNOWN;
        

        
        /* Dump accelerometer data for a gesture */
        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            //writeLine("Accelerometer data gained: " + accel.toString());
            if (timestamp - timestampOld > 500) {
                if (mArm != Arm.UNKNOWN && accel.z() > 1.3) {
                    writeLine("Gunshot POSE YA");
                    gunShotPose = true;
                    onPose(myo, timestamp, Pose.REST);
                    return;
                }
            }
        }

        // nConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            writeLine("successful connection.");
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            writeLine("disconnected.");
        }

        // onArmRecognized() is called whenever Myo has recognized a setup gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmRecognized(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mArm = arm;
            mXDirection = xDirection;
            //writeLine("arm registered, orientation noted, " + mArm.name());
        }

        // onArmLost() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmLost(Myo myo, long timestamp) {
            mArm = Arm.UNKNOWN;
            mXDirection = XDirection.UNKNOWN;
            writeLine("removed from arm?");
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.

        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            if(mArm != Arm.UNKNOWN){
                // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
                float roll = (float) Math.toDegrees(Quaternion.roll(rotation));

                // Adjust roll and pitch for the orientation of the Myo on the arm.
                if (mXDirection == XDirection.TOWARD_ELBOW) {
                    roll *= -1;
                }

                //Log.e(TAG, "Orientation: " + roll);
            }
        }


        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.

            for (Device device : currentDevices) {
                Control control;
                if ((control = device.getControl(pose, gunShotPose ? "gunShot" : "")) != null) {
                    writeLine(control.signal);
                    sendMessage(control.signal);
                }
            }

            gunShotPose = false;

            switch (pose) {
                case UNKNOWN:
                    //writeLine("case unknown");
                    break;
                case REST:
                    switch (mArm) {
                        case LEFT:
                            //writeLine("Arm left");
                            break;
                        case RIGHT:
                            //writeLine("Arm right");
                            break;
                    }

                    //Let the user know they are done the current pose
                    //and in the rest state
                    myo.vibrate(Myo.VibrationType.SHORT);
                    break;

                case FIST:
                    //writeLine("case FIST");
                    break;
                case WAVE_IN:
                    //writeLine("case WAVE IN");
                    break;
                case WAVE_OUT:
                    //writeLine("case WAVE OUT");
                    break;
                case FINGERS_SPREAD:
                    //writeLine(TAG, "case FINGER SPREAD");
                    break;
                case THUMB_TO_PINKY:
                    //writeLine("case THUMB TO DAT PINKY DOE");
                    break;
            }
            timestampOld = timestamp;
        }
    };

    public void updateDevice(Device d, int index) {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Global.DEVICES + index, d.toString());

        editor.commit();
    }

    public void addDevice(Device d, int index) {

        //save the task list to preference
        currentDevices.add(d);
        d.id = index;
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Global.DEVICES + index, d.toString());
        editor.putInt(Global.NUM_DEVICES, index + 1);

        editor.commit();
    }

    private void initScreen() {
        if (userHasAuthenticated) {

            Ln.d("Foo");
            final FragmentManager fragmentManager = getSupportFragmentManager();
            carouselFragment = new CarouselFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, carouselFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (!isTablet() && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                //menuDrawer.toggleMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navigateToTimer() {
        final Intent i = new Intent(this, BootstrapTimerActivity.class);
        startActivity(i);
    }

    @Subscribe
    public void onNavigationItemSelected(NavItemSelectedEvent event) {

        Ln.d("Selected: %1$s", event.getItemPosition());

        switch (event.getItemPosition()) {
            case 0:
                // Home
                // do nothing as we're already on the home screen.
                break;
            case 1:
                // Timer
                navigateToTimer();
                break;
        }
    }

    public void writeLine(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });

        Log.e("UART", message);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if(resultCode == RESULT_OK){
                Control control = (Control) data.getSerializableExtra("result");
                for (Device device : currentDevices) {
                    if (device.name.equals(control.deviceName)) {
                        device.controls.add(control);
                        updateDevice(device, device.id);
                        return;
                    }
                }
                Device newDevice = new Device();
                newDevice.name = control.deviceName;
                newDevice.controls = new ArrayList<Control>();
                newDevice.controls.add(control);
                addDevice(newDevice, currentDevices.size());
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }


    }

}
