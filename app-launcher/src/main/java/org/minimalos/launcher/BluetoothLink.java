package org.minimalos.launcher;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** RFCOMM without repeated system pairing prompts; the app authenticates and encrypts each session. */
@SuppressLint("MissingPermission") // L'activité vérifie BLUETOOTH_CONNECT ; services et jobs capturent SecurityException.
final class BluetoothLink {
    static final UUID SERVICE = UUID.fromString("919a2e49-1c10-4bbd-b20c-e3c045596257");
    private BluetoothLink() { }
    static BluetoothAdapter adapter() throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) throw new IOException("Activez le Bluetooth");
        return adapter;
    }
    static List<BluetoothDevice> bonded() throws IOException {
        return new ArrayList<>(adapter().getBondedDevices());
    }
    static BluetoothSocket connect(String address) throws IOException {
        BluetoothAdapter adapter = adapter();
        BluetoothSocket socket = adapter.getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(SERVICE);
        try { socket.connect(); return socket; }
        catch (IOException failure) { try { socket.close(); } catch (IOException ignored) { } throw failure; }
    }
    static BluetoothServerSocket listen() throws IOException {
        return adapter().listenUsingInsecureRfcommWithServiceRecord("Minimal OS media", SERVICE);
    }
}
