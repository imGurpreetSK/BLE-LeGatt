package me.gurpreetsk.android.bluetoothlegatt.receiver;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.R;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.pow;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final String TAG = "DeviceScanActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 60 seconds.
    private static final long SCAN_PERIOD = 60000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//        Intent intent = new Intent(this, TempActivity.class);
//        intent.putExtra("temp", temp);
//        startActivity(intent);
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        Log.d(TAG, "onListItemClick() called with:" +
                " device name = [" + device.getName() + "], address = [" + device.getAddress() + "], position = [" + position + "]");
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
//                viewHolder.deviceName = (TextView) view.findViewById(R.id.distance);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

//            viewHolder.TVdistance.setText(calcDistance(dev));

            return view;
        }
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                double distance;
                HashMap<String, Double> nearestDistances = new HashMap<>();

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            Log.i(TAG, "run: device name: " + device.getAddress() + ", RSSI: " + rssi + " distance: " + calcDistance(device.getAddress(), rssi));
//                            distance = calcDistance(device.getAddress(), rssi);
//                            if (nearestDistances.containsKey(device.getAddress())) {
//                                if (nearestDistances.get(device.getAddress()) >= distance)
//                                    nearestDistances.put(device.getAddress(), distance);
//                            } else {
//                            nearestDistances.put(device.getAddress(), distance);
//                            }
//                            Log.i(TAG, "run: nearestDistances: " + nearestDistances.toString());

//                            getMeetingPoints();
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }

            };

    String temp = "";

    /**
     * calculate distance of device from beacon
     *
     * @param address the address of the beacon
     * @param rssi    the rssi value of beacon
     */
    private double calcDistance(String address, int rssi) {
        //TODO: set txPower
        double txPower = -65.0; // Manufacture set this power in the device
        double distance;

        if (rssi == 0) {
            distance = -1.0; // if we cannot determine accuracy, return -1.
        } else {
            double ratio = rssi * 1.0 / txPower;
            if (ratio < 1.0)
                distance = pow(ratio, 10);
            else
                distance = (0.89976) * pow(ratio, 7.7095) + 0.111;
        }

//        if (address.equals("55:46:4F:B2:96:25") && rssi<=-30) {     //TODO: change MAC
        Log.i(TAG, "calcDistance: distance: " + distance + " color: yellow1");
//            temp+="calcDistance: distance: " + distance + " color: yellow1\n";
//        }
//        if (address.equals("55:46:4F:B2:95:E9") && rssi<=-30) {
        Log.i(TAG, "calcDistance: distance: " + distance + " color: yellow2");
//            temp+="calcDistance: distance: " + distance + " color: yellow2\n";
//        }

        return distance;
    }


    private void getMeetingPoints(double distanceA, double distanceB, double distanceC,
                                  double pointA1, double pointA2, double pointB1,
                                  double pointB2, double pointC1, double pointC2) {

        double w, z, x, y, y2;
        w = distanceA * distanceA - distanceB * distanceB - pointA1 * pointA1 - pointA2 * pointA2 + pointB1 * pointB1 + pointB2 * pointB2;

        z = distanceB * distanceB - distanceC * distanceC - pointB1 * pointB1 - pointB2 * pointB2 + pointC1 * pointC1 + pointC2 * pointC2;

        x = (w * (pointC2 - pointB2) - z * (pointB2 - pointA2)) /
                (2 * ((pointB1 - pointA1) * (pointC1 - pointB2) - (pointC1 - pointB1) * (pointB2 - pointA2)));

        y = (w - 2 * x * (pointB1 - pointA1)) / (2 * (pointB2 - pointA2));

        y2 = (z - 2 * x * (pointC1 - pointB1)) / (2 * (pointC1 - pointB2));

        y = (y + y2) / 2;

        Log.i(TAG, "getMeetingPoints: x,y,z are: " + x + y + z);

    }


    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
//        TextView TVdistance;
    }

}