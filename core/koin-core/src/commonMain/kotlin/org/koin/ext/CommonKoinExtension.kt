/*
 * Copyright 2017-2021 the original author or authors.
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
package org.koin.ext

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatformTools

/**
 * Global Koin extensions for dependency injection
 */

/**
 * Retrieve given dependency
 * @param qualifier - bean canonicalName / optional
 * @param parameters - dependency parameters / optional
 */
inline fun <reified T : Any> get(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
) =
        getKoin().get<T>(qualifier, parameters)

/**
 * Inject lazily given dependency
 * @param qualifier - bean canonicalName / optional
 * @param parameters - dependency parameters / optional
 */
inline fun <reified T : Any> inject(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
) =
        lazy { get<T>(qualifier, parameters) }

/**
 * Retrieve Koin instance
 */
fun getKoin() = KoinPlatformTools.defaultContext().get()