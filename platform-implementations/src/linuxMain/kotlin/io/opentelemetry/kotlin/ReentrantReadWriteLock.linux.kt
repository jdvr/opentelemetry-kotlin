@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package io.opentelemetry.kotlin

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.posix.PTHREAD_MUTEX_RECURSIVE
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_t
import platform.posix.pthread_mutex_unlock
import platform.posix.pthread_mutexattr_destroy
import platform.posix.pthread_mutexattr_init
import platform.posix.pthread_mutexattr_settype
import platform.posix.pthread_mutexattr_t
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class ReentrantReadWriteLock {
    @PublishedApi
    internal val mutex: CPointer<pthread_mutex_t> = createRecursiveMutex()

    @Suppress("unused")
    private val cleaner = createCleaner(mutex) {
        if (pthread_mutex_destroy(it) == 0) {
            nativeHeap.free(it)
        }
    }

    public actual inline fun <T> write(action: () -> T): T {
        pthread_mutex_lock(mutex)
        try {
            return action()
        } finally {
            pthread_mutex_unlock(mutex)
        }
    }

    public actual inline fun <T> read(action: () -> T): T = write(action)
}

private fun createRecursiveMutex(): CPointer<pthread_mutex_t> = memScoped {
    val attributes = alloc<pthread_mutexattr_t>()
    pthread_mutexattr_init(attributes.ptr)
    try {
        pthread_mutexattr_settype(attributes.ptr, PTHREAD_MUTEX_RECURSIVE.toInt())
        val mutex = nativeHeap.alloc<pthread_mutex_t>()
        pthread_mutex_init(mutex.ptr, attributes.ptr)
        mutex.ptr
    } finally {
        pthread_mutexattr_destroy(attributes.ptr)
    }
}
