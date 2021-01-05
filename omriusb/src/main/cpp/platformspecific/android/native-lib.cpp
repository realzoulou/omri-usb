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
#include "demousbtunerinput.h"
#include "ediinput.h"
#include "jni-helper.h"
#include "jtunerusbdevice.h"
#include "jusbdevice.h"
#include "raontunerinput.h"

extern "C" {

const static std::string LOG_TAG{"UsbHelperNative"};

static std::vector<std::shared_ptr<JUsbDevice>> m_usbDevices;

static std::vector<std::unique_ptr<DabUsbTunerInput>> m_dabInputs;

static JavaVM *m_javaVm = nullptr;

static jclass m_usbTunerClass = nullptr;
static jclass m_radioServiceDabImplClass = nullptr;
static jmethodID m_radioServiceDabImpl_init_mId = nullptr;
static jmethodID m_radioServiceDabImpl_setEnsembleEcc_mId = nullptr;
static jmethodID m_radioServiceDabImpl_setEnsembleFrequency_mId = nullptr;
static jmethodID m_radioServiceDabImpl_setEnsembleId_mId = nullptr;
static jmethodID m_radioServiceDabImpl_setServiceId_mId = nullptr;
static jclass m_radioServiceImplClass = nullptr;
static jclass m_dabServiceComponentClass = nullptr;
static jclass m_dabServiceUserApplicationClass = nullptr;
static jclass m_termIdClass = nullptr;
static jclass m_dynamicLabelClass = nullptr;
static jclass m_dynamicLabelPlusItemClass = nullptr;
static jclass m_slideshowClass = nullptr;

static jclass m_ediTunerClass = nullptr;
static jclass m_dabTimeClass = nullptr;

static jclass m_demoTunerClass = nullptr;

static jclass m_ArrayListClass = nullptr;
static jmethodID m_ArrayList_init_mId = nullptr;
static jmethodID m_ArrayList_add_mId = nullptr;

static jboolean m_CoutRedirectedToALog = JNI_FALSE;
static std::string m_rawRecordingPath;

static void cacheClassDefinitions(JavaVM *vm) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);

    // ArrayList
    m_ArrayListClass = (jclass) env->NewGlobalRef(env->FindClass("java/util/ArrayList"));
    m_ArrayList_init_mId = env->GetMethodID(m_ArrayListClass, "<init>", "(I)V");
    m_ArrayList_add_mId = env->GetMethodID(m_ArrayListClass, "add", "(Ljava/lang/Object;)Z");

    m_usbTunerClass = (jclass) env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TunerUsb"));
    m_radioServiceImplClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/RadioServiceImpl"));
    m_radioServiceDabImplClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/RadioServiceDabImpl"));
    m_radioServiceDabImpl_init_mId = env->GetMethodID(m_radioServiceDabImplClass, "<init>", "()V");
    m_radioServiceDabImpl_setEnsembleEcc_mId = env->GetMethodID(m_radioServiceDabImplClass,
                                                                "setEnsembleEcc", "(I)V");
    m_radioServiceDabImpl_setEnsembleFrequency_mId = env->GetMethodID(m_radioServiceDabImplClass,
                                                                      "setEnsembleFrequency",
                                                                      "(I)V");
    m_radioServiceDabImpl_setEnsembleId_mId = env->GetMethodID(m_radioServiceDabImplClass,
                                                               "setEnsembleId", "(I)V");
    m_radioServiceDabImpl_setServiceId_mId = env->GetMethodID(m_radioServiceDabImplClass,
                                                              "setServiceId", "(I)V");
    m_dabServiceComponentClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/RadioServiceDabComponentImpl"));
    m_dabServiceUserApplicationClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/RadioServiceDabUserApplicationImpl"));

    //Metadata classes
    m_termIdClass = (jclass) env->NewGlobalRef(env->FindClass("org/omri/radio/impl/TermIdImpl"));
    m_dynamicLabelClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/TextualDabDynamicLabelImpl"));
    m_dynamicLabelPlusItemClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/TextualDabDynamicLabelPlusItemImpl"));
    m_slideshowClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/VisualDabSlideShowImpl"));

    m_ediTunerClass = (jclass) env->NewGlobalRef(
            env->FindClass("org/omri/radio/impl/TunerEdistream"));
    //DABtime class
    m_dabTimeClass = (jclass) env->NewGlobalRef(env->FindClass("org/omri/radio/impl/DabTime"));

    m_demoTunerClass = (jclass) env->NewGlobalRef(env->FindClass("org/omri/radio/impl/DemoTuner"));
    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

static void cleanClassDefinitions(JavaVM *vm) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);

    env->DeleteGlobalRef(m_ArrayListClass);

    env->DeleteGlobalRef(m_usbTunerClass);
    env->DeleteGlobalRef(m_radioServiceDabImplClass);
    env->DeleteGlobalRef(m_radioServiceImplClass);
    env->DeleteGlobalRef(m_dabServiceComponentClass);
    env->DeleteGlobalRef(m_dabServiceUserApplicationClass);

    //Metadata classes
    env->DeleteGlobalRef(m_termIdClass);
    env->DeleteGlobalRef(m_dynamicLabelClass);
    env->DeleteGlobalRef(m_dynamicLabelPlusItemClass);
    env->DeleteGlobalRef(m_slideshowClass);

    env->DeleteGlobalRef(m_ediTunerClass);
    env->DeleteGlobalRef(m_dabTimeClass);
    env->DeleteGlobalRef(m_demoTunerClass);
    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    //sets the redirect stream for logcat logging
    std::clog.rdbuf(new androidlogbuf("stdwarn", ANDROID_LOG_WARN));
    std::cerr.rdbuf(new androidlogbuf("stderr", ANDROID_LOG_ERROR));

    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    env->GetJavaVM(&m_javaVm);

    cacheClassDefinitions(m_javaVm);

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    std::clog << LOG_TAG << " OnUnload" << std::endl;

    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    env->GetJavaVM(&m_javaVm);

    cleanClassDefinitions(m_javaVm);

    if (m_CoutRedirectedToALog == JNI_TRUE) {
        m_CoutRedirectedToALog = JNI_FALSE;
        delete std::cout.rdbuf(0);
    }
    delete std::clog.rdbuf(0);
    delete std::cerr.rdbuf(0);
}

JNIEXPORT void JNICALL Java_org_omri_radio_impl_UsbHelper_created(JNIEnv *env, jobject thiz,
                                                                  jboolean redirectCoutToALog,
                                                                  jstring rawRecordingPath = nullptr) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

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
              << ",rawRecordingPath=" << m_rawRecordingPath
              << ")" << std::endl;

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_deviceDetached(JNIEnv *env, jobject thiz, jstring deviceName) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    const char *detachedDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::cout << LOG_TAG << " device detached: " << detachedDeviceName << std::endl;

    std::string removedDev(detachedDeviceName);
    env->ReleaseStringUTFChars(deviceName, detachedDeviceName);

    auto bla = m_dabInputs.begin();
    while (bla < m_dabInputs.end()) {
        if (bla->get() != nullptr && bla->get()->getDeviceName() == removedDev) {
            std::cout << LOG_TAG << " Removing UsbTunerInput: " << removedDev << " : "
                      << bla->get()->getDeviceName() << std::endl;
            m_dabInputs.erase(bla);
            break;
        }
        ++bla;
    }

    auto devIter = m_usbDevices.cbegin();
    while (devIter != m_usbDevices.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == removedDev) {
            std::cout << LOG_TAG << " Removing device: " << removedDev << std::endl;
            m_usbDevices.erase(devIter);
            break;
        }
        ++devIter;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_deviceAttached(JNIEnv *env, jobject thiz, jobject usbDevice) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    std::cout << LOG_TAG << " Device attached!" << std::endl;

    std::shared_ptr<JTunerUsbDevice> jusbDevice = std::shared_ptr<JTunerUsbDevice>(
            new JTunerUsbDevice(m_javaVm, env, usbDevice));

    jusbDevice->setJavaClassUsbTuner(env, m_usbTunerClass);
    jusbDevice->setJavaClassDabService(env, m_radioServiceDabImplClass);
    jusbDevice->setJavaClassDabServiceComponent(env, m_dabServiceComponentClass);
    jusbDevice->setJavaClassDabServiceUserApplication(env, m_dabServiceUserApplicationClass);
    jusbDevice->setJavaClassTermId(env, m_termIdClass);

    m_usbDevices.push_back(jusbDevice);

    uint16_t prodId = jusbDevice->getProductId();
    uint16_t vendId = jusbDevice->getVendorId();

    if (vendId == 0x16C0 && prodId == 0x05DC) {
        m_dabInputs.push_back(std::unique_ptr<RaonTunerInput>(
                new RaonTunerInput(jusbDevice, m_rawRecordingPath)));
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_devicePermission(JNIEnv *env, jobject thiz, jstring deviceName,
                                                    jboolean permissionGranted) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    const char *permissionDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string permitDev(permissionDeviceName);
    env->ReleaseStringUTFChars(deviceName, permissionDeviceName);

    std::cout << LOG_TAG << " device permission granted for: " << permitDev << " : "
              << std::boolalpha << static_cast<bool>(permissionGranted) << std::noboolalpha
              << std::endl;

    auto devIter = m_usbDevices.cbegin();
    while (devIter != m_usbDevices.cend()) {
        if (devIter->get() != nullptr) {
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

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_startSrv(JNIEnv *env, jobject thiz, jstring deviceName,
                                            jobject dabService) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " starting service for device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            std::shared_ptr<JDabService> sharedPtr = std::make_shared<JDabService>(m_javaVm, env,
                                                                                   m_radioServiceDabImplClass,
                                                                                   m_dynamicLabelClass,
                                                                                   m_dynamicLabelPlusItemClass,
                                                                                   m_slideshowClass,
                                                                                   dabService);
            (*devIter).get()->startService(sharedPtr);
            break;
        }
        devIter++;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_stopSrv(JNIEnv *env, jobject thiz, jstring deviceName) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " stopping service on device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            (*devIter).get()->stopAllRunningServices();
            break;
        }
        devIter++;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_tuneFreq(JNIEnv *env, jobject thiz, jstring deviceName,
                                            jlong freq) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    std::cerr << LOG_TAG << " tuning to frequency not implemented!" << std::endl;

    //TODO UsbHelper.tuneFreq

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_startServiceScan(JNIEnv *env, jobject thiz, jstring deviceName) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " starting serviceScan on device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            (*devIter).get()->startServiceScan();
            break;
        }
        devIter++;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_stopServiceScan(JNIEnv *env, jobject thiz, jstring deviceName) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);

    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " stopping serviceScan on device: " << devName << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            (*devIter).get()->stopServiceScan();
            break;
        }
        devIter++;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT jobject JNICALL
Java_org_omri_radio_impl_UsbHelper_getLinkedServices(JNIEnv *env, jobject thiz, jstring deviceName,
                                                     jobject dabService) {

    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return nullptr;
    }

    jobject retObj = nullptr;

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::cout << LOG_TAG << " getting service linking services for device: " << devName
              << std::endl;

    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {

            // convert Java input dabService via JDabService to a LinkedServiceDab
            JDabService jDabService(m_javaVm, env, m_radioServiceDabImplClass, m_dynamicLabelClass,
                                    m_dynamicLabelPlusItemClass, m_slideshowClass, dabService);
            LinkedServiceDab inputService(jDabService.getEnsembleEcc(),
                                          jDabService.getServiceId(),
                                          jDabService.getEnsembleId(),
                                          jDabService.getEnsembleFrequency() / 1000);
            // retrieve data
            auto retServices = devIter->get()->getLinkedServices(inputService);

            // build output return data
            retObj = env->NewObject(m_ArrayListClass, m_ArrayList_init_mId,
                                    static_cast<jint>(retServices.size()));
            for (const auto &s : retServices) {

                jobject jLinkedServiceDab = env->NewObject(m_radioServiceDabImplClass,
                                                           m_radioServiceDabImpl_init_mId);
                env->CallVoidMethod(jLinkedServiceDab, m_radioServiceDabImpl_setEnsembleEcc_mId,
                                    static_cast<jint>(s.get()->getEnsembleEcc()));
                env->CallVoidMethod(jLinkedServiceDab,
                                    m_radioServiceDabImpl_setEnsembleFrequency_mId,
                                    static_cast<jint>(s.get()->getEnsembleFrequencyKHz() * 1000));
                env->CallVoidMethod(jLinkedServiceDab, m_radioServiceDabImpl_setEnsembleId_mId,
                                    static_cast<jint>(s.get()->getEnsembleId()));
                env->CallVoidMethod(jLinkedServiceDab, m_radioServiceDabImpl_setServiceId_mId,
                                    static_cast<jint>(s.get()->getServiceId()));

                env->CallBooleanMethod(retObj, m_ArrayList_add_mId, jLinkedServiceDab);
            }
            break;
        }
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
    return retObj;
}

JNIEXPORT jstring JNICALL
Java_org_omri_radio_impl_UsbHelper_getHardwareVersion(JNIEnv *env, jobject thiz,
                                                      jstring deviceName) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return nullptr;
    }

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::string hardwareVersion;
    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            hardwareVersion = (*devIter).get()->getHardwareVersion();
            std::cout << LOG_TAG << " hardware version of device: " << devName << " : " << hardwareVersion << std::endl;
            break;
        }
    }

    jstring retString = env->NewStringUTF(hardwareVersion.c_str());

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
    return retString;
}

JNIEXPORT jstring JNICALL
Java_org_omri_radio_impl_UsbHelper_getSoftwareVersion(JNIEnv *env, jobject thiz,
                                                      jstring deviceName) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return nullptr;
    }

    const char *cDeviceName = env->GetStringUTFChars(deviceName, JNI_FALSE);
    std::string devName(cDeviceName);
    env->ReleaseStringUTFChars(deviceName, cDeviceName);

    std::string softwareVersion;
    auto devIter = m_dabInputs.cbegin();
    while (devIter != m_dabInputs.cend()) {
        if (devIter->get() != nullptr && devIter->get()->getDeviceName() == devName) {
            softwareVersion = (*devIter).get()->getSoftwareVersion();
            std::cout << LOG_TAG << " software version of device: " << devName << " : " << softwareVersion << std::endl;
            break;
        }
    }

    jstring retString = env->NewStringUTF(softwareVersion.c_str());

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
    return retString;
}

/* EdiStream -> highly experimental */
static std::vector<std::shared_ptr<EdiInput>> m_ediInputs;

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_ediTunerAttached(JNIEnv *env, jobject thiz, jobject ediTuner) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    std::cout << LOG_TAG << " EdiTuner attached" << std::endl;

    std::shared_ptr<EdiInput> jediTuner = std::shared_ptr<EdiInput>(
            new EdiInput(m_javaVm, env, ediTuner));
    jediTuner->setJavaClassEdiTuner(env, m_ediTunerClass);
    jediTuner->setJavaClassDabTime(env, m_dabTimeClass);
    jediTuner->setJavaClassDabService(env, m_radioServiceDabImplClass);
    jediTuner->setJavaClassDabServiceComponent(env, m_dabServiceComponentClass);
    jediTuner->setJavaClassDabServiceUserApplication(env, m_dabServiceUserApplicationClass);
    jediTuner->setJavaClassTermId(env, m_termIdClass);

    m_ediInputs.push_back(jediTuner);

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_ediTunerDetached(JNIEnv *env, jobject thiz, jobject ediTuner) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    std::cout << LOG_TAG << " EdiTuner detached" << std::endl;

    //TODO only erase specific tuner instance instead of clear
    m_ediInputs.clear();

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_startEdiStream(JNIEnv *env, jobject thiz, jobject ediTuner,
                                                  jobject dabService) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    m_ediInputs[0]->startService(
            std::make_shared<JDabService>(m_javaVm, env, m_radioServiceDabImplClass,
                                          m_dynamicLabelClass, m_dynamicLabelPlusItemClass,
                                          m_slideshowClass, dabService));

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_ediStreamData(JNIEnv *env, jobject thiz, jbyteArray dabEdiData,
                                                 jint size) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    //std::cout << LOG_TAG << " UsbHelper starting EdistreamService: " << std::endl;
    std::vector<uint8_t> dataArr(size);
    env->GetByteArrayRegion(dabEdiData, 0, size, reinterpret_cast<jbyte *>(dataArr.data()));

    m_ediInputs[0]->ediDataInput(dataArr, size);

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_ediFlushBuffer(JNIEnv *env, jobject thiz) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    std::cout << LOG_TAG << " flushing component data" << std::endl;

    m_ediInputs[0]->flushComponentBuffer();

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

/* Demo Tuner */
static std::shared_ptr<DemoUsbTunerInput> m_demoInput = nullptr;

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_demoTunerAttached(JNIEnv *env, jobject thiz, jobject demoTuner) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    if (m_demoInput == nullptr) {
        std::cout << LOG_TAG << "DemoTuner attached" << std::endl;
        m_demoInput = std::shared_ptr<DemoUsbTunerInput>(new DemoUsbTunerInput(m_javaVm, env));
        m_demoInput->setJavaClassDemoTuner(env, m_demoTunerClass);
        m_demoInput->setJavaObjectDemoTuner(env, demoTuner);
        m_demoInput->setJavaClassRadioService(env, m_radioServiceImplClass);
    } else {
        std::cerr << LOG_TAG << "DemoTuner already attached" << std::endl;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_demoTunerDetached(JNIEnv *env, jobject thiz, jobject demoTuner) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    std::cout << LOG_TAG << "DemoTuner detached" << std::endl;
    if (m_demoInput != nullptr) {
        m_demoInput.reset();
        m_demoInput = nullptr;
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_demoServiceStart(JNIEnv *env, jobject thiz,
                                                    jobject radioService) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    if (m_demoInput != nullptr) {
        std::shared_ptr<JDabService> service = std::make_shared<JDabService>(m_javaVm, env,
                                                                             m_radioServiceDabImplClass,
                                                                             m_dynamicLabelClass,
                                                                             m_dynamicLabelPlusItemClass,
                                                                             m_slideshowClass,
                                                                             radioService);
        m_demoInput->startService(service);
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

JNIEXPORT void JNICALL
Java_org_omri_radio_impl_UsbHelper_demoServiceStop(JNIEnv *env, jobject thiz) {
    bool wasDetached;
    if (!JNI_ATTACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
        return;
    }

    if (m_demoInput != nullptr) {
        m_demoInput->stopAllRunningServices();
    }

    if (!JNI_DETACH(m_javaVm, wasDetached)) {
        std::cerr << LOG_TAG << "jniEnv thread failed to detach!" << std::endl;
    }
}

} // extern "C"
