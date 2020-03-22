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

#ifndef DAB_DEMOUSBTUNERINPUT_H
#define DAB_DEMOUSBTUNERINPUT_H

#include <fstream>
#include <thread>
#include <memory>
#include <stdint.h>
#include "dabusbtunerinput.h"
#include "jtunerusbdevice.h"
#include "jdabservice.h"


class DemoUsbTunerInput final : public DabUsbTunerInput, DabEnsemble {

public:
    constexpr static uint32_t FILEMARKER_MSC = 0xDEADBEEF;
    constexpr static uint32_t FILEMARKER_FIC = 0xDEADAFFE;

public:
    explicit DemoUsbTunerInput(JavaVM* javaVm, JNIEnv* env);
    virtual ~DemoUsbTunerInput();

    //delete copy and assignment constructors
    DemoUsbTunerInput(DemoUsbTunerInput &) = delete;
    void operator=(DemoUsbTunerInput &) = delete;

    void initialize() override;
    bool isInitialized() const override;
    int getCurrentTunedFrequency() const override;
    void tuneFrequency(int frequencyKHz) override;
    const DabEnsemble &getEnsemble() const override;
    int getMaximumConcurrentSubChannels() const override;
    void addMscCallback(CallbackFunction cb, uint8_t subchanId) override;
    void addFicCallback(CallbackFunction cb) override;
    void startService(std::shared_ptr <JDabService> serviceLink) override;
    void stopService(const DabService &service) override;
    void stopAllRunningServices() override;
    void startServiceScan() override;
    void stopServiceScan() override;
    std::string getDeviceName() const override;

public:
    void setJavaClassDemoTuner(JNIEnv* env, jclass demoTunerClass);
    void setJavaClassRadioService(JNIEnv* env, jclass radioService);
    void setJavaObjectDemoTuner(JNIEnv* env, jobject demoTuner);

public: // callbacks to Java code
    void serviceStarted(jobject radioService);
    void serviceStopped(jobject radioService);
    std::string callJavaRadioServiceGetDescription(jobject radioService);

private:
    void setService();
    void ensembleCollectFinished();

private:
    void readDataThreadStart();
    void readDataThreadStop();
    void readThreadProc();
    void inputStreamOpen(const std::string & filename);
    void inputStreamClose();

private:
    const std::string LOG_TAG{"[DemoTuner] "};
    constexpr static uint8_t MAXIMUM_CONCURRENT_SUBCHANNELS{1};

    JavaVM* m_javaVm;

    jclass m_demoTunerClass{nullptr};
    jobject m_demoTunerObject{nullptr};
    jmethodID m_demoTunerServiceStartedMId{nullptr};
    jmethodID m_demoTunerServiceStoppedMId{nullptr};

    jclass m_radioServiceClass{nullptr};
    jmethodID m_radioServiceGetLongDescriptionMId{nullptr};

    bool m_isInitialized{false};
    uint32_t m_currentFrequency{0};
    uint8_t m_currentSubchanId{0xFF};

    std::shared_ptr<JDabService> m_startServiceLink{nullptr};
    std::shared_ptr<std::function<void()>> m_ensembleFinishedCb;

    std::atomic<bool> m_readThreadRunning{false};
    std::thread * m_readThread{nullptr};
    std::ifstream m_inFileStream;
};

#endif //DAB_DEMOUSBTUNERINPUT_H
