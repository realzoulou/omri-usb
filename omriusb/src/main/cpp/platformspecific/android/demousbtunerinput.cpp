/*
 * Copyright (C) 2020 realzoulou
 *
 * Author:
 *  realzoulou
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

#include <iostream>
#include <chrono>
#include <sys/endian.h>
#include "demousbtunerinput.h"

DemoUsbTunerInput::DemoUsbTunerInput(JavaVM* javaVm, JNIEnv* env) {
    std::cout << LOG_TAG << "Constructing...." << std::endl;
    m_javaVm = javaVm;

    m_ensembleFinishedCb = DabEnsemble::registerEnsembleCollectDoneCallback(std::bind(&DemoUsbTunerInput::ensembleCollectFinished, this));
}

DemoUsbTunerInput::~DemoUsbTunerInput() {
    std::cout << LOG_TAG << "Destructing...." << std::endl;

    stopAllRunningServices();

    JNIEnv* env;
    m_javaVm->GetEnv((void **)&env, JNI_VERSION_1_6);
    if (m_demoTunerObject != nullptr) {
        env->DeleteGlobalRef(m_demoTunerObject);
        m_demoTunerObject = nullptr;
    }

    m_demoTunerClass = nullptr;
    m_demoTunerServiceStartedMId = nullptr;
    m_demoTunerServiceStoppedMId = nullptr;

    m_radioServiceClass = nullptr;
    m_radioServiceGetLongDescriptionMId = nullptr;

    if (m_ensembleFinishedCb != nullptr) {
        m_ensembleFinishedCb.reset();
        m_ensembleFinishedCb = nullptr;
    }
}

void DemoUsbTunerInput::initialize() {
    m_isInitialized = true;
}

bool DemoUsbTunerInput::isInitialized() const {
    return m_isInitialized;
}

int DemoUsbTunerInput::getCurrentTunedFrequency() const {
    return m_currentFrequency;
}

void DemoUsbTunerInput::tuneFrequency(int frequencyKHz) {
    m_currentFrequency = static_cast<uint32_t>(frequencyKHz);
}

const DabEnsemble &DemoUsbTunerInput::getEnsemble() const {
    return *this;
}

int DemoUsbTunerInput::getMaximumConcurrentSubChannels() const {
    return MAXIMUM_CONCURRENT_SUBCHANNELS;
}

void DemoUsbTunerInput::addMscCallback(DabInput::CallbackFunction cb, uint8_t subchanId) {
}

void DemoUsbTunerInput::addFicCallback(DabInput::CallbackFunction cb) {
}

void DemoUsbTunerInput::startService(std::shared_ptr<JDabService> serviceLink) {
    if (serviceLink != nullptr) {
        // stop any running service
        stopAllRunningServices();

        // reset all DabEnsemble information
        reset();

        // need to refer to serviceLink, otherwise it may be destroyed
        m_startServiceLink = serviceLink;

        jobject javaDabServiceObject = serviceLink.get()->getJavaDabServiceObject();
        if (javaDabServiceObject != nullptr) {
            std::string description = callJavaRadioServiceGetDescription(javaDabServiceObject);
            std::cout << LOG_TAG << "startService " << description << std::endl;
            inputStreamOpen(description); // description contains the absolute filename
            readDataThreadStart();
        } else {
            std::clog << LOG_TAG << "startService: object null" << std::endl;
        }
    } else {
        std::clog << LOG_TAG << "startService: serviceLink null" << std::endl;
    }
}

void DemoUsbTunerInput::ensembleCollectFinished() {
    std::cout << LOG_TAG << "Ensemble collect finished" << std::endl;
    setService();
}

void DemoUsbTunerInput::setService() {
    bool foundSId = false, foundSrvPrimary = false;
    for (const auto &srv : getDabServices()) {
        if (srv->getServiceId() == m_startServiceLink->getServiceId()) {
            m_startServiceLink->setLinkDabService(srv);

            for (const auto &srvComp : srv->getServiceComponents()) {
                if (srvComp->isPrimary()) {
                    std::cout << LOG_TAG << "Starting SrvComp: " << std::hex
                              << +srvComp->getSubChannelId() << std::dec << std::endl;
                    m_currentSubchanId = srvComp->getSubChannelId();
                    foundSrvPrimary = true;
                    break;
                }
            }

            m_startServiceLink->decodeAudio(true);
            foundSId = true;
            break;
        }
    }
    if (!foundSId) {
        std::clog << LOG_TAG << "setService: not found SId " << std::hex
            << +m_startServiceLink->getServiceId() << std::dec << std::endl;
    } else if (!foundSrvPrimary) {
        std::clog << LOG_TAG << "setService: not found primary srv " << std::hex
            << +m_startServiceLink->getServiceId() << std::dec << std::endl;
    }
}

void DemoUsbTunerInput::stopService(const DabService &service) {
    std::cout << LOG_TAG << "stopService" << std::endl;
    stopAllRunningServices();
}

void DemoUsbTunerInput::stopAllRunningServices() {
    readDataThreadStop();
    inputStreamClose();
    if (m_startServiceLink != nullptr) {
        jobject radioService = m_startServiceLink.get()->getJavaDabServiceObject();
        if (radioService != nullptr) {
            serviceStopped(radioService);
        }
        m_startServiceLink->decodeAudio(false);
        m_startServiceLink->unlinkDabService();
        m_startServiceLink.reset();
        m_startServiceLink = nullptr;
    }
}

void DemoUsbTunerInput::startServiceScan() {
    std::cout << LOG_TAG << "startServiceScan not supported" << std::endl;
}

void DemoUsbTunerInput::stopServiceScan() {
    std::cout << LOG_TAG << "stopServiceScan not supported" << std::endl;
}

std::string DemoUsbTunerInput::getDeviceName() const {
    return "";
}

void DemoUsbTunerInput::setJavaClassDemoTuner(JNIEnv *env, jclass demoTunerClass) {
    // local reference from GlobalRef in OnLoad
    m_demoTunerClass = demoTunerClass;
    m_demoTunerServiceStartedMId = env->GetMethodID(m_demoTunerClass, "serviceStarted", "(Lorg/omri/radioservice/RadioService;)V");
    m_demoTunerServiceStoppedMId = env->GetMethodID(m_demoTunerClass, "serviceStopped", "(Lorg/omri/radioservice/RadioService;)V");
}

void DemoUsbTunerInput::setJavaClassRadioService(JNIEnv *env, jclass radioServiceClass) {
    // local reference from GlobalRef in OnLoad
    m_radioServiceClass = radioServiceClass;
    m_radioServiceGetLongDescriptionMId = env->GetMethodID(m_radioServiceClass, "getLongDescription", "()Ljava/lang/String;");
}

void DemoUsbTunerInput::setJavaObjectDemoTuner(JNIEnv* env, jobject demoTuner) {
    m_demoTunerObject = env->NewGlobalRef(demoTuner);
}

void DemoUsbTunerInput::serviceStarted(jobject radioService) {
    bool wasDetached = false;
    JNIEnv *enve;

    int envState = m_javaVm->GetEnv((void **) &enve, JNI_VERSION_1_6);
    if (envState == JNI_EDETACHED) {
        if (m_javaVm->AttachCurrentThread(&enve, NULL) == 0) {
            wasDetached = true;
        } else {
            std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
            return;
        }
    }

    if (m_demoTunerObject != nullptr
        && m_demoTunerServiceStartedMId != nullptr
        && radioService != nullptr) {

        enve->CallVoidMethod(m_demoTunerObject, m_demoTunerServiceStartedMId, radioService);
    } else {
        std::cout << LOG_TAG << "serviceStarted unable to CallVoidMethod" << std::endl;
    }

    if (wasDetached) {
        m_javaVm->DetachCurrentThread();
    }
}

void DemoUsbTunerInput::serviceStopped(jobject radioService) {
    bool wasDetached = false;
    JNIEnv *enve;

    int envState = m_javaVm->GetEnv((void **) &enve, JNI_VERSION_1_6);
    if (envState == JNI_EDETACHED) {
        if (m_javaVm->AttachCurrentThread(&enve, NULL) == 0) {
            wasDetached = true;
        } else {
            std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
            return;
        }
    }

    if (m_demoTunerObject != nullptr
        && m_demoTunerServiceStoppedMId != nullptr
        && radioService != nullptr) {

        enve->CallVoidMethod(m_demoTunerObject, m_demoTunerServiceStoppedMId, radioService);

    } else {
        std::cout << LOG_TAG << "serviceStopped unable to CallVoidMethod" << std::endl;
    }

    if (wasDetached) {
        m_javaVm->DetachCurrentThread();
    }
}

std::string DemoUsbTunerInput::callJavaRadioServiceGetDescription(jobject radioService) {
    bool wasDetached = false;
    JNIEnv *enve;
    jstring jstr;

    int envState = m_javaVm->GetEnv((void **) &enve, JNI_VERSION_1_6);
    if (envState == JNI_EDETACHED) {
        if (m_javaVm->AttachCurrentThread(&enve, NULL) == 0) {
            wasDetached = true;
        } else {
            std::cerr << LOG_TAG << "jniEnv thread failed to attach!" << std::endl;
            return "";
        }
    }
    const char *strReturn;
    std::string retString("");
    if (radioService != nullptr
        && m_radioServiceGetLongDescriptionMId != nullptr ) {

        jstr = (jstring) enve->CallObjectMethod(radioService, m_radioServiceGetLongDescriptionMId);
        strReturn = enve->GetStringUTFChars(jstr, 0);
        retString = strReturn;
        enve->ReleaseStringUTFChars(jstr, strReturn);
    }

    if (wasDetached) {
        m_javaVm->DetachCurrentThread();
    }

    return retString;
}


/**
 * Private methods
 */

void DemoUsbTunerInput::readDataThreadStart() {
    if (m_readThread != nullptr) {
        readDataThreadStop();
    }
    m_readThreadRunning = true;
    m_readThread = new std::thread(&DemoUsbTunerInput::readThreadProc, this);
}

void DemoUsbTunerInput::readDataThreadStop() {
    if (m_readThread != nullptr && m_readThreadRunning) {
        m_readThreadRunning = false;
        if (m_readThread->joinable()) {
            m_readThread->join();
        }
        delete m_readThread;
        m_readThread = nullptr;
    }
}

void DemoUsbTunerInput::readThreadProc() {
    pthread_setname_np(pthread_self(), "DemoTunerRead");

    if (m_startServiceLink == nullptr) {
        m_readThreadRunning = false;
        std::clog << LOG_TAG << "readThreadProc: startServiceLink null" << std::endl;
        return;
    }
    jobject radioService = m_startServiceLink.get()->getJavaDabServiceObject();

    if (radioService == nullptr) {
        m_readThreadRunning = false;
        std::clog << LOG_TAG << "readThreadProc: radioService null" << std::endl;
        return;
    }
    if (m_inFileStream.bad()) {
        m_readThreadRunning = false;
        std::clog << LOG_TAG << "readThreadProc: input stream bad" << std::endl;
        return;
    }
    if (!m_inFileStream.is_open()) {
        m_readThreadRunning = false;
        std::clog << LOG_TAG << "readThreadProc: input stream not open" << std::endl;
        return;
    }

    serviceStarted(radioService);

    // buffer for reading from file (except for the actual huge data)
    char buffer[8]; // biggest element to buffer is a uint64_t
    uint64_t prevFileTimeMs;
    uint32_t frameCount;
    bool error_occured;

    // label to use for returning to begin of file when EOF was reached
file_at_beginning:

    m_inFileStream.clear();
    m_inFileStream.seekg(0, m_inFileStream.beg);
    prevFileTimeMs = 0;
    frameCount = 0;  // for logging purpose only
    error_occured = false;

    while (m_readThreadRunning && !m_inFileStream.eof()) {
        uint32_t marker, dataSize;
        uint64_t fileTimeMs;
        int64_t filePos;
        auto timeBeforeFrame = std::chrono::steady_clock::now();

        if (error_occured) { // in previous loop !
            // to be able to see logs online
            std::this_thread::sleep_for(std::chrono::milliseconds(1000));
            error_occured = false;
        }

        frameCount++;
        filePos = m_inFileStream.tellg();

        // read marker
        m_inFileStream.read(buffer, sizeof(uint32_t));
        if (m_inFileStream.bad()) {
            std::clog << LOG_TAG << "readThreadProc: failed to read marker"
                << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
            error_occured = true;
            continue;
        }
        marker = ntohl(*((uint32_t*)&buffer[0]));

        // read recording timestamp in millisec (monotonic)
        m_inFileStream.read(buffer, sizeof(uint64_t));
        if (m_inFileStream.bad()) {
            std::clog << LOG_TAG << "readThreadProc: failed to read timestamp"
                    << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
            error_occured = true;
            continue;
        }
        fileTimeMs = ntohq(*((uint64_t*)&buffer[0]));
        if (prevFileTimeMs == 0) {
            prevFileTimeMs = fileTimeMs; // first loop
        } else if (prevFileTimeMs > fileTimeMs) {
            std::clog << LOG_TAG << "readThreadProc: file timestamp " << fileTimeMs
                    << " lower than previous " << prevFileTimeMs
                    << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
            error_occured = true;
            continue;
        }

        // read data vector size
        m_inFileStream.read(buffer, sizeof(uint32_t));
        if (m_inFileStream.bad()) {
            std::clog << LOG_TAG << "readThreadProc: failed to read data len"
                    << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
            error_occured = true;
            continue;
        }
        dataSize = ntohl(*((uint32_t*)&buffer[0]));
        if (dataSize > 100*1024) { // 100 KB is implausible
            std::clog << LOG_TAG << "readThreadProc: data size implausible" << dataSize
                      << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
            error_occured = true;
            continue;
        }

        if (dataSize > 0) {
            // read data vector
            std::vector<uint8_t> vec(dataSize);
            m_inFileStream.read((char*)vec.data(), dataSize);
            if (m_inFileStream.bad()) {
                std::clog << LOG_TAG << "readThreadProc: failed to read data of size "
                          << dataSize
                          << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
                if (vec.size() >= 3) {
                    std::clog << LOG_TAG << "readThreadProc: data 0..2: " << std::hex
                              << +vec[0] << " " << +vec[1] << " " << +vec[2] << std::dec << std::endl;
                }
                error_occured = true;
                continue;
            }

            // log every 100th frame
            if (frameCount % 100 == 1) { // 1 because we start counting frames at 1, not 0
                std::cout << LOG_TAG << "readThreadProc frame " << frameCount
                          << " @filepos " << filePos
                          << ": marker " << std::hex << marker << std::dec
                          << ", time " << fileTimeMs
                          << ", size " << dataSize;
                if (vec.size() >= 3) {
                    std::cout<< ", data 0..2: 0x" << std::hex
                              << +vec[0] << " " << +vec[1] << " " << +vec[2] << std::dec;
                }
                std::cout << std::endl;
            }

            if (FILEMARKER_MSC == marker) {
                dataInput(vec, m_currentSubchanId, false);
            } else if (FILEMARKER_FIC == marker) {
                dataInput(vec, 0x64, false);
            } else {
                std::clog << LOG_TAG << "readThreadProc: invalid marker 0x" << std::hex << marker << std::dec
                          << " at file pos " << std::dec << m_inFileStream.tellg() << std::endl;
                error_occured = true;
                continue;
            }
        }

        // throttle reading from file to best match with the recording time sequence
        auto timeAfterFrame = std::chrono::steady_clock::now();
        int64_t frameProcessDurationMs = std::chrono::duration_cast<std::chrono::milliseconds>
                (timeAfterFrame - timeBeforeFrame).count();
        int64_t sleepForMs = fileTimeMs - prevFileTimeMs;
        if (sleepForMs > frameProcessDurationMs) { // don't allow to get negative sleepForMs
            sleepForMs -= frameProcessDurationMs;
        }
        if (sleepForMs > 100) {
            sleepForMs -= 50; // be slightly faster than realtime to mitigate stuttering
        }
        if (sleepForMs >= 0) {
            std::this_thread::sleep_for(std::chrono::milliseconds(sleepForMs));
        } else {
            std::clog << LOG_TAG << "readThreadProc: timestamp diff implausible " << sleepForMs << std::endl;
            std::this_thread::sleep_for(std::chrono::milliseconds(500));
        }
        prevFileTimeMs = fileTimeMs;
    }

    if (m_inFileStream.eof() && m_readThreadRunning) {
        std::cout << LOG_TAG << "readThreadProc: EOF, rewinding ..." << std::endl;
        std::this_thread::sleep_for(std::chrono::milliseconds(1000));
        reset();
        std::this_thread::sleep_for(std::chrono::milliseconds(1000));
        goto file_at_beginning;
    }

    m_readThreadRunning = false;
}

void DemoUsbTunerInput::inputStreamOpen(const std::string & filename) {
    inputStreamClose();
    m_inFileStream.open(filename, std::ios::in | std::ios::binary);
    // Stop eating new lines in binary mode
    m_inFileStream.unsetf(std::ios::skipws);
}

void DemoUsbTunerInput::inputStreamClose() {
    if (m_inFileStream.is_open()) {
        m_inFileStream.close();
    }
}
