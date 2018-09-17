package com.neurotec.tutorials.biometrics;

import static com.neurotec.tutorials.biometrics.JsonUtil.*;
import static spark.Spark.*;

import com.neurotec.tutorials.util.LibraryManager;

/**
 * Enrollment class.
 */
public final class EnrollFingerFromScanner {
    public static void main(String[] args) {
       LibraryManager.initLibraryPath();
       FingerScannerUtils fScannerUtils = new FingerScannerUtils();

        get("/check-license", (req, res) -> {
            if (fScannerUtils.checkLicense()) {
                return "Biometric Lib has license";
            }
            return "Biometric Lib is not licensed";
        });

        get("/devices", (req, res) -> {
            String[] devices = fScannerUtils.getDeviceList();
            if (devices.length > 0) {
                return devices;
            }

            return "No Device found";
        }, json());

        get("/select-device", (req, res) -> {
            int selected = Integer.parseInt(req.queryMap().get("selected").value());
            return selected;
        });

        get("/scan", (req, res) -> {
            fScannerUtils.initApp();

            if (!fScannerUtils.checkLicense()) {
                return "Biometric Lib has no license";
            }

            String[] devices = fScannerUtils.getDeviceList();
            if (devices.length == 0) {
                return "Please connect a device";
            }

            int selected = Integer.parseInt(req.queryMap().get("selected").value());
            String name = req.queryMap().get("name").value();
            String imageName = name + ".jpg";
            String templateName = name + ".ndf";
            boolean created = fScannerUtils.scanFinger(selected, imageName, templateName);

            if (created) {
                return fScannerUtils.mapFiles(imageName, templateName);
            }
            return "finger was not scanned";
        }, json());
    }
}
