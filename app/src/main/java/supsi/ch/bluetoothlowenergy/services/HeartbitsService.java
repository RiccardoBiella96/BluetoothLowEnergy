//package supsi.ch.bluetoothlowenergy.services;
//
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanFilter;
//import android.bluetooth.le.ScanResult;
//import android.bluetooth.le.ScanSettings;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.ParcelUuid;
//import android.util.Log;
//
//import androidx.annotation.RequiresApi;
//
//import java.util.ArrayList;
//import java.util.UUID;
//
//import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
//
//public class HeartbitsService extends Service {
//
//    String TAG = "SERVICE";
//    private BluetoothManager bluetoothManager;
//    private BluetoothAdapter bluetoothAdapter;
//    private BluetoothLeScanner bluetoothLeScanner;
//
//    private ScanCallback scanCallback;
////    Context context = this;
//    BluetoothGatt bluetoothGatt;
//
//    public HeartbitsService() {
//
//        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        bluetoothAdapter = bluetoothManager.getAdapter();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            scanCallback = new ScanCallback() {
//                @Override
//                public void onScanResult(int callbackType, ScanResult result) {
//                    Log.i(TAG, "onScanResult: Device Found");
//                    BluetoothDevice bluetoothDevice = result.getDevice();
//                    connectToGatt(bluetoothDevice);
//                }
//            };
//            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))).build();
////            ScanFilter scanFilter = new ScanFilter.Builder().setDeviceName("DELL-RICCARDO").build();
//            ArrayList<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
//            scanFilters.add(scanFilter);
//            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
//            bluetoothLeScanner.startScan(scanFilters, settings, scanCallback);
//        }
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void connectToGatt(final BluetoothDevice bluetoothDevice) {
//        bluetoothLeScanner.stopScan(scanCallback);
//        Handler handler = new Handler();
//        handler.post(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void run() {
//                bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
//            }
//        });
//    }
//
//    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
//
//            Log.i(TAG, " Connection state change: " + status + " -> " + newState);
//            //Connection established
//            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
//                Log.i(TAG, "DISCOVERY SERVICES");
//                gatt.discoverServices();
//            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
//                Log.i(TAG, "Device STATE_disconnect ");
//            } else if (status != BluetoothGatt.GATT_SUCCESS) {
//                Log.i(TAG, "Disconnected from the BLE device");
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
//            Log.i(TAG, "Device Services Discovered: " + status);
//            BluetoothGattCharacteristic bluetoothGattCharacteristic =
//                    gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
//            gatt.setCharacteristicNotification(bluetoothGattCharacteristic,true);
//            BluetoothGattDescriptor bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(bluetoothGattDescriptor);
//
//
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic, int status) {
//            Log.i(TAG, "onCharacteristicRead: " + characteristic.toString());
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            Integer value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
//            Log.i(TAG, "onCharacteristicChanged: " + value);
//
//
//        }
//    };
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
