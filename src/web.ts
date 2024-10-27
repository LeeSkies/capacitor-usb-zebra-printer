import { WebPlugin } from '@capacitor/core';

import type { UsbZebraPrinterPlugin } from './definitions';

export class UsbZebraPrinterWeb extends WebPlugin implements UsbZebraPrinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
