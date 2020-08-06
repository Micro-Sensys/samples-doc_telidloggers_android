# samples-doc_telidloggers_android / Android DOC sample code for TELID®300 dataloggers
This sample code is for handling **TELID®300.nfc** dataloggers on Android devices using the device integrated NFC interface.

[Test LINK: TELID®300.nfc DataLoggers](../doc/communication-modes/spc)
//TODO Add link See "..."

## Requirements
* Android Studio
* Android device to debug/deploy the sample code. The Android device must have NFC functionality integrated
* TELID®300.nfc datalogger

## Implementation
This code shows how to use **TELIDLoggerHandler** class to communicate with a TELID®300.nfc datalogger. 
Using this class and once properly initialized, the handler will automatically detect tapped TELID®300.nfc datalogger and read the information and current status. 
Afterwards a read or program can be started using the functions available.

> Class information is available under API documentation. See [Useful Links](#Useful-Links)

## Steps
Just import this project into Android Studio, connect the Android device to your computer and deploy the SampleApp.
In the *onCreate* function the availability of NFC functionality will be checked. In the *onResume* function there is a snippet to check if the NFC interface is enabled

//TODO screenshot!!
<!--- ![Screenshot](screenshot/SampleApp_SpcControl_AndroidJava.png) --->

 1. Tap TELID®300.nfc logger
 2. Data logger information will be read and provided using **TELIDLoggerCallback**
 3. Once the information is read, the logged protocol can be read using *startReadProtocol*. A new log process can be started using *startProgram*

## Useful Links

 - [AAR Library und API documentation](https://www.microsensys.de/downloads/DevSamples/Libraries/Android/TELID300nfc%20-%20aar%20library/)
 - [Try out our TELID®soft NFC Android application!](https://play.google.com/store/apps/details?id=de.microsensys.telidsoftnfc)

## Contact

* For coding questions or questions about this sample code, you can use [support@microsensys.de](mailto:support@microsensys.de)
* For general questions about the company or our devices, you can contact us using [info@microsensys.de](mailto:info@microsensys.de)

## Authors

* **Victor Garcia** - *Initial work* - [MICS-VGarcia](https://github.com/MICS-VGarcia/)
