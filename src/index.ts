import { registerPlugin } from '@capacitor/core';

import type { UsbZebraPrinterPlugin } from './definitions';

const UsbZebraPrinter = registerPlugin<UsbZebraPrinterPlugin>('UsbZebraPrinter');

export * from './definitions';
export { UsbZebraPrinter };
