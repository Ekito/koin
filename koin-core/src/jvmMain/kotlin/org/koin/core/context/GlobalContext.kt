/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.koin.core.context

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.error.KoinAppAlreadyStartedException

/**
 * Global context - current Koin Application available globally
 *
 * Support to help inject automatically instances once KoinApp has been started
 *
 */
actual object GlobalContext {
    internal actual var koin: Koin? = null

    /**
     * StandAlone Koin App instance
     */
    @JvmStatic
    actual fun get() = koin ?: error("KoinApplication has not been started")

    /**
     * StandAlone Koin App instance
     */
    @JvmStatic
    actual fun getOrNull() = koin

    /**
     * Start a Koin Application as StandAlone
     */
    @JvmStatic
    actual fun start(koinApplication: KoinApplication) = synchronized(this) {
        if (koin != null) {
            throw KoinAppAlreadyStartedException("A Koin Application has already been started")
        }
        koin = koinApplication.koin
    }

    /**
     * Stop current StandAlone Koin application
     */
    @JvmStatic
    actual fun stop() {
        koin?.close()
        koin = null
    }
}
