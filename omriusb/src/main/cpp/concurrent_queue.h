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

#ifndef CONCURRENT_QUEUE
#define CONCURRENT_QUEUE

#include <queue>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <chrono>

template <typename T>
class ConcurrentQueue {

public:
    // non-blocking get first queue element
    // if queue empty, then a default constructed T is returned
    T front() {
        std::lock_guard<std::mutex> mlock(m_queue_mutex);
        if (!m_queue.empty()) {
            T item = m_queue.front();
            m_queue.pop();
            return item;
        } else {
            return T();
        }
    }

    T pop() {
        std::unique_lock<std::mutex> mlock(m_queue_mutex);
        //effective against spurious wakes
        while (m_queue.empty()) {
            m_cond.wait(mlock);
        }

        auto item = std::move(m_queue.front());
        m_queue.pop();
        return item;
    }

    //tries to pop an element but unlocks after timeout
    bool tryPop(T& item, const std::chrono::milliseconds timeout) {
        std::unique_lock<std::mutex> mlock(m_queue_mutex);
        auto start = std::chrono::steady_clock::now();
        std::chrono::milliseconds lTimeout = timeout;
        while(m_queue.empty()) {
            if (m_cond.wait_for(mlock, lTimeout) == std::cv_status::timeout) {
                return false; // timeout
            } else {
                // maybe a spurious wakeup, adjust the next lTimeout
                auto now = std::chrono::steady_clock::now();
                auto waitedForMs = std::chrono::duration_cast<std::chrono::milliseconds>(now - start);
                if (lTimeout > waitedForMs) {
                    lTimeout -= waitedForMs;
                } else {
                    return false; // timeout
                }
            }
        }
       
        item = std::move(m_queue.front());
        m_queue.pop();
        return true;
    }

    void push(const T& item) {
        std::unique_lock<std::mutex> mlock(m_queue_mutex);
        m_queue.push(std::move(item));
        //unlock manually before notifying
        mlock.unlock();
        m_cond.notify_one();
    }

    size_t getSize() const {
        std::lock_guard<std::mutex> mlock(m_queue_mutex);
        return m_queue.size();
    }

    bool isEmpty() const {
        std::lock_guard<std::mutex> mlock(m_queue_mutex);
        return m_queue.empty();
    }

    void clear() {
        std::unique_lock<std::mutex> mlock(m_queue_mutex);
        std::queue<T> empty;
        std::swap(m_queue, empty);
        mlock.unlock();
        //make sure every waiting thread gets its fair share
        m_cond.notify_all();
    }

private:
    std::queue<T> m_queue;
    mutable std::mutex m_queue_mutex;
    std::condition_variable m_cond;
};

#endif // CONCURRENT_QUEUE

