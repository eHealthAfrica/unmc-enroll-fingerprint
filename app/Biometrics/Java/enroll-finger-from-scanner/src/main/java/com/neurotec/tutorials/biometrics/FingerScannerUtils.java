package com.neurotec.tutorials.biometrics;

import java.net.*;
import java.io.*;
import java.util.*;

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

public class FingerScannerUtils {
    NBiometricClient biometricClient = null;
    NSubject subject = null;
    NFinger finger = null;
    DeviceCollection devices;


    public FingerScannerUtils () {
        this.initDevices();
    }
    
    private void freeFileResource (FileInputStream fStream) {
        try {
            if (fStream != null) {
                fStream.close();
            }
        } catch (Throwable e) {
            System.err.println(e);
        }
    }

    private String getFileBase64String (String filePath) {
        File originalFile = new File(filePath);
        String encodedBase64 = null;
        FileInputStream fileInputStreamReader = null;
        try {
            fileInputStreamReader = new FileInputStream(originalFile);
            byte[] bytes = new byte[(int)originalFile.length()];
            fileInputStreamReader.read(bytes);
            encodedBase64 = new String(Base64.getEncoder().encode(bytes));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.freeFileResource(fileInputStreamReader);
        }
        return encodedBase64;
    }

    public void initDevices () {
        this.biometricClient = new NBiometricClient();
        this.biometricClient.setUseDeviceManager(true);
        NDeviceManager deviceManager = this.biometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();

        this.devices = deviceManager.getDevices();
    }

    public void closeResources () {
        if (this.finger != null) this.finger.dispose();
        if (this.subject != null) this.subject.dispose();
        if (this.biometricClient != null) this.biometricClient.dispose();
    }

    public String[] getDevicesList () {
        int total = this.devices.size();
        String[] deviceList = new String[this.devices.size()];
        if (total > 0) {
            for (int i = 0; i < total; i++)
                deviceList[i] = this.devices.get(i).getDisplayName();
        }

        return deviceList;
    }

    public boolean checkLicense () {
        final String components = "Biometrics.FingerExtraction,Devices.FingerScanners";
        boolean hasLicense = true;
        try {
            if (!NLicense.obtainComponents("/local", 5000, components)) {
                hasLicense = false;
            }
        } catch (Throwable th) {
            System.out.println(th);
            hasLicense = false;
        }

        return hasLicense;
    }

    public void selectDevice (int selection) {
        this.biometricClient.setFingerScanner((NFScanner) this.devices.get(selection));
    }

    public void initScan () {
        this.subject = new NSubject();
		this.finger = new NFinger();
    }

    public boolean scanFinger (int selection, String imageName, String templateName) {
        this.selectDevice(selection);
        this.initScan();
        this.subject.getFingers().add(this.finger);
        System.out.println("Capturing....");

        NBiometricStatus status = biometricClient.capture(subject);

			biometricClient.setFingersTemplateSize(NTemplateSize.LARGE);

			status = biometricClient.createTemplate(subject);

			if (status == NBiometricStatus.OK) {
				System.out.println("Template extracted");
			} else {
				System.out.format("Extraction failed: %s\n", status);
				System.exit(-1);
            }
            
            return this.extractDataFromScanner(imageName, templateName);
    }

    public boolean extractDataFromScanner (String imageName, String templateName) {
        boolean created = false;
        try {
            this.subject.getFingers().get(0).getImage().save(imageName);
			System.out.println("Fingerprint image saved successfully...");

			NFile.writeAllBytes(templateName, this.subject.getTemplate().save());
            System.out.println("Template file saved successfully...");
            created = true;
        } catch (Throwable th) {
            System.out.println(th);
            created = false;
        }

        return created;
    }

    public Map<String, String> mapFiles (String imageFilePath, String templateFilePath) {
        Map<String, String> fileMap = new HashMap<String, String>();
        String image = this.getFileBase64String(imageFilePath);
        String template = this.getFileBase64String(templateFilePath);
        fileMap.put("image", image);
        fileMap.put("template", template);
        return fileMap;
    }
}