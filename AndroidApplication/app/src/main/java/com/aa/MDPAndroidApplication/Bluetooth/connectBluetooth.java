package com.aa.MDPAndroidApplication.Bluetooth;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.aa.MDPAndroidApplication.R;

public class connectBluetooth extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    BluetoothAdapter bluetoothAdapter;
    Button btnEnableDisable_Discoverable;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){ // catching the ACTION_STATE_CHANGED request which was sent with the intent
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){  // This is why using broadcast receiver so the feedback is elaborate
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };



    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){ // catching the ACTION_STATE_CHANGED request which was sent with the intent
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, bluetoothAdapter.ERROR);

                switch(mode){  // This is why using broadcast receiver so the feedback is elaborate
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };



    @Override
    protected void onDestroy() { // so that the broadcast receiver is closed when the app is destroyed
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF); // getting the ON/OFF button from the layout
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //object represents the devices own bluetooth adapter

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enable/disable bluetooth");
                enableDisableBT();
            }
        });
    }



    public void enableDisableBT() {
        if (bluetoothAdapter == null) { //Device does not support bluetooth
            Log.d(TAG, "enableDisableBT: Device does not support bluetooth");
        }
        if (!bluetoothAdapter.isEnabled()){ // If the bluetooth is not enabled
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED); //filter that intercepts changes to bluetooth status
            registerReceiver(mBroadcastReceiver1, BTIntent);

        }
        if (bluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED); //filter to capture ste change
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }


    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }
}
