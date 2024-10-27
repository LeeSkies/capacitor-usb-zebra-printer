export interface UsbZebraPrinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
