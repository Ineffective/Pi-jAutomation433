#include "NativeRCSwitchAdapter.h"
#include "RCSwitch.h"
#include <stdio.h>
#include <stdlib.h>

#include <iostream>
#include <wiringPi.h>

using namespace std;

RCSwitch mySwitch = RCSwitch();


JNIEXPORT void JNICALL Java_NativeRCSwitchAdapter_switchOn(JNIEnv * env, jobject obj, jstring jsGroup, jstring jsChannel ){
    if (wiringPiSetup () == -1) {
       printf("noWiringPiSetup");
    }

    const char *csGroup = env->GetStringUTFChars(jsGroup, 0);
    const char *csChannel = env->GetStringUTFChars(jsChannel, 0);

    char sGroup[6];
    char sChannel[6];

    for (int i = 0; i<5; i++) {
        sGroup[i] = csGroup[i];
        sChannel[i] = csChannel[i];
    }
    sGroup[5] = '\0';
    sChannel[5] = '\0';

    cout<<"ON with"<<sGroup<<"and "<< sChannel <<endl;
    piHiPri(20);
    //for testing purposes set to the ELRO Power Plugs
    mySwitch.setPulseLength(300);
    mySwitch.enableTransmit(0);
    mySwitch.setRepeatTransmit(3);

    mySwitch.switchOn(sGroup, sChannel);


}

JNIEXPORT void JNICALL Java_NativeRCSwitchAdapter_switchOff(JNIEnv * env, jobject obj, jstring jsGroup, jstring jsChannel ){
    if (wiringPiSetup () == -1) {
            printf("noWiringPiSetup");
    }

    const char *csGroup = env->GetStringUTFChars(jsGroup, 0);
    const char *csChannel = env->GetStringUTFChars(jsChannel, 0);

    char sGroup[6];
    char sChannel[6];

    for (int i = 0; i<5; i++) {
        sGroup[i] = csGroup[i];
        sChannel[i] = csChannel[i];
    }
    sGroup[5] = '\0';
    sChannel[5] = '\0';

    cout<<"OFF with "<<sGroup<<" and "<< sChannel <<endl;


    piHiPri(20);


    //for testing purposes set to the ELRO Power Plugs
    mySwitch.setPulseLength(300);
    mySwitch.enableTransmit(0);
    mySwitch.setRepeatTransmit(3);

    mySwitch.switchOff(sGroup, sChannel);


}



JNIEXPORT void JNICALL Java_NativeRCSwitchAdapter_sendBinary (JNIEnv *env, jobject obj, jstring javaString)
{
    //Get the native string from javaString
    //const char *nativeString = env->GetStringUTFChars(javaString, 0);
    //Do something with the nativeString
    //cout<<nativeString<<endl;
    //DON'T FORGET THIS LINE!!!
    //env->ReleaseStringUTFChars(javaString, nativeString);

    //turning on an LED on pin 0
    wiringPiSetup ();

      RCSwitch mySwitch = RCSwitch();
      mySwitch.setPulseLength(300);
      mySwitch.enableTransmit(0);
      mySwitch.setRepeatTransmit(4);

      mySwitch.switchOn("10101", "11111");



}
