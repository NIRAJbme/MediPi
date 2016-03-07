/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.practitionerdevices;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.medipi.MediPi;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;
import org.warlock.itk.distributionenvelope.DistributionEnvelopeHelper;
import org.warlock.itk.distributionenvelope.Payload;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.devices.Oximeter;

/**
 * A concrete implementation designed to mimic a device but allowing received
 * data to be injected in.
 *
 * This is a quick way of implementing a system that will display results from a
 * file containing a DistributionEnvelope. When combined with a host listening
 * for incoming messages from MediPi and writing then to disk (e.g. TKW) it can
 * be used to demonstrate that the same data which has been sent is being
 * received. This is obviously not a solution for a production receiver! 
 *
 * @author rick@robinsonhq.com
 */
public class OximeterPractitioner extends Oximeter implements Runnable {

    private final String DEVICE_TYPE = "Finger Oximeter";
    private static final String MAKE = "Contec";
    private static final String MODEL = "CMS50D+";
    private static final String MESSAGE_DIR = "medipi.executionmode.practitioner.incomingmessagedir";

    BufferedReader dataReader;
    protected PipedOutputStream pos = new PipedOutputStream();

    /**
     * Constructor for ContecCMS50DPlus
     */
    public OximeterPractitioner() {
    }

    @Override
    public String init() throws Exception{
        super.init();
        return null;
    }

    /**
     * method to get the Make and Model of the device
     *
     * @return make and model of device
     */
    @Override
    public String getName() {
        return MAKE + " " + MODEL;
    }

    /**
     * method to get the generic Type of the device
     *
     * @return generic type of device
     */
    @Override
    public String getType() {
        return DEVICE_TYPE;
    }

    /**
     * Opens the USB serial connection and prepares for serial data
     *
     * @return boolean value of success of the connection initiation
     */
    @Override
    public BufferedReader startSerialDevice() {
        try {
            pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos);
            dataReader = new BufferedReader(new InputStreamReader(pis));
            new Thread(this).start();
            return dataReader;
        } catch (IOException e) {
            MediPiMessageBox.getInstance().makeErrorMessage("Connection Failure", e, Thread.currentThread());
            return null;
        }
    }

    /**
     * Stops the USB serial port and resets the listeners
     *
     * @return boolean value of success of the connection closing
     */
    @Override
    public boolean stopSerialDevice() {

        try {
            dataReader.close();
        } catch (IOException ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Cant close data connection", ex, Thread.currentThread());
        }
        try {
            pos.close();
        } catch (IOException ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Cant close data connection", ex,Thread.currentThread());
        }
        return true;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            String itkTrunkDir = MediPiProperties.getInstance().getProperties().getProperty(MESSAGE_DIR);
            File file = lastFileModified(itkTrunkDir);
            FileInputStream fis = new FileInputStream(file);
            StringBuilder builder = new StringBuilder();
            int ch;
            while ((ch = fis.read()) != -1) {
                builder.append((char) ch);
            }
            DistributionEnvelopeHelper helper = DistributionEnvelopeHelper.getInstance();
            DistributionEnvelope d = helper.getDistributionEnvelope(builder.toString());
            Payload[] p = helper.getPayloads(d);
            for (Payload pay : p) {
                                if (medipi.getDebugMode() == MediPi.DEBUG) System.out.println(pay.getContent());
                if (pay.getProfileId().equals(getProfileId())) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pay.getContent().getBytes())));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if(line.startsWith("metadata:")){
                            continue;
                        }
                        pos.write((line+"\n").getBytes());
                        pos.flush();
                    }
                }
            }
            task.cancel();
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Device no longer accessible", ex, Thread.currentThread());
        }
    }

    private static File lastFileModified(String dir) {
        File fl = new File(dir);
        File[] files = fl.listFiles(File::isFile);
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }
}