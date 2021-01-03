/*
 * Copyright (C) 2021 realzoulou
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

#ifndef DAB_USBHOST_H
#define DAB_USBHOST_H

#include <linux/usbdevice_fs.h>

// from system/core/libusbhost/include/usbhost/usbhost.h
struct usb_request
{
    struct usb_device *dev;
    void* buffer;
    int buffer_length;
    int actual_length;
    int max_packet_size;
    void *private_data; /* struct usbdevfs_urb* */
    int endpoint;
    void *client_data;  /* free for use by client */
};

// from system/core/libusbhost/usbhost.c
#define MAX_DESCRIPTORS_LENGTH 4096
struct usb_device {
    char dev_name[64];
    unsigned char desc[MAX_DESCRIPTORS_LENGTH];
    int desc_length;
    int fd;
    int writeable;
};

// from bionic/libc/kernel/uapi/linux/usb/ch9.h

struct usb_endpoint_descriptor {
    __u8 bLength;
    __u8 bDescriptorType;
    __u8 bEndpointAddress;
    __u8 bmAttributes;
    __le16 wMaxPacketSize;
    __u8 bInterval;
    __u8 bRefresh;
    __u8 bSynchAddress;
} __attribute__((packed));

#define USB_DT_ENDPOINT_SIZE 7
#define USB_DT_ENDPOINT 0x05


#endif //DAB_USBHOST_H
