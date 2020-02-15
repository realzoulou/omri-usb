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

#ifndef CALLBACKHANDLE
#define CALLBACKHANDLE

#include <memory>
#include <vector>
#include <algorithm>

#include <iostream>

/**
 * Dispatches function on a number of callbacks and cleans up callbacks when
 * they are dead.
 */
template <typename C> class CallbackDispatcher final {

public:
    ~CallbackDispatcher() { }

    std::shared_ptr<C> add(C& callback) {
        auto shared = std::make_shared<C>(callback);
        this->callbacks.push_back(shared);
        return shared;
    }

    bool hasCallbacks() {
        // Remove all callbacks that are gone, only if we are not dispatching.
        if (this->concurrent_dispatcher_count == 0) {
            this->callbacks.erase(std::remove_if(this->callbacks.begin(), this->callbacks.end(),
                                                 [](std::weak_ptr<C> callback) {
                                                     return callback.expired();
                                                 }), this->callbacks.end());
        }
        return !callbacks.empty();
    }

    void clear() {
        this->callbacks.clear();
    }

    template <typename ...A>
    void invoke(A && ... args) {
        // Remove all callbacks that are gone
        hasCallbacks();

        this->concurrent_dispatcher_count++;

        size_t current = 0;
        while (current < this->callbacks.size()) {
            try {
                if (auto callback = this->callbacks[current++].lock()) {
                    if (callback != nullptr &&
                        ((static_cast<std::shared_ptr<C>>(callback)).get() != nullptr)) {
                        (*callback)(std::forward<A>(args)...);
                    }
                }
            } catch (...) {}
        }

        this->concurrent_dispatcher_count--;
    }

private:
    //maybe use a list for constant iterators
    std::vector<std::weak_ptr<C>> callbacks;
    std::atomic_int32_t concurrent_dispatcher_count{0};
};

#endif // CALLBACKHANDLE

