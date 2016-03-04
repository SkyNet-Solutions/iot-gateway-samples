# iot-gateway-samples
This repository contains samples for connecting your gateway to the IBM Watson Internet of Things Platform and perform one or more operations. Currently, there are samples for Java; information and instructions regarding the use of these samples can be found in their respective directories.

Gateways are a specialized class of devices in Watson IoT Platform which serve as access points to the Watson IoT Platform for other devices. Gateway devices have additional permission when compared to regular devices and can perform the following  functions:

* Register new devices to Watson IoT Platform
* Send and receive its own sensor data like a directly connected device,
* Send and receive data on behalf of the devices connected to it
* Run a device management agent, so that it can be managed, also manage the devices connected to it

Refer to the [documentation](https://docs.internetofthings.ibmcloud.com/gateways/mqtt.html) for more information about the Gateway support in Watson IoT Platform.
