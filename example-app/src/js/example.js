import { UsbZebraPrinter } from 'capacitor-usb-zebra-printer';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    UsbZebraPrinter.echo({ value: inputValue })
}
