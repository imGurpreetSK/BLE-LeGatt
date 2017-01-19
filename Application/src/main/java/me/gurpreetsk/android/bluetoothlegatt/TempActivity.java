package me.gurpreetsk.android.bluetoothlegatt;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.R;

public class TempActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        String temp = getIntent().getStringExtra("temp");

        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText(temp);

    }
}
