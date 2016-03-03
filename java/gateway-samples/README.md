gateway-samples
============================================

This Java project contains 3 samples, which will help you to connect your Gateway and devices behind the Gateway to IBM Watson Internet of Things Platform. All the samples use the Java Client Library for IBM Watson IoT Platform, that simplifies the Gateway interactions with the Platform.

Following are examples present in this project,

* SimpleGatewayExample - A stand-alone basic example that connects a gateway and a device behind the gateway to IBM Watson IoT Platform.
* SampleRasPiGateway - The Gateway support is demonstrated in this sample by connecting the Arduino Uno to Raspberry Pi, where the Raspberry Pi act as a Gateway and publishes events/receives commands on behalf of Arduino Uno to IBM Watson IoT Platform. This sample has a simulator object and can be used in the places where Raspberry Pi and Arduino Uno is not there.Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-gateway-to-watson-iot-platform/) for more information about the sample and how to run the sample in detail?
* ManagedRasPiGateway - Gateway Device Management(DM) capabilities are demonstrated in this sample by managing the Arduino Uno device through the Raspberry Pi Gateway. If you do not have Raspberry Pi and Arduino UNO, don’t worry, you can still follow the sample to connect your device as a gateway and manage one or more attached devices. In this case, you can use your Windows or Linux server as the gateway instead of Raspberry Pi. Also, the sample has a simulator in place of Arduino UNO to respond to gateway requests. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-1/) for more information about the sample and how to run the sample in detail?

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

    git clone https://github.com/ibm-messaging/gateway-samples.git
    
* Navigate to the gateway-samples project, 

    cd gateway-samples\java\gateway-samples
    
* Run the maven build as follows,

    mvn clean package
    
This will download the Java Client library for Watson IoT Platform (Currently its shipped as part of this sample, but soon it will be made available in maven central repository), download all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-gateway-samples-0.0.1.jar.

Running 
