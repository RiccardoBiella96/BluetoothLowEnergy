package supsi.ch.bluetoothlowenergy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PROVA";

    private static final int REQUEST_ENABLE_BT = 1;
    private final int PERMISSION_LOCATION_REQUEST = 2;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private ScanCallback scanCallback;
    Context context = this;
    BluetoothGatt bluetoothGatt;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("PROVA", "onReceive: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "checkIfBluetoothSupported: BLE not supported");
            finish();
        }
        Log.i(TAG, "checkIfBluetoothSupported: BLE supported");

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();



        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Check and request the permission of location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // must ask permissions
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_LOCATION_REQUEST);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.i(TAG, "onScanResult: Device Found");
                    BluetoothDevice bluetoothDevice = result.getDevice();
                    connectToGatt(bluetoothDevice);
                }
            };
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))).build();
//            ScanFilter scanFilter = new ScanFilter.Builder().setDeviceName("DELL-RICCARDO").build();
            ArrayList<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
            scanFilters.add(scanFilter);
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            bluetoothLeScanner.startScan(scanFilters, settings, scanCallback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void connectToGatt(final BluetoothDevice bluetoothDevice) {
        bluetoothLeScanner.stopScan(scanCallback);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK) finish();
                break;
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

            Log.i(TAG, " Connection state change: " + status + " -> " + newState);
            //Connection established
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "DISCOVERY SERVICES");
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Device STATE_disconnect ");
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Disconnected from the BLE device");
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.i(TAG, "Device Services Discovered: " + status);
            BluetoothGattCharacteristic bluetoothGattCharacteristic =
                    gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
            gatt.setCharacteristicNotification(bluetoothGattCharacteristic,true);
            BluetoothGattDescriptor bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(bluetoothGattDescriptor);


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead: " + characteristic.toString());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Integer value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
            Log.i(TAG, "onCharacteristicChanged: " + value);


        }
    };

}