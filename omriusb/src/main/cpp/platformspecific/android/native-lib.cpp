/*
 * Copyright (C) 2018 IRT GmbH
 *
 * Author:
 *  Fabian Sattler
 *
 * This file is a part of IRT DAB library.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 */

#include <jni.h>

#include <string>
#include <vector>
#include <memory>

#include "androidlogbuf.h"

#include "jtunerusbdevice.h"
#include "jusbdevice.h"
#include "raontunerinput.h"
#include "demousbtunerinput.h"
#include "ediinput.h"

extern "C" {

const std::string LOG_TAG{"UsbHelperNative"};

std::vector<std::shared_ptr<JUsbDevice>> m_usbDevices;

std::vector<std::unique_ptr<DabUsbTunerInput>> m_dabInputs;

JavaVM* m_javaVm;

jclass m_usbTunerClass = nullptr;
jclass m_dabServiceClass = nullptr;
jclass m_radioServiceClass = nullptr;
jclass m_dabServiceComponentClass = nullptr;
jclass m_dabServiceUserApplicationClass = nullptr;
jclass m_termIdClass = nullptr;
jclass m_dynamicLabelClass = nullptr;
jclass m_dynamicLabelPlusItemClass = nullptr;
jclass m_slideshowClass = nullptr;

jclass m_ediTunerClass = nullptr;
jclass m_dabTimeClass = nullptr;

jclass m_demoTunerClass = nullptr;

jboolean m_CoutRedirectedToALog = JNI_FALSE;
std::string m_rawRecordingPath{""};

void cacheClassDefinitions(JavaVM *vm) {
    JNIEnv* env;
    vm->GetEnv ((void **) &env, JNI_VERSION_1_6);

    m_usbTunerClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TunerUsb"));
    m_dabServiceClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/RadioServiceDabImpl"));
    m_radioServiceClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/RadioServiceImpl"));
    m_dabServiceComponentClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/RadioServiceDabComponentImpl"));
    m_dabServiceUserApplicationClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/RadioServiceDabUserApplicationImpl"));

    //Metadata classes
    m_termIdClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TermIdImpl"));
    m_dynamicLabelClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TextualDabDynamicLabelImpl"));
    m_dynamicLabelPlusItemClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TextualDabDynamicLabelPlusItemImpl"));
    m_slideshowClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/VisualDabSlideShowImpl"));

    m_ediTunerClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TunerEdistream"));
    //DABtime class
    m_dabTimeClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/DabTime"));

    m_demoTunerClass = (jclass)env->NewGlobalRef(env->FindClass("org/omri/radio/impl/DemoTuner"));
}

void cleanClassDefinitions(JavaVM *vm) {
    JNIEnv* env;
    vm->GetEnv ((void **) &env, JNI_VERSION_1_6);

    env->DeleteGlobalRef(m_usbTunerClass);
    env->DeleteGlobalRef(m_dabServiceClass);
    env->DeleteGlobalRef(m_radioServiceClass);
    env->DeleteGlobalRef(m_dabServiceComponentClass);
    env->DeleteGlobalRef(m_dabServiceUserApplicationClass);

    //Metadata classes
    env->DeleteGlobalRef(m_termIdClass);
    env->DeleteGlobalRef(m_dynamicLabelClass);
    env->DeleteGlobalRef(m_dynamicLabelPlusItemClass);
    env->DeleteGlobalRef(m_slideshowClass);

    env->DeleteGlobalRef(m_ediTunerClass);
    //DABtime class
    env->DeleteGlobalRef(m_dabTimeClass);

    env->DeleteGlobalRef(m_demoTunerClass);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    //sets the redirect stream for logcat logging
    std::clog.rdbuf(new androidlogbuf("stdwarn", ANDROID_LOG_WARN));
    std::cerr.rdbuf(new androidlogbuf("stderr", ANDROID_LOG_ERROR));

    JNIEnv* env;
    vm->GetEnv((void **)&env, JNI_VERSION_1_6);
    env->GetJavaVM(&m_javaVm);

    cacheClassDefinitions(m_javaVm);

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    std::clog << LOG_TAG << " OnUnload" << std::endl;

    JNIEnv* env;
    vm->GetEnv((void **)&env, JNI_VERSION_1_6);
    env->GetJavaVM(&m_javaVm);

    cleanClassDefinitions(m_javaVm);

    if (m_CoutRedirectedToALog == JNI_TRUE) {
        m_CoutRedirectedToALog = JNI_FALSE;
        delete std::cout.rdbuf(0);
    }
    delete std::clog.rdbuf(0);
    delete std::cerr.rdbuf(0);
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_created(JNIEnv* env, jobject thiz,
        jboolean redirectCoutToALog, jstring rawRecordingPath = nullptr) {
    if (JNI_TRUE == redirectCoutToALog) {
        m_CoutRedirectedToALog = JNI_TRUE;
        std::cout.rdbuf(new androidlogbuf);
    }
    if (rawRecordingPath != nullptr) {
        const char *path = env->GetStringUTFChars(rawRecordingPath, JNI_FALSE);
        if (path != nullptr) {
            m_rawRecordingPath = path;
        }
    }
    std::cout << LOG_TAG << " created (redirectCout="
        << std::boolalpha << (JNI_TRUE == m_CoutRedirectedToALog) << std::noboolalpha
        << ",rawRecordingPath=" <<  m_rawRecordingPath
        << ")"<< std::endl;
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_deviceDetached(JNIEnv* env, jobject thiz, jstring deviceName) {
    const char* detachedDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::cout << LOG_TAG << " device detached: " << detachedDeviceName << std::endl;

    std::string removedDev(detachedDeviceName);

    auto bla = m_dabInputs.begin();
    while(bla < m_dabInputs.end()) {
        if(bla->get() != nullptr && bla->get()->getDeviceName() == removedDev) {
            std::cout << LOG_TAG << " Removing UsbTunerInput: " << removedDev << " : " << bla->get()->getDeviceName() << std::endl;
            m_dabInputs.erase(bla);
            break;
        }
        ++bla;
    }

    auto devIter = m_usbDevices.cbegin();
    while(devIter != m_usbDevices.cend()) {
        if(devIter->get() != nullptr && devIter->get()->getDeviceName() == removedDev) {
            std::cout << LOG_TAG << " Removing device: " << removedDev << std::endl;
            m_usbDevices.erase(devIter);
            break;
        }
        ++devIter;
    }

    env->ReleaseStringUTFChars(deviceName, detachedDeviceName);
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_deviceAttached(JNIEnv* env, jobject thiz, jobject usbDevice) {
    std::cout << LOG_TAG << " Device attached!" << std::endl;

    std::shared_ptr<JTunerUsbDevice> jusbDevice = std::shared_ptr<JTunerUsbDevice>(new JTunerUsbDevice(m_javaVm, env, usbDevice));

    jusbDevice->setJavaClassUsbTuner(env, m_usbTunerClass);
    jusbDevice->setJavaClassDabService(env, m_dabServiceClass);
    jusbDevice->setJavaClassDabServiceComponent(env, m_dabServiceComponentClass);
    jusbDevice->setJavaClassDabServiceUserApplication(env, m_dabServiceUserApplicationClass);
    jusbDevice->setJavaClassTermId(env, m_termIdClass);

    m_usbDevices.push_back(jusbDevice);

    uint16_t prodId = jusbDevice->getProductId();
    uint16_t vendId = jusbDevice->getVendorId();

    if(vendId == 0x16C0 && prodId == 0x05DC) {
        m_dabInputs.push_back(std::unique_ptr<RaonTunerInput>(new RaonTunerInput(jusbDevice, m_rawRecordingPath)));
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_devicePermission(JNIEnv* env, jobject thiz, jstring deviceName, jboolean permissionGranted) {
    const char* permissionDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string permitDev(permissionDeviceName);
    env->ReleaseStringUTFChars(deviceName, permissionDeviceName);

    std::cout << LOG_TAG << " device permission granted for: " << permitDev << " : " <<  std::boolalpha << static_cast<bool>(permissionGranted) << std::noboolalpha << std::endl;

    auto devIter = m_usbDevices.cbegin();
    while(devIter != m_usbDevices.cend()) {
        if(devIter->get() != nullptr) {
            std::cout << LOG_TAG << " searching device: " << devIter->get()->getDeviceName()
                      << std::endl;
            if (devIter->get()->getDeviceName() == permitDev) {
                std::cout << LOG_TAG << " Permission device: " << permitDev << std::endl;

                devIter->get()->permissionGranted(env, static_cast<bool>(permissionGranted));
                break;
            }
        }
        ++devIter;
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_startSrv(JNIEnv* env, jobject thiz, jstring deviceName, jobject dabService) {
    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " starting service for device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while(devIter != m_dabInputs.cend()) {
        if(devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            std::shared_ptr<JDabService> sharedPtr = std::make_shared<JDabService>(m_javaVm, env, m_dabServiceClass, m_dynamicLabelClass, m_dynamicLabelPlusItemClass, m_slideshowClass, dabService);
            (*devIter).get()->startService(sharedPtr);
            break;
        }
        devIter++;
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_stopSrv(JNIEnv* env, jobject thiz, jstring deviceName) {
    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " stopping service on device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while(devIter != m_dabInputs.cend()) {
        if(devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            (*devIter).get()->stopAllRunningServices();
            break;
        }
        devIter++;
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_tuneFreq(JNIEnv* env, jobject thiz, jstring deviceName, jlong freq) {
    std::cerr << LOG_TAG << " tuning to frequency not implemented!" << std::endl;

    //TODO
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_startServiceScan(JNIEnv* env, jobject thiz, jstring deviceName) {
    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " starting serviceScan on device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while(devIter != m_dabInputs.cend()) {
        if(devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            (*devIter).get()->startServiceScan();
            break;
        }
        devIter++;
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_stopServiceScan(JNIEnv* env, jobject thiz, jstring deviceName) {
    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " stopping serviceScan on device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while(devIter != m_dabInputs.cend()) {
        if(devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            (*devIter).get()->stopServiceScan();
            break;
        }
        devIter++;
    }
}

/* EdiStream -> highly experimental */
std::vector<std::shared_ptr<EdiInput>> m_ediInputs;

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_ediTunerAttached(JNIEnv* env, jobject thiz, jobject ediTuner) {
    std::cout << LOG_TAG << " EdiTuner attached" << std::endl;

    std::shared_ptr<EdiInput> jediTuner = std::shared_ptr<EdiInput>(new EdiInput(m_javaVm, env, ediTuner));
    jediTuner->setJavaClassEdiTuner(env, m_ediTunerClass);
    jediTuner->setJavaClassDabTime(env, m_dabTimeClass);
    jediTuner->setJavaClassDabService(env, m_dabServiceClass);
    jediTuner->setJavaClassDabServiceComponent(env, m_dabServiceComponentClass);
    jediTuner->setJavaClassDabServiceUserApplication(env, m_dabServiceUserApplicationClass);
    jediTuner->setJavaClassTermId(env, m_termIdClass);

    m_ediInputs.push_back(jediTuner);
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_ediTunerDetached(JNIEnv* env, jobject thiz, jobject ediTuner) {
    std::cout << LOG_TAG << " EdiTuner detached" << std::endl;

    //TODO only erase specific tuner instance instead of clear
    m_ediInputs.clear();
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_startEdiStream(JNIEnv* env, jobject thiz, jobject ediTuner, jobject dabService) {
    m_ediInputs[0]->startService(std::make_shared<JDabService>(m_javaVm, env, m_dabServiceClass, m_dynamicLabelClass, m_dynamicLabelPlusItemClass, m_slideshowClass, dabService));
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_ediStreamData(JNIEnv* env, jobject thiz, jbyteArray dabEdiData, jint size) {
    //std::cout << LOG_TAG << " UsbHelper starting EdistreamService: " << std::endl;
    std::vector<uint8_t> dataArr(size);
    env->GetByteArrayRegion (dabEdiData, 0, size, reinterpret_cast<jbyte*>(dataArr.data()));

    m_ediInputs[0]->ediDataInput(dataArr, size);
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_ediFlushBuffer(JNIEnv* env, jobject thiz) {
    std::cout << LOG_TAG << " flushing component data" << std::endl;

    m_ediInputs[0]->flushComponentBuffer();
}

/* Demo Tuner */
std::shared_ptr<DemoUsbTunerInput> m_demoInput = nullptr;

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_demoTunerAttached(JNIEnv* env, jobject thiz, jobject demoTuner) {
    if (m_demoInput == nullptr) {
        std::cout << LOG_TAG << "DemoTuner attached" << std::endl;
        m_demoInput = std::shared_ptr<DemoUsbTunerInput>(new DemoUsbTunerInput(m_javaVm, env));
        m_demoInput->setJavaClassDemoTuner(env, m_demoTunerClass);
        m_demoInput->setJavaObjectDemoTuner(env, demoTuner);
        m_demoInput->setJavaClassRadioService(env, m_radioServiceClass);
    } else {
        std::cerr << LOG_TAG << "DemoTuner already attached" << std::endl;
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_demoTunerDetached(JNIEnv* env, jobject thiz, jobject demoTuner) {
    std::cout << LOG_TAG << "DemoTuner detached" << std::endl;
    if (m_demoInput != nullptr) {
        m_demoInput.reset();
        m_demoInput = nullptr;
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_demoServiceStart(JNIEnv* env, jobject thiz, jobject radioService) {
    if (m_demoInput != nullptr) {
        std::shared_ptr<JDabService> service = std::make_shared<JDabService>(m_javaVm, env, m_dabServiceClass, m_dynamicLabelClass,
            m_dynamicLabelPlusItemClass, m_slideshowClass, radioService);
        m_demoInput->startService(service);
    }
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_demoServiceStop(JNIEnv* env, jobject thiz) {
    if (m_demoInput != nullptr) {
        m_demoInput->stopAllRunningServices();
    }
}

} // extern "C"
