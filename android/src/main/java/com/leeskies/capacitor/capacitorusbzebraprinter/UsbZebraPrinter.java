package com.leeskies.capacitor.capacitorusbzebraprinter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.app.PendingIntent;
import android.os.Build;

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
    private static final String ACTION_USB_PERMISSION = "com.leeskies.capacitor.capacitorusbzebraprinter.USB_PERMISSION";
    private final Map<String, DiscoveredPrinterUsb> discoveredPrinters = new HashMap<>();
    private final Context context;
    private final UsbManager usbManager;
    private final PendingIntent permissionIntent;
    private EventEmitter eventEmitter;
    private BroadcastReceiver usbReceiver;

    public interface EventEmitter {
        void emit(String eventName, JSObject data);
    }

    public UsbZebraPrinter(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        // Use FLAG_IMMUTABLE for Android 12+ compatibility
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        this.permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), flags);

        registerUsbReceiver();
    }

    private void registerUsbReceiver() {
        usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                        if (eventEmitter != null) {
                            JSObject result = new JSObject()
                                    .put("granted", granted)
                                    .put("address", device != null ? device.getDeviceName() : "")
                                    .put("error", device == null ? "Device not found" : null);
                            eventEmitter.emit("usbPermissionResult", result);
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(usbReceiver, filter);
        }
    }

    public void cleanup() {
        if (usbReceiver != null) {
            try {
                context.unregisterReceiver(usbReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver not registered, ignore
            }
            usbReceiver = null;
        }
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
            printerObj.put("vendorId", printer.device.getVendorId());
            printerObj.put("productId", printer.device.getProductId());
            printersArray.put(printerObj);
        }

        JSObject ret = new JSObject();
        ret.put("printers", printersArray);
        ret.put("count", printersArray.length());
        return ret;
    }

    public JSObject requestPermission(String address) {
        DiscoveredPrinterUsb printer = discoveredPrinters.get(address);
        JSObject response = new JSObject();

        if (printer == null) {
            response.put("granted", false);
            response.put("error", "Printer not found");
            return response;
        }

        boolean hasPermission = usbManager.hasPermission(printer.device);
        if (!hasPermission) {
            usbManager.requestPermission(printer.device, permissionIntent);
        }

        response.put("granted", hasPermission);
        return response;
    }

    public JSObject print(String address, String data) {
        JSObject response = new JSObject();

        try {
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
                conn.write(data.getBytes("UTF-8"));
                response.put("success", true);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
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
            if (eventEmitter != null) {
                JSObject error = new JSObject()
                        .put("error", message);
                eventEmitter.emit("discoveryError", error);
            }
        }
    }
}