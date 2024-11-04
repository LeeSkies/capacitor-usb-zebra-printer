package com.example.plugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.leeskies.capacitor.capacitorusbzebraprinter.UsbZebraPrinter;

@CapacitorPlugin(name = "UsbZebraPrinter")
public class UsbZebraPrinterPlugin extends Plugin {
    private UsbZebraPrinter implementation;

    @Override
    public void load() {
        implementation = new UsbZebraPrinter(getContext());
        implementation.setEventEmitter(this::notifyListeners);
    }

    @PluginMethod
    public void discoverPrinters(final PluginCall call) {
        try {
            JSObject result = implementation.discoverPrinters();
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Error discovering printers: " + e.getMessage());
        }
    }

    @PluginMethod
    public void requestPermission(PluginCall call) {
        String address = call.getString("address");
        if (address == null) {
            call.reject("Printer address is required");
            return;
        }

        try {
            JSObject result = implementation.requestPermission(address);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Error requesting permission: " + e.getMessage());
        }
    }

    @PluginMethod
    public void print(PluginCall call) {
        String address = call.getString("address");
        String data = call.getString("data");

        if (address == null || data == null) {
            call.reject("Printer address and data are required");
            return;
        }

        try {
            implementation.print(address, data);
            call.resolve();
        } catch (Exception e) {
            call.reject("Error printing: " + e.getMessage());
        }
    }
}