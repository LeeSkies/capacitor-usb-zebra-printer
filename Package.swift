// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorUsbZebraPrinter",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorUsbZebraPrinter",
            targets: ["UsbZebraPrinterPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "UsbZebraPrinterPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/UsbZebraPrinterPlugin"),
        .testTarget(
            name: "UsbZebraPrinterPluginTests",
            dependencies: ["UsbZebraPrinterPlugin"],
            path: "ios/Tests/UsbZebraPrinterPluginTests")
    ]
)