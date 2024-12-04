export interface ZebraPrinterInfo {
    name: string;
    address: string;
}
export interface UsbZebraPrinterPlugin {
    discoverPrinters(): Promise<{
        printers: ZebraPrinterInfo[];
    }>;
    requestPermission(options: {
        address: string;
    }): Promise<{
        granted: boolean;
    }>;
    print(options: {
        address: string;
        data: string;
    }): Promise<void>;
}
