package com.leeskies.capacitor.capacitorusbzebraprinter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.app.PendingIntent;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UsbZebraPrinter {
    private static final String ACTION_USB_PERMISSION = "com.example.plugin.USB_PERMISSION";
    private final Map<String, DiscoveredPrinterUsb> discoveredPrinters = new HashMap<>();
    private final Context context;
    private final UsbManager usbManager;
    private final PendingIntent permissionIntent;
    private EventEmitter eventEmitter;

    public interface EventEmitter {
        void emit(String eventName, JSObject data);
    }

    public UsbZebraPrinter(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);

        // Register USB permission broadcast receiver
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                        if (eventEmitter != null) {
                            JSObject result = new JSObject()
                                    .put("granted", granted)
                                    .put("address", device != null ? device.getDeviceName() : "");
                            eventEmitter.emit("usbPermissionResult", result);
                        }
                    }
                }
            }
        }, filter);
    }

    public void setEventEmitter(EventEmitter emitter) {
        this.eventEmitter = emitter;
    }

    public JSObject discoverPrinters() throws InterruptedException {
        UsbDiscoveryHandler handler = new UsbDiscoveryHandler();
        UsbDiscoverer.findPrinters(context, handler);

        while (!handler.discoveryComplete) {
            Thread.sleep(100);
        }

        JSArray printersArray = new JSArray();
        for (DiscoveredPrinterUsb printer : handler.printers) {
            String address = printer.device.getDeviceName();
            discoveredPrinters.put(address, printer);

            JSObject printerObj = new JSObject();
            printerObj.put("name", printer.device.getProductName());
            printerObj.put("address", address);
            printersArray.put(printerObj);
        }

        JSObject ret = new JSObject();
        ret.put("printers", printersArray);
        return ret;
    }

    public JSObject requestPermission(String address) {
        DiscoveredPrinterUsb printer = discoveredPrinters.get(address);
        if (printer == null) {
            throw new IllegalArgumentException("Printer not found");
        }

        boolean hasPermission = usbManager.hasPermission(printer.device);
        if (!hasPermission) {
            usbManager.requestPermission(printer.device, permissionIntent);
        }

        return new JSObject().put("granted", hasPermission);
    }

    public void print(String address, String data) throws ConnectionException {
        DiscoveredPrinterUsb printer = discoveredPrinters.get(address);
        if (printer == null) {
            throw new IllegalArgumentException("Printer not found");
        }

        if (!usbManager.hasPermission(printer.device)) {
            throw new SecurityException("No permission to communicate with printer");
        }

        Connection conn = null;
        try {
            conn = printer.getConnection();
            conn.open();
            conn.write(data.getBytes());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private class UsbDiscoveryHandler implements DiscoveryHandler {
        public List<DiscoveredPrinterUsb> printers = new LinkedList<>();
        public boolean discoveryComplete = false;

        public void foundPrinter(DiscoveredPrinter printer) {
            printers.add((DiscoveredPrinterUsb) printer);
        }

        public void discoveryFinished() {
            discoveryComplete = true;
        }

        public void discoveryError(String message) {
            discoveryComplete = true;
        }
    }
}