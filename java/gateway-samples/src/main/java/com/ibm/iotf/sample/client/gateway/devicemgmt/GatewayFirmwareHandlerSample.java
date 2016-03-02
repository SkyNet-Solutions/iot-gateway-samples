/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Modified to include Resource Model
 Sathiskumar Palaniappan - Modified to include Threadpool to support multiple 
                           downloads/updates at the same time (for attached devices)
 *****************************************************************************
 *
 */
package com.ibm.iotf.sample.client.gateway.devicemgmt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.gateway.device.DeviceInterface;
/**
 * This sample Firmware handler demonstrates how one can download and 
 * apply a firmware image in simple steps.
 * 
 * 1. downloadFirmware method is invoked whenever there is a Firmware download
 *    request from the Watson IoT Platform. In this example, we try to download
 *    the firmware file using HTTP methods. It could be a arduino.hex file for Arduino Uno
 *    device attached to the Raspberry Pi Gateway, or the debian package for the Raspberry Pi 
 *    Gateway itself.
 *   
 * 2. updateFirmware method is invoked whenever there is a update firmware request
 *    from the Watson IoT Platform. In this example, it tries to install the 
 *    debian package that is downloaded if its a Gateway, and calls the appropriate
 *    method in ArduinoInterface if its for Arduino Uno device.
 */
public class GatewayFirmwareHandlerSample extends DeviceFirmwareHandler {
	
	private static final String CLASS_NAME = GatewayFirmwareHandlerSample.class.getName();
	private static final String DEPENDENCY_ERROR_MSG = "dependency problems - leaving unconfigured";
	private static final String ERROR_MSG = "Errors were encountered while processing";
	private static final String INSTALL_LOG_FILE = "install.log";
	
	private enum InstalStatus {
		SUCCESS(0),
		DEPENDENCY_ERROR(2),
		ERROR(3);
		
		private int status;
		
		private InstalStatus(int status) {
			this.status = status;
		}
		
	}
	
	private Map<String, DeviceInterface> deviceMap = new HashMap<String, DeviceInterface>();
	private ManagedGateway gateway;
	public String gatewayDownloadFirmwareName;
	
	public void addDeviceInterface(String key, DeviceInterface device) {
		deviceMap.put(key, device);
	}
	
	public GatewayFirmwareHandlerSample() {
	}

	/**
	 * <p>This task downloads the firmware for any attached device and Gateway, because 
	 * the download process is common for all the devices wherein one need to
	 * download the firmware image from the given URL.</p>
	 */
	private static class FirmwareDownloadTask implements Runnable {
		
		private DeviceFirmware deviceFirmware;
		private GatewayFirmwareHandlerSample handler;

		public FirmwareDownloadTask(DeviceFirmware deviceFirmware, GatewayFirmwareHandlerSample handler) {
			this.deviceFirmware = deviceFirmware;
			this.handler = handler;
		}

		@Override
		public void run() {
			System.out.println(" --> Firmware Download requested for device "+deviceFirmware.getDeviceId());
			boolean success = false;
			URL firmwareURL = null;
			URLConnection urlConnection = null;
			
			String downloadedFirmwareName = "";
			
			// As a first step inform the server about the firmware start
			deviceFirmware.setState(FirmwareState.DOWNLOADING);
			
			DeviceInterface device = handler.getDevice(handler.getKey(deviceFirmware));
			/**
			 * start downloading the firmware image
			 */
			try {
				System.out.println(CLASS_NAME + ": Downloading Firmware from URL " + deviceFirmware.getUrl());
				
				firmwareURL = new URL(deviceFirmware.getUrl());
				urlConnection = firmwareURL.openConnection();
				int fileSize = urlConnection.getContentLength();
				
				if(deviceFirmware.getName() != null &&
						!"".equals(deviceFirmware.getName())) {
					downloadedFirmwareName = deviceFirmware.getName();
				} else {
					// use the timestamp as the name
					downloadedFirmwareName = deviceFirmware.getDeviceId() + "firmware_" + new Date().getTime() + ".deb";
				}
				
				File file = new File(downloadedFirmwareName);
				BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getName()));
				
				// count the download size to send the progress report as DiagLog to Watson IoT Platform
				int downloadedSize = 0;
				
				int data = bis.read();
				downloadedSize += 1;
				if(data != -1) {
					bos.write(data);
					byte[] block = new byte[1024];
					int previousProgress = 0;
					while (true) {
						int len = bis.read(block, 0, block.length);
						if(len != -1) {
							downloadedSize += len;
							// Send the progress update
							if(fileSize > 0) {
								int progress = (int) (((float)downloadedSize / fileSize) * 100);
								if(progress > previousProgress) {
									String message = "Firmware Download progress: "+progress + "%";
									if(device != null) {
										// This download is for device, so send device log
										device.setLog(LogSeverity.informational, message, null, new Date());
									} else {
										handler.addGatewayLog(message, new Date(), LogSeverity.informational);
									}
									System.out.println(message);
								}
							} else {
								// If we can't retrieve the filesize, let us update how much we have download so far
								String message = "Downloaded : "+ downloadedSize + " bytes so far";
								if(device != null) {
									// This download is for device, so send device log
									device.setLog(LogSeverity.informational, message, null, new Date());
								} else {
									handler.addGatewayLog(message, new Date(), LogSeverity.informational);
								}
								System.out.println(message);
							}
							bos.write(block, 0, len);
						} else {
							break;
						}
					}
					bos.close();
					bis.close();
					
					success = true;
				} else {
					//There is no data to read, so throw an exception
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
				}
				
				// Verify the firmware image if verifier is set
				if(deviceFirmware.getVerifier() != null && !deviceFirmware.getVerifier().equals("")) {
					success = verifyFirmware(file, deviceFirmware.getVerifier());
					
					/**
					 * As per the documentation, If a firmware verifier has been set, the device should 
					 * attempt to verify the firmware image. 
					 * 
					 * If the image verification fails, mgmt.firmware.state should be set to 0 (Idle) 
					 * and mgmt.firmware.updateStatus should be set to the error status value 4 (Verification Failed).
					 */
					if(success == false) {
						deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.VERIFICATION_FAILED);
						// the firmware state is updated to IDLE below
					}
				}
				
			} catch(MalformedURLException me) {
				// Invalid URL, so set the status to reflect the same,
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
				me.printStackTrace();
			} catch (IOException e) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.CONNECTION_LOST);
				e.printStackTrace();
			} catch (OutOfMemoryError oom) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
			}
			
			/**
			 * Set the firmware download and possibly the firmware update status
			 * (will be sent later) accordingly
			 */
			if(success == true) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.DOWNLOADED);
				if(device != null) {
					device.setFirmwareName(downloadedFirmwareName);
				} else {
					// this firmware request is for gateway
					handler.gatewayDownloadFirmwareName = downloadedFirmwareName;
				}
			} else {
				deviceFirmware.setState(FirmwareState.IDLE);
			}
			
			System.out.println("<-- Firmware Download END...("+success+ ")");
		}
		
	}
	
	private ExecutorService threadPoolExecutor = null;
	
	private void addGatewayLog(String message, Date date,
			LogSeverity severity) {
		gateway.addGatewayLog(message, date, severity);
		
	}

	@Override
	public void downloadFirmware(DeviceFirmware deviceFirmware) {
		FirmwareDownloadTask task = new FirmwareDownloadTask(deviceFirmware, this);
		threadPoolExecutor.execute(task);
	}

	@Override
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		FirmwareUpdateTask task = new FirmwareUpdateTask(deviceFirmware, this);
		threadPoolExecutor.execute(task);
	}
	
	private static class FirmwareUpdateTask implements Runnable {
		DeviceFirmware deviceFirmware;
		GatewayFirmwareHandlerSample handler;
		
		public FirmwareUpdateTask(DeviceFirmware deviceFirmware,
				GatewayFirmwareHandlerSample handler) {
			this.deviceFirmware = deviceFirmware;
			this.handler = handler;
		}

		/**
		 * <p>A sample Firmware update task, that calls the appropriate device interface
		 * to update the firmware if its attached device.</p>
		 * 
		 * Also, if its a gateway, the sample tries to update the debian package
		 * with the following command. <br>
		 * 
		 * sudo dpkg -i /path/to/filename.deb
		 * 
		 * <p>If this fails with a message about the package depending on something 
		 * that isn't installed, then fix it by running the following command,</p>
		 * 
		 * sudo apt-get -fy install
		 * 
		 * <p>This will install the dependencies (assuming they're available in the 
		 * repos your system knows about) AND the package that were originally 
		 * requested to install ('f' is the 'fix' option and 'y' is the 'assume 
		 * yes to prompts' or 'don't ask me if it's ok, just install it already' 
		 * option -- very useful for scripted silent installs).</p> 
		 * 
		 */
		public void run() {
			System.out.println("--> Firmware update requested for device = "+deviceFirmware.getDeviceId());
			// As a first step inform the server about the start of firmware update
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.IN_PROGRESS);
			try {
				/**
				 * Call the attached device interface to update the firmware if its
				 * targeted for attached devices.
				 */
				DeviceInterface device = handler.getDevice(handler.getKey(deviceFirmware));
				if(device != null) {
					device.updateFirmware(deviceFirmware);
					return;
				}
				
				
				// Code to update the firmware on the Gateway
				ProcessBuilder pkgInstaller = null;
				ProcessBuilder dependencyInstaller = null;
				Process p = null;
				
				pkgInstaller = new ProcessBuilder("sudo", "dpkg", "-i", handler.gatewayDownloadFirmwareName);
				pkgInstaller.redirectErrorStream(true);
				pkgInstaller.redirectOutput(new File(INSTALL_LOG_FILE));
				
				dependencyInstaller = new ProcessBuilder("sudo", "apt-get", "-fy", "install");
				dependencyInstaller.redirectErrorStream(true);
				dependencyInstaller.inheritIO();
	
				try {
					p = pkgInstaller.start();
					boolean status = waitForCompletion(p, 5);
					if(status == false) {
						p.destroy();
						deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
						return;
					}
					// check for install error
					InstalStatus instalStatus = ParseInstallLog();
					if(instalStatus == InstalStatus.DEPENDENCY_ERROR) {
						System.err.println("Following dependency error occured while "
								+ "installing the image " + handler.gatewayDownloadFirmwareName);
						System.err.println(getInstallLog(INSTALL_LOG_FILE));
						
						System.out.println("Trying to update the dependency with the following command...");
						System.out.println("sudo apt-get -fy install");
						p = dependencyInstaller.start();
						status = waitForCompletion(p, 5);
					} else if(instalStatus == InstalStatus.ERROR) {
						System.err.println("Following error occured while "
								+ "installing the image " + handler.gatewayDownloadFirmwareName);
						System.err.println(getInstallLog(INSTALL_LOG_FILE));
						deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
						return;
					}
					System.out.println("Firmware Update command "+status);
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				} catch (IOException e) {
					e.printStackTrace();
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
				} catch (InterruptedException e) {
					e.printStackTrace();
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
				}
			} catch (OutOfMemoryError oom) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
			}
			deviceFirmware.setState(FirmwareState.IDLE);
		
			/**
			 * Delete the temporary firmware file
			 */
			deleteFile(handler.gatewayDownloadFirmwareName);
			deleteFile(INSTALL_LOG_FILE);
			
			handler.gatewayDownloadFirmwareName = null;
			System.out.println("<-- Firmware update End...");
		}
		
	}
	
	/**
	 * Since JDK7 doesn't take any timeout parameter, we provide an workaround
	 * that wakes up every second and checks for the completion status of the process.
	 * @param process
	 * @param minutes
	 * @return
	 * @throws InterruptedException 
	 */
	public static boolean waitForCompletion(Process process, int minutes) throws InterruptedException {
		long timeToWait = (60 * minutes);
		
		int exitValue = -1;
		for(int i = 0; i < timeToWait; i++) {
			try {
				exitValue = process.exitValue();
			} catch(IllegalThreadStateException  e) {
				// Process is still running
			}
			if(exitValue == 0) {
				return true;
			}
			Thread.sleep(1000);
		}
		// Destroy the process forcibly
		try {
			process.destroy();
		} catch(Exception e) {}
	
		return false;
	}

	
	private static InstalStatus ParseInstallLog() throws FileNotFoundException {
		try {
			File file = new File(INSTALL_LOG_FILE);
		    Scanner scanner = new Scanner(file);

		    while (scanner.hasNextLine()) {
		        String line = scanner.nextLine();
		        if(line.contains(DEPENDENCY_ERROR_MSG)) {
		        	scanner.close();
		            return InstalStatus.DEPENDENCY_ERROR;
		        } else if(line.contains(ERROR_MSG)) {
		        	scanner.close();
		        	return InstalStatus.ERROR;
		        }
		    }
		    scanner.close();
		} catch(FileNotFoundException e) { 
		    throw e;
		}
		return InstalStatus.SUCCESS;
	}
	
	public static String getInstallLog(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
	    Scanner scanner = new Scanner(file);
	    StringBuilder sb = new StringBuilder();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        sb.append(line);
	        sb.append('\n');
	    }
	    scanner.close();
	    return sb.toString();
	}
	
	public static void deleteFile(String fileName) {
		/**
		 * Delete the temporary firmware file
		 */
		try {
			Path path = new File(fileName).toPath();
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean verifyFirmware(File file, String verifier) throws IOException {
		FileInputStream fis = null;
		String md5 = null;
		try {
			fis = new FileInputStream(file);
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			System.out.println("Downloaded Firmware MD5 sum:: "+ md5);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fis.close();
		}
		if(verifier.equals(md5)) {
			System.out.println("Firmware verification successful");
			return true;
		}
		System.out.println("Download firmware checksum verification failed.. "
				+ "Expected "+verifier + " found "+md5);
		return false;
	}

	public void setGateway(ManagedGateway gwClient) {
		this.gateway = gwClient;
	}
	
	public DeviceInterface getDevice(String key) {
		return this.deviceMap.get(key);
	}
	
	/**
	 * Let us use the WIoTP client Id as the key to identify the device
	 * @return
	 */
	private String getKey(DeviceFirmware firmware) {
		return new StringBuilder("d:").
				append(this.gateway.getOrgId()).
				append(':').
				append(firmware.getTypeId()).
				append(':').
				append(firmware.getDeviceId()).toString();
	}

	public void setExecutor(ExecutorService threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

}
