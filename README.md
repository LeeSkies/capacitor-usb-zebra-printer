# capacitor-usb-zebra-printer

The plugin enables you to detect, connect and print with Zebra printer devices through usb

## Install

```bash
npm install capacitor-usb-zebra-printer
npx cap sync
```

## API

<docgen-index>

* [`discoverPrinters()`](#discoverprinters)
* [`requestPermission(...)`](#requestpermission)
* [`print(...)`](#print)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### discoverPrinters()

```typescript
discoverPrinters() => Promise<{ printers: ZebraPrinterInfo[]; }>
```

**Returns:** <code>Promise&lt;{ printers: ZebraPrinterInfo[]; }&gt;</code>

--------------------


### requestPermission(...)

```typescript
requestPermission(options: { address: string; }) => Promise<{ granted: boolean; }>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ address: string; }</code> |

**Returns:** <code>Promise&lt;{ granted: boolean; }&gt;</code>

--------------------


### print(...)

```typescript
print(options: { address: string; data: string; }) => Promise<void>
```

| Param         | Type                                            |
| ------------- | ----------------------------------------------- |
| **`options`** | <code>{ address: string; data: string; }</code> |

--------------------


### Interfaces


#### ZebraPrinterInfo

| Prop          | Type                |
| ------------- | ------------------- |
| **`name`**    | <code>string</code> |
| **`address`** | <code>string</code> |

</docgen-api>
