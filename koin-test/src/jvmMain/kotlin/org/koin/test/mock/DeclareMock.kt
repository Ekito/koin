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
package org.koin.test.mock

import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.logger.Level
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.core.time.measureDurationForResult
import org.koin.ext.getFullName
import org.koin.test.KoinTest
import kotlin.reflect.KClass

/**
 * Declare & Create a mock in Koin container for given type
 *
 * @author Arnaud Giuliani
 */
inline fun <reified T : Any> KoinTest.declareMock(
        qualifier: Qualifier? = null,
        noinline stubbing: (StubFunction<T>)? = null
): T {
    val koin = GlobalContext.get()
    return koin.declareMock(qualifier, stubbing)
}

/**
 * Declare & Create a mock in Koin container for given type
 *
 * @author Arnaud Giuliani
 */
inline fun <reified T : Any> Koin.declareMock(
        qualifier: Qualifier? = null,
        noinline stubbing: (StubFunction<T>)? = null
): T {
    return _scopeRegistry.rootScope.declareMock(qualifier, stubbing)
}

/**
 * Declare & Create a mock in Koin container for given type and scope
 *
 * @author Pedro Moura
 */
inline fun <reified T : Any> Scope.declareMock(
        qualifier: Qualifier? = null,
        noinline stubbing: (T.() -> Unit)? = null
): T {
    val clazz = T::class
    val foundDefinition: BeanDefinition<T> = getDefinition(clazz, qualifier)
    declareMockDefinition(foundDefinition, stubbing)
    return get(qualifier)
}

inline fun <reified T : Any> Scope.declareMockDefinition(
        foundDefinition: BeanDefinition<T>,
        noinline stubbing: (T.() -> Unit)?
) {
    val definition: BeanDefinition<T> = foundDefinition.createMockedDefinition(_koin, stubbing)
    _scopeDefinition.save(definition, forceOverride = true)
    _instanceRegistry.saveDefinition(definition, override = true)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> Scope.getDefinition(
        clazz: KClass<T>,
        qualifier: Qualifier?
): BeanDefinition<T> {
    _koin._logger.info("declare mock for '${clazz.getFullName()}'")
    return _scopeDefinition.definitions.firstOrNull { it.`is`(clazz, qualifier, _scopeDefinition) } as? BeanDefinition<T>
            ?: throw NoBeanDefFoundException("No definition found for qualifier='$qualifier' & class='$clazz'")
}

inline fun <reified T : Any> BeanDefinition<T>.createMockedDefinition(koin: Koin, noinline stubbing: (StubFunction<T>)?): BeanDefinition<T> {
    val copy = copy(definition = {
        if (koin._logger.isAt(Level.DEBUG)) {
            val (mock, time) = measureDurationForResult {
                createMock(stubbing)
            }
            koin._logger.debug("[Mock] created in $time ms")
            mock
        } else {
            createMock(stubbing)
        }
    })
    return copy
}

inline fun <reified T> BeanDefinition<T>.createMock(noinline stubbing: (StubFunction<T>)? = null): T {
    val mock = MockProvider.makeMock<T>()
    return stubbing?.run {
        stubbing(mock)
        mock
    } ?: mock
}

typealias StubFunction<T> = T.() -> Unit
