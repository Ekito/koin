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
package org.koin.log

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Logger that print on system.out
 * @author - Arnaud GIULIANI
 */
class PrintLogger(private val showDebug: Boolean = false) : Logger {

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
    init {
        info("[PrintLogger] display debug = $showDebug")
    }

    private fun date() = df.format(Date()).toString()

    override fun debug(msg: String) {
        if (showDebug) {
            println("${date()} (KOIN)::[d] $msg")
        }
    }

    override fun info(msg: String) {
        println("${date()} (KOIN)::[i] $msg")
    }

    override fun err(msg: String) {
        System.err.println("${date()} (KOIN)::[e] $msg")
    }
}