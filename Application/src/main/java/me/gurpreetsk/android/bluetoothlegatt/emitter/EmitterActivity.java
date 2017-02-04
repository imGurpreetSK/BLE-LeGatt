package me.gurpreetsk.android.bluetoothlegatt.emitter;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.R;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EmitterActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    BluetoothLeAdvertiser bluetoothLeAdvertiser;
    AdvertiseData mAdvertiseData;
    AdvertiseSettings mAdvertiseSettings;
    Pubnub pubnub;
    TextView textView;

    private static final String TAG = "EmitterActivity";
    String text = "Emission started successfully";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emitter);
        Log.i(TAG, "onCreate ");

        pubnub = new Pubnub();
        textView = (TextView) findViewById(R.id.text);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        setAdvertiseData();
        setAdvertiseSettings();

        bluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, advertiseCallback);

//        String channel = "YourCompany_";//+ major + "_" + minor;
//        try {
//            pubnub.presence(channel, mPresenceCallback);
//        } catch (PubnubException e) {
//            Log.d(TAG, e.toString());
//        }

    }


    protected Callback mPresenceCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
            pubnub.publish(channel, "Come in now to get $2 off your next drink!", new Callback() {
            });
        }
    };


    AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "onStartSuccess: inside");
            textView.setText(text);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.i(TAG, "onStartFailure: failed");
        }
    };


    protected void setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = getIdAsByte(UUID.fromString("00002901-0000-1000-8000-00805f9b34fba"));
        mManufacturerData.put(0, (byte) 0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte) 0xAC); // Beacon Identifier
        for (int i = 2; i <= 17; i++) {
            mManufacturerData.put(i, uuid[i - 2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte) 0x00); // first byte of Major
        mManufacturerData.put(19, (byte) 0x09); // second byte of Major
        mManufacturerData.put(20, (byte) 0x00); // first minor
        mManufacturerData.put(21, (byte) 0x06); // second minor
        mManufacturerData.put(22, (byte) 0xB5); // txPower
        mBuilder.addManufacturerData(224, mManufacturerData.array()); //TODO: using google's company ID
        mAdvertiseData = mBuilder.build();
    }


    public static byte[] getIdAsByte(java.util.UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }


    protected void setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();
        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        mBuilder.setConnectable(true);
        mBuilder.setTimeout(0);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        text += " at High power";
        mAdvertiseSettings = mBuilder.build();
    }

}
