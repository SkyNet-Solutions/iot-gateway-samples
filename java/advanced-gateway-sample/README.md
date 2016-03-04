Advanced gateway sample
============================================

In this sample, we demonstrate a [sample home gateway](https://github.com/ibm-messaging/gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/HomeGatewaySample.java) that manages few attached home devices like, Lights, Switches, Elevator, Oven and OutdoorTemperature. And the following configuration is assumed,
 
 * Few devices are not manageable
 * Few devices are manageable but accept only firmware
 * Few devices are manageable but accept only Device actions
 * Few devices are manageable and accept both firmware/device actions 
 * All devices publish events and few devices accept commands.

Also, the sample has an [application](https://github.com/ibm-messaging/gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/application/HomeApplication.java) that can be used to control one or more attached devices. For example, turn on/off a particular switch, turn on Oven or control the brightness of the light and etc..

Also, one can use the IBM Watson IoT Platform dashboard to update the firmware, reboot and reset the gateway or devices connected through the gateway.

----

### Prerequisites
To build and run the sample, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

### Build & Run the sample using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the gateway-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/gateway-samples.git`
    
* Import the advanced-gateway-sample project into eclipse using the File->Import option in eclipse.

* Modify the **DMGatewaySample.properties** file with the gateway registration details (Refer below to know how to register the gateway in Watson IoT Platform).

* Also, generate the Organization's API-Key and Token and update the same in **DMGatewaySample.properties** file if the registration mode is manual (as of now, only the manual registration is supported).

* Run the **HomeGatewaySample** by right clicking on the project and selecting "Run as" option.

* Observe that the gateway publishes events for itself and on behalf of the devices connected through it.

* In order to control one or more devices, you need to start the **HomeApplication** present in the project. The application provides list of options to control the devices attached.

----

### Building the sample

* Clone the gateway-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/gateway-samples.git`
    
* Navigate to the advanced-gateway-sample project, 

    `cd gateway-samples\java\advanced-gateway-sample`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform (Currently its shipped as part of this sample, but soon it will be made available in maven central repository), download all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-advanced-gateway-sample-0.0.1.jar.

----

### Register Gateway in IBM Watson IoT Platform

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-gateways-in-ibm-watson-iot-platform/) to register your gateway in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Device-Type = [Your Gateway Device Type]
* Device-ID = [Your Gateway Device ID]
* Authentication-Method = token
* Authentication-Token = [Your Gateway Token]

We need these details to connect the gateway to IBM Watson IoT Platform.

----

### Running the HomeGateway Sample

* Navigate to **target/classes** directory and modify **MGatewaySample.properties** file with the registration details that you noted in the previous step.
* Also, generate the Organization's API-Key and Token and update the same if the registration mode is manual (as of now, only the manual registration is supported)
* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.gateway.HomeGatewaySample"`

**Note:** If there is an Error, try extracting the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location. 

* In order to control one or more devices, one need to start the sample application present in the sample, Run the following command to start the application sample,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.application.HomeApplication"`

Observe that the Application provides list of options to control one or more devices,

Also, In order to push a **firmware to a Gateway/device or reboot Gateway/device**, follow the [part-2 and part-3 of this recipe](https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-2/). This shows how to push a firmware using the Watson IoT Platform dashboard.

----
