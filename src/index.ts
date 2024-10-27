import { registerPlugin } from '@capacitor/core';

import type { UsbZebraPrinterPlugin } from './definitions';

const UsbZebraPrinter = registerPlugin<UsbZebraPrinterPlugin>('UsbZebraPrinter', {
  web: () => import('./web').then((m) => new m.UsbZebraPrinterWeb()),
});

export * from './definitions';
export { UsbZebraPrinter };
