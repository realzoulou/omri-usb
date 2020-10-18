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

#include "jni-helper.h"

bool JNI_ATTACH(JavaVM * javaVmPtr, bool & wasDetached) {
    if (javaVmPtr != nullptr) {
        wasDetached = false;
        JNIEnv *enve;
        int envState = javaVmPtr->GetEnv((void **) &enve, JNI_VERSION_1_6);
        if (envState == JNI_EDETACHED) {
            if (javaVmPtr->AttachCurrentThread(&enve, nullptr) == 0) {
                wasDetached = true;
            } else {
                return false;
            }
        }
        return true;
    }
    return false;
}

bool JNI_DETACH(JavaVM * javaVmPtr, bool wasDetached) {
    if (wasDetached) {
        if (javaVmPtr != nullptr) {
            if (javaVmPtr->DetachCurrentThread() == 0) {
                return true;
            }
        }
        return false;
    }
    return true;
}

