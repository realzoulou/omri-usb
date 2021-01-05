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


#include <cerrno>
#include <iostream>
#include <linux/byteorder/little_endian.h>
#include <linux/usbdevice_fs.h>
#include <sys/ioctl.h>
#include <poll.h>
#include <unistd.h>

#include "jusbdevice.h"

#ifdef POSIX_IOCTL_WRITE_ASYNC_BULKTRANSFER
#include "usbhost.h"
#endif

JUsbDevice::JUsbDevice(JavaVM* javaVm, JNIEnv *env, jobject usbDevice) {
    m_javaVm = javaVm;

    //UsbHelper class definitions
    m_usbHelperClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("org/omri/radio/impl/UsbHelper")));
    m_usbHelperGetInstanceMId = env->GetStaticMethodID(m_usbHelperClass, "getInstance", "()Lorg/omri/radio/impl/UsbHelper;");
    m_usbHelperRequestPermissionMId = env->GetMethodID(m_usbHelperClass, "requestPermission", "(Landroid/hardware/usb/UsbDevice;)V");
    m_usbHelperOpenDeviceMId = env->GetMethodID(m_usbHelperClass, "openDevice", "(Landroid/hardware/usb/UsbDevice;)Landroid/hardware/usb/UsbDeviceConnection;");
    m_usbHelperCloseDeviceConnectionMId = env->GetMethodID(m_usbHelperClass, "closeDeviceConnection", "(Landroid/hardware/usb/UsbDeviceConnection;)V");

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
    // close an open UsbDeviceConnection
    m_javaVm->GetEnv((void **)&env, JNI_VERSION_1_6);
    if (m_usbDeviceConnectionObject != nullptr && m_usbHelperClass != nullptr) {
        jobject usbHelper = env->CallStaticObjectMethod(m_usbHelperClass, m_usbHelperGetInstanceMId);
        if (usbHelper != nullptr) {
            env->CallVoidMethod(usbHelper,
                                m_usbHelperCloseDeviceConnectionMId, m_usbDeviceConnectionObject);
        }
        env->DeleteGlobalRef(m_usbDeviceConnectionObject);
    }
    env->DeleteGlobalRef(m_usbDeviceObject);
    env->DeleteGlobalRef(m_usbHelperClass);
    env->DeleteGlobalRef(m_usbDeviceClass);
    env->DeleteGlobalRef(m_usbDeviceConnectionClass);
    env->DeleteGlobalRef(m_usbDeviceInterfaceClass);
    env->DeleteGlobalRef(m_usbDeviceEndpointClass);
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

        jobject usbInterface = env->CallObjectMethod(m_usbDeviceObject, m_usbDeviceGetInterfaceMId, m_interfaceNum);

        jboolean claimed = env->CallBooleanMethod(m_usbDeviceConnectionObject, m_usbDeviceConnectionClaimInterfaceMId, usbInterface, JNI_TRUE);

        std::cout << LOG_TAG << "Interface claimed: " << std::boolalpha << static_cast<bool>(claimed) << std::noboolalpha << std::endl;

        jint endpointCnt = env->CallIntMethod(usbInterface, m_usbDeviceInterfaceGetEndpointCountMId);
        std::cout << LOG_TAG <<  "Endpoint count: " << +endpointCnt << std::endl;

        m_fileDescriptor = env->CallIntMethod(m_usbDeviceConnectionObject, m_usbDeviceConnectionGetFileDescriptorMid);
        std::cout << LOG_TAG << "FileDescriptor: " << +m_fileDescriptor << std::endl;

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

#if defined(POSIX_IOCTL_WRITE_ASYNC_BULKTRANSFER)
int JUsbDevice::writeBulkTransferDataAsync(uint8_t endPointAddress, const std::vector<uint8_t> &buffer, int timeOutMs) {
    struct usb_device dev{};
    struct usb_endpoint_descriptor desc{};
    struct usbdevfs_urb urb{};
    struct usb_request request{};

    memset(&dev, 0, sizeof(dev));
    memset(&desc, 0, sizeof(desc));
    memset(&urb, 0, sizeof(urb));
    memset(&request, 0, sizeof(request));

    dev.fd = m_fileDescriptor;
    desc.bLength = USB_DT_ENDPOINT_SIZE;
    desc.bDescriptorType = USB_DT_ENDPOINT;
    desc.bEndpointAddress = endPointAddress;
    desc.bmAttributes = 192; //  from a log, TODO take from real usb_endpoint_descriptor
    desc.wMaxPacketSize = 64; //  from a log, TODO take from real usb_endpoint_descriptor
    desc.bInterval = 0; //  from a log, TODO take from real usb_endpoint_descriptor

    urb.type = USBDEVFS_URB_TYPE_BULK;
    urb.endpoint = endPointAddress;
    urb.status = -1; // filled with response
    urb.flags = 0;
    urb.buffer = (uint8_t*) buffer.data();
    urb.buffer_length = buffer.size();
    urb.actual_length = -1; // will contain length of response
    urb.usercontext = &request;

    request.dev = &dev;
    request.max_packet_size = __le16_to_cpu(desc.wMaxPacketSize);
    request.private_data = &urb;
    request.endpoint = urb.endpoint;
    request.buffer = urb.buffer;
    request.buffer_length = urb.buffer_length;
    request.client_data = nullptr; // could be used for waitRequest() to find this request
    request.private_data = &urb;

    int res;
    res = TEMP_FAILURE_RETRY(ioctl(request.dev->fd, USBDEVFS_SUBMITURB, &urb));

    if (res != 0) {
        std::clog << LOG_TAG << "writeBulkTransferDataAsync: SUBMITURB failed: " << +res << std::endl;
        return -1;
    }

    struct pollfd p{};
    p.fd = dev.fd;
    p.events = POLLOUT;
    p.revents = 0;

    // wait for response (with timeout)
    while (true) {
        res = poll(&p, 1, timeOutMs);
        // check return code
        if (res == 1) {
            break; // success
        } else if (res == 0) {
            std::clog << LOG_TAG << "writeBulkTransferDataAsync: timeout" << std::endl;
            return -1;
        } else /* -1 */ {
            int lerror = errno;
            if (lerror != EAGAIN) {
                std::clog << LOG_TAG << "writeBulkTransferDataAsync: poll failed: res=" << +res
                          << " event=" << +p.revents << " error=" << +lerror << std::endl;
                return -1;
            }
        }
        // check returned event
        if (p.revents != POLLOUT) {
            int lerror = errno;
            std::clog << LOG_TAG << "writeBulkTransferDataAsync: poll failed: res=" << +res
                      << " event=" << +p.revents << " error=" << +lerror << std::endl;
            return -1;
        }
    }

    // read the response
    struct usbdevfs_urb *urbResponse = nullptr;
    res = TEMP_FAILURE_RETRY(ioctl(dev.fd,  USBDEVFS_REAPURBNDELAY, &urbResponse));  // TODO how does ioctl know about timeout value?
    if (res < 0) {
        int lerror = errno;
        std::clog << LOG_TAG << "writeBulkTransferDataAsync: REAPURBNDELAY failed: res=" << +res
                  << " error=" << +lerror << std::endl;
        return -1;
    } else {
        if (urbResponse != nullptr) {
            auto *reqResponse = (struct usb_request*) (urbResponse->usercontext);
            if (reqResponse != nullptr) {
                // note: reqResponse should be equal to &req :-)
                reqResponse->actual_length = urbResponse->actual_length;
                return reqResponse->actual_length;
            } else {
                std::clog << LOG_TAG << "writeBulkTransferDataAsync: reqResponse null" << std::endl;
                return -1;
            }
        } else {
            std::clog << LOG_TAG << "writeBulkTransferDataAsync: urbResponse null" << std::endl;
            return -1;
        }
    }
}
#endif // defined(POSIX_IOCTL_WRITE_ASYNC_BULKTRANSFER)

int JUsbDevice::writeBulkTransferDataDirect(uint8_t endPointAddress, const std::vector<uint8_t> &buffer, int timeOutMs) {
    struct usbdevfs_bulktransfer bt{};
    auto len = buffer.size();
    unsigned int timeout = (timeOutMs >= 0) ? (unsigned int) timeOutMs : 0U;
    auto pData = (uint8_t *) buffer.data();
    int count = 0;

    do {
        memset(&bt, 0, sizeof(bt));
        bt.ep = endPointAddress;  /* endpoint */
        bt.timeout = timeout;     /* timeout in ms */
        bt.len = len;             /* length of data to be written */
        bt.data = pData;          /* the data to write */

        int n = TEMP_FAILURE_RETRY(ioctl(m_fileDescriptor, USBDEVFS_BULK, &bt));

        if (n < 0) {
            int lError = errno;
            std::clog << LOG_TAG << "writeBulkTransferDataDirect len " << +len << "/"
                      << +buffer.size() << " failed errno=" << +lError << " "
                      << strerror(lError) << std::endl;
            return -1;
        } else {
            len -= n;
            pData += n;
            count += n;
        }
    } while (len > 0);

    return count;
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
    struct usbdevfs_bulktransfer bt{};
    auto len = buffer.size();
    unsigned int timeout = (timeOutMs >= 0) ? (unsigned int) timeOutMs : 0U;
    auto pData = (uint8_t *) buffer.data();
    int count = 0;

    while (len > 0) {
        memset(&bt, 0, sizeof(bt));
        bt.ep = endPointAddress;  /* endpoint */
        bt.timeout = timeout;     /* timeout in ms */
        bt.len = len;             /* length of expected data */
        bt.data = pData;          /* for the received data */

        int n = TEMP_FAILURE_RETRY(ioctl(m_fileDescriptor, USBDEVFS_BULK, &bt));


        if (n < 0) {
            int lError = errno;
            if (lError != ETIMEDOUT) {
                std::clog << LOG_TAG << "readBulkTransferDataDirect failed errno=" << +lError << " "
                          << strerror(lError) << std::endl;
                return -1;
            } else {
                // timeout
                return count;
            }
        } else {
            len -= n;
            pData += n;
            count += n;
        }
    }
    return count;
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
