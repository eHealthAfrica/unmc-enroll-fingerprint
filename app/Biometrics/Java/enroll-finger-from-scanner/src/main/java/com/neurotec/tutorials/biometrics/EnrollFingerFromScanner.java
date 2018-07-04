package com.neurotec.tutorials.biometrics;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Arrays;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFScanner;
import com.neurotec.devices.NDeviceManager.DeviceCollection;
import com.neurotec.io.NFile;
import com.neurotec.lang.NCore;
import com.neurotec.licensing.NLicense;
import com.neurotec.tutorials.util.LibraryManager;
import com.neurotec.tutorials.util.Utils;
import com.neurotec.tutorials.biometrics.FingerScannerUtils;

import static spark.Spark.*;
import static com.neurotec.tutorials.biometrics.JsonUtil.*;

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
            String[] devices = fScannerUtils.getDevicesList();
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
            if (!fScannerUtils.checkLicense()) {
                return "Biometric Lib has no license";
            }

            String[] devices = fScannerUtils.getDevicesList();
            if (devices.length == 0) {
                return "Please connect a device";
            }

            int selected = Integer.parseInt(req.queryMap().get("selected").value());
            String name = req.queryMap().get("name").value();
            String imageName = name + ".jpg";
            String templateName = name + ".ndf";
            boolean created = fScannerUtils.scanFinger(selected, imageName, templateName);

            if (created) {
                // fScannerUtils.closeResources();
                return fScannerUtils.mapFiles(imageName, templateName);
            }
            return "finger was not scanned";
        });
    }
}
