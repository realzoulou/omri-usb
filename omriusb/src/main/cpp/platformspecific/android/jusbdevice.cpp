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

#include "jusbdevice.h"

#include "iostream"

#include <linux/usbdevice_fs.h>
#include <sys/ioctl.h>

// POSIX_IOCTL_READ_WRITE_BULKTRANSFER
// 0 : Use Android UsbDeviceConnection (with immense overhead for JNI)
// 1 : Use POSIX ioctl() to read/write (no overhead, direct kernel communication)
#define POSIX_IOCTL_READ_WRITE_BULKTRANSFER 1

JUsbDevice::JUsbDevice(JavaVM* javaVm, JNIEnv *env, jobject usbDevice) {
    m_javaVm = javaVm;

    //UsbHelper class definitions
    m_usbHelperClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("org/omri/radio/impl/UsbHelper")));
    m_usbHelperGetInstanceMId = env->GetStaticMethodID(m_usbHelperClass, "getInstance", "()Lorg/omri/radio/impl/UsbHelper;");
    m_usbHelperRequestPermissionMId = env->GetMethodID(m_usbHelperClass, "requestPermission", "(Landroid/hardware/usb/UsbDevice;)V");
    m_usbHelperOpenDeviceMId = env->GetMethodID(m_usbHelperClass, "openDevice", "(Landroid/hardware/usb/UsbDevice;)Landroid/hardware/usb/UsbDeviceConnection;");

    //Android UsbDevice class definitions
    m_usbDeviceClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("android/hardware/usb/UsbDevice")));
    m_usbDeviceGetNameMId = env->GetMethodID(m_usbDeviceClass, "getDeviceName", "()Ljava/lang/String;");
    m_usbDeviceGetProductIdMId = env->GetMethodID(m_usbDeviceClass, "getProductId", "()I");
    m_usbDeviceGetVendorIdMId = env->GetMethodID(m_usbDeviceClass, "getVendorId", "()I");
    m_usbDeviceGetInterfaceCountMId = env->GetMethodID(m_usbDeviceClass, "getInterfaceCount", "()I");
    m_usbDeviceGetInterfaceMId = env->GetMethodID(m_usbDeviceClass, "getInterface", "(I)Landroid/hardware/usb/UsbInterface;");

    //Android UsbDeviceConnection class definitions
    m_usbDeviceConnectionClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("android/hardware/usb/UsbDeviceConnection")));
    m_usbDeviceConnectionClaimInterfaceMId = env->GetMethodID(m_usbDeviceConnectionClass, "claimInterface", "(Landroid/hardware/usb/UsbInterface;Z)Z");
    m_usbDeviceConnectionReleaseInterfaceMId = env->GetMethodID(m_usbDeviceConnectionClass, "releaseInterface", "(Landroid/hardware/usb/UsbInterface;)Z");
    m_usbDeviceConnectionBulkTransferWithOffsetMId = env->GetMethodID(m_usbDeviceConnectionClass, "bulkTransfer", "(Landroid/hardware/usb/UsbEndpoint;[BIII)I");
    m_usbDeviceConnectionBulkTransferMId = env->GetMethodID(m_usbDeviceConnectionClass, "bulkTransfer", "(Landroid/hardware/usb/UsbEndpoint;[BII)I");
    m_usbDeviceConnectionGetFileDescriptorMid = env->GetMethodID(m_usbDeviceConnectionClass, "getFileDescriptor", "()I");

    //Android UsbInterface class definitions
    m_usbDeviceInterfaceClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("android/hardware/usb/UsbInterface")));
    m_usbDeviceInterfaceGetEndpointCountMId = env->GetMethodID(m_usbDeviceInterfaceClass, "getEndpointCount", "()I");
    m_usbDeviceInterfaceGetEndpointMId = env->GetMethodID(m_usbDeviceInterfaceClass, "getEndpoint", "(I)Landroid/hardware/usb/UsbEndpoint;");

    //Android UsbEndpoint class definitions
    m_usbDeviceEndpointClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("android/hardware/usb/UsbEndpoint")));
    m_usbDeviceEndpointGetEndpointNumberMId = env->GetMethodID(m_usbDeviceEndpointClass, "getEndpointNumber", "()I");
    m_usbDeviceEndpointGetEndpointAddressMId = env->GetMethodID(m_usbDeviceEndpointClass, "getAddress", "()I");
    m_usbDeviceEndpointGetDirectionMId = env->GetMethodID(m_usbDeviceEndpointClass, "getDirection", "()I");
    m_usbDeviceEndpointGetIntervalMId = env->GetMethodID(m_usbDeviceEndpointClass, "getInterval", "()I");

    if(usbDevice != nullptr) {
        m_usbDeviceObject = env->NewGlobalRef(usbDevice);
        if(m_usbDeviceObject != nullptr) {
            std::cout << LOG_TAG << "Constructor" << std::endl;

            jstring devName = static_cast<jstring >(env->CallObjectMethod(m_usbDeviceObject, m_usbDeviceGetNameMId));
            const char* utfrep = env->GetStringUTFChars(devName, JNI_FALSE);
            m_usbDeviceName = std::string(utfrep);
            env->ReleaseStringUTFChars(devName, utfrep);

            m_vendorId = static_cast<uint16_t >(env->CallIntMethod(m_usbDeviceObject, m_usbDeviceGetVendorIdMId));
            m_productId = static_cast<uint16_t >(env->CallIntMethod(m_usbDeviceObject, m_usbDeviceGetProductIdMId));

            if( (m_vendorId == 0x1D19 && m_productId == 0x110D) || (m_vendorId == 0x0FD9 && m_productId == 0x004C) || (m_vendorId == 0x16C0 && m_productId == 0x05DC)) {
                m_interfaceNum = 0;
            }

            if(m_vendorId == 0x0416 && m_productId == 0xB003) {
                m_interfaceNum = 1;
            };

            std::cout << LOG_TAG << "ProductID: " << std::hex << +m_productId << " VendorID: " << +m_vendorId << std::dec << std::endl;
        }
    }
}

JUsbDevice::~JUsbDevice() {
    std::cout << LOG_TAG << "Destructor" << std::endl;

    JNIEnv* env;
    m_javaVm->GetEnv((void **)&env, JNI_VERSION_1_6);

    env->DeleteGlobalRef(m_usbDeviceObject);
    env->DeleteGlobalRef(m_usbHelperClass);
    env->DeleteGlobalRef(m_usbDeviceClass);
    env->DeleteGlobalRef(m_usbDeviceConnectionClass);
    env->DeleteGlobalRef(m_usbDeviceInterfaceClass);
    env->DeleteGlobalRef(m_usbDeviceEndpointClass);
    env->DeleteGlobalRef(m_usbDeviceConnectionObject);
}

std::string JUsbDevice::getDeviceName() const {
    return m_usbDeviceName;
}

uint16_t JUsbDevice::getProductId() const {
    return m_productId;
}

uint16_t JUsbDevice::getVendorId() const {
    return m_vendorId;
}

bool JUsbDevice::isPermissionGranted() const {
    return m_permissionGranted;
}

void JUsbDevice::requestPermission(JUsbDevice::PermissionCallbackFunction permissionCallback) {
    m_permissionCallback = permissionCallback;
    JNIEnv* env;
    m_javaVm->GetEnv((void **)&env, JNI_VERSION_1_6);
    env->CallVoidMethod(env->CallStaticObjectMethod(m_usbHelperClass, m_usbHelperGetInstanceMId), m_usbHelperRequestPermissionMId, m_usbDeviceObject);
}

void JUsbDevice::permissionGranted(JNIEnv *env, bool granted) {
    std::cout << LOG_TAG << "Device permission granted: " << std::boolalpha << granted << std::noboolalpha << std::endl;

    if(granted) {
        m_permissionGranted = true;

        m_usbDeviceConnectionObject = env->NewGlobalRef(env->CallObjectMethod(env->CallStaticObjectMethod(m_usbHelperClass, m_usbHelperGetInstanceMId), m_usbHelperOpenDeviceMId, m_usbDeviceObject));

        jboolean claimed = env->CallBooleanMethod(m_usbDeviceConnectionObject, m_usbDeviceConnectionClaimInterfaceMId, env->CallObjectMethod(m_usbDeviceObject, m_usbDeviceGetInterfaceMId, m_interfaceNum), true);

        std::cout << LOG_TAG << "Interface claimed: " << std::boolalpha << static_cast<bool>(claimed) << std::noboolalpha << std::endl;

        jobject usbInterface = env->CallObjectMethod(m_usbDeviceObject, m_usbDeviceGetInterfaceMId, m_interfaceNum);

        jint endpointCnt = env->CallIntMethod(usbInterface, m_usbDeviceInterfaceGetEndpointCountMId);
        std::cout << LOG_TAG <<  "Endpoint count: " << +endpointCnt << std::endl;

        m_fileDescriptor = env->CallIntMethod(m_usbDeviceConnectionObject, m_usbDeviceConnectionGetFileDescriptorMid);
        std::cout << LOG_TAG << "FileDescriptor: " << m_fileDescriptor << std::endl;

        for(int i = 0; i < endpointCnt; i++) {
            jobject endPoint = env->CallObjectMethod(usbInterface, m_usbDeviceInterfaceGetEndpointMId, i);
            jint endpointNumber = env->CallIntMethod(endPoint, m_usbDeviceEndpointGetEndpointNumberMId);
            jint endpointAddress = env->CallIntMethod(endPoint, m_usbDeviceEndpointGetEndpointAddressMId);
            jint endpointDirection = env->CallIntMethod(endPoint, m_usbDeviceEndpointGetDirectionMId);
            std::cout << LOG_TAG << "Endpoint Number: " << +endpointNumber << " Address: " << +endpointAddress << " Direction: " << +endpointDirection << std::endl;

            m_endpointsMap.insert(std::pair<uint8_t,jobject>(static_cast<uint8_t>(endpointAddress), env->NewGlobalRef(endPoint)));
        }
    }

    m_permissionCallback(m_permissionGranted);
}

int JUsbDevice::writeBulkTransferDataDirect(uint8_t endPointAddress, const std::vector<uint8_t> &buffer, int timeOutMs) {
    // https://stackoverflow.com/questions/16963237/passing-usb-file-descriptor-to-android-ndk-program/17046674#17046674

    struct usbdevfs_bulktransfer bt;
    bt.ep = endPointAddress;  /* endpoint */
    bt.timeout = (unsigned int) timeOutMs; /* timeout in ms */
    bt.len = buffer.size();              /* length of data to be written */
    bt.data = (uint8_t*) buffer.data();  /* the data to write */

    int rtn = ioctl(m_fileDescriptor, USBDEVFS_BULK, &bt);
    return rtn;
}

int JUsbDevice::writeBulkTransferData(uint8_t endPointAddress, const std::vector<uint8_t>& buffer, int timeOutMs) {

#if POSIX_IOCTL_READ_WRITE_BULKTRANSFER
    return writeBulkTransferDataDirect(endPointAddress, buffer, timeOutMs);
#else
    auto endpointIter = m_endpointsMap.find(endPointAddress);
    if(endpointIter != m_endpointsMap.cend()) {
        //std::cout << "Found endpoint...sending-retrieving data on Endpoint: " << +endPointAddress << " with buffer size: " << +buffer.size() << std::endl;

        bool wasDeattached = false;
        JNIEnv* enve;

        int envState = m_javaVm->GetEnv((void**)&enve, JNI_VERSION_1_6);
        if(envState == JNI_EDETACHED) {
            if(m_javaVm->AttachCurrentThread(&enve, NULL) == 0) {
                wasDeattached = true;
            } else {
                std::cout << "jniEnv thread failed to attach!" << std::endl;
                return -1;
            }
        }

        jbyteArray data = enve->NewByteArray(buffer.size());
        if(data == NULL) {
            return -1;
        }
        enve->SetByteArrayRegion(data, 0, buffer.size(), (jbyte*)buffer.data());

        jint ret = enve->CallIntMethod(m_usbDeviceConnectionObject, m_usbDeviceConnectionBulkTransferWithOffsetMId, endpointIter->second, data, 0, buffer.size(), timeOutMs);
        //std::cout << "Sent: " << +ret << " bytes" << std::endl;

        enve->DeleteLocalRef(data);

        if(wasDeattached) {
            m_javaVm->DetachCurrentThread();
        }

        return ret;
    }

    return -1;
#endif // POSIX_IOCTL_READ_WRITE_BULKTRANSFER
}

int JUsbDevice::readBulkTransferDataDirect(uint8_t endPointAddress, const std::vector<uint8_t> &buffer, int timeOutMs) {
    // https://stackoverflow.com/questions/16963237/passing-usb-file-descriptor-to-android-ndk-program/17046674#17046674

    struct usbdevfs_bulktransfer bt;
    bt.ep = endPointAddress;  /* endpoint */
    bt.timeout = (unsigned int) timeOutMs; /* timeout in ms */
    bt.len = buffer.size();              /* length of receive buffer */
    bt.data = (uint8_t*) buffer.data();  /* for the received data */

    int rtn = ioctl(m_fileDescriptor, USBDEVFS_BULK, &bt);

    return rtn;
}

int JUsbDevice::readBulkTransferData(uint8_t endPointAddress, std::vector<uint8_t> &buffer, int timeOutMs) {

#if POSIX_IOCTL_READ_WRITE_BULKTRANSFER
    return readBulkTransferDataDirect(endPointAddress, buffer, timeOutMs);
#else
    auto endpointIter = m_endpointsMap.find(endPointAddress);
    if(endpointIter != m_endpointsMap.cend()) {
        //std::cout << "Found endpoint...sending-retrieving data on Endpoint: " << +endPointAddress << " with buffer size: " << +buffer.size() << std::endl;

        bool wasDeattached = false;
        JNIEnv* enve;

        int envState = m_javaVm->GetEnv((void**)&enve, JNI_VERSION_1_6);
        if(envState == JNI_EDETACHED) {
            if(m_javaVm->AttachCurrentThread(&enve, NULL) == 0) {
                wasDeattached = true;
            } else {
                std::cout << "jniEnv thread failed to attach!" << std::endl;
                return -1;
            }
        }

        jbyteArray data = enve->NewByteArray(buffer.size());
        jint ret = enve->CallIntMethod(m_usbDeviceConnectionObject, m_usbDeviceConnectionBulkTransferWithOffsetMId, endpointIter->second, data, 0, buffer.size(), timeOutMs);
        //std::cout << "Retrieved: " << +ret << " bytes" << std::endl;

        if(ret > 0) {
            enve->GetByteArrayRegion(data, 0, ret, (jbyte *) buffer.data());
        }
        enve->DeleteLocalRef(data);

        if(wasDeattached) {
            m_javaVm->DetachCurrentThread();
        }

        return ret;
    }

    return -1;
#endif // POSIX_IOCTL_READ_WRITE_BULKTRANSFER
}
