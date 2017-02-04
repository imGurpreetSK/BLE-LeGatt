package me.gurpreetsk.android.bluetoothlegatt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.android.bluetoothlegatt.R;

import me.gurpreetsk.android.bluetoothlegatt.emitter.EmitterActivity;
import me.gurpreetsk.android.bluetoothlegatt.receiver.DeviceScanActivity;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: MainActivity");
    }

    public void OpenEmitterctivity(View view){
        Intent intent = new Intent(this, EmitterActivity.class);
        startActivity(intent);
    }

    public void OpenReceiverActivity(View view){
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }
}
