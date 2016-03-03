gateway-samples
============================================

This Java project contains 3 samples, which will help you to connect your Gateway and devices behind the Gateway to IBM Watson Internet of Things Platform. All the samples use the Java Client Library for IBM Watson IoT Platform, that simplifies the Gateway interactions with the Platform.

Following are examples present in this project,

* SimpleGatewayExample
* SampleRasPiGateway
* ManagedRasPiGateway
----

Prerequisites
=============
To build and run the samples, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

Building the samples
=====================

* Clone the gateway-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/gateway-samples.git`
    
* Navigate to the gateway-samples project, 

    `cd gateway-samples\java\gateway-samples`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform (Currently its shipped as part of this sample, but soon it will be made available in maven central repository), download all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-gateway-samples-0.0.1.jar.

Register Gateway in IBM Watson IoT Platform
===========================================

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-gateways-in-ibm-watson-iot-platform/) to register your gateway in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Gateway-Type = Your Gateway Device Type
* Gateway-ID = Your Gateway Device ID
* Authentication-Method = token
* Authentication-Token = Your Gateway Token

We need these details to connect the gateway to IBM Watson IoT Platform.
----

Running SimpleGatewayExample
============================
A stand-alone sample that connects a gateway and a device behind the gateway to IBM Watson IoT Platform. 

* Navigate to **target/classes** directory and modify **gateway.properties** file with the registration details that you noted in the previous step.
* Also, generate the Organization's API-Key and Token and update the same if the registration mode is manual (as of now, only the manual registration is supported)
* Run the sample using the following command
    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.gateway.SimpleGatewayExample"`

**Note:** If there is an Error, try extracting the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location. 
----

Running SampleRasPiGateway
============================
The Gateway support is demonstrated in this sample by connecting the Arduino Uno to Raspberry Pi, where the Raspberry Pi act as a Gateway and publishes events/receives commands on behalf of Arduino Uno to IBM Watson IoT Platform. This sample has a simulator object and can be used in the places where Raspberry Pi and Arduino Uno is not there. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-gateway-to-watson-iot-platform/) for more information about the sample and how to run the sample in detail?

* Run the sample using the following command
    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.gateway.SampleRasPiGateway"`

**Note:** If there is an Error, try extracting the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location. 
----

Running ManagedRasPiGateway
============================
**Gateway Device Management(DM)** capabilities are demonstrated in this sample by managing the Arduino Uno device through the Raspberry Pi Gateway. If you do not have Raspberry Pi and Arduino UNO, don’t worry, you can still follow the sample to connect your device as a gateway and manage one or more attached devices. In this case, you can use your Windows or Linux server as the gateway instead of Raspberry Pi. Also, the sample has a simulator in place of Arduino UNO to respond to gateway requests. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-1/) for more information about the sample and how to run the sample in detail?

* Run the sample using the following command
    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.gateway.devicemgmt.ManagedRasPiGateway"`

**Note:** If there is an Error, try extracting the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location. 
----
