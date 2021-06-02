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
package org.koin.dsl

import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Callbacks
import org.koin.core.definition.OnCloseCallback
import kotlin.reflect.KClass

/**
 * BeanDefinition DSL specific functions
 *
 * @author Arnaud Giuliani
 */

/**
 * Add a compatible type to match for definition
 * @param clazz
 */
infix fun <S : Any, P : S> BeanDefinition<P>.bind(clazz: KClass<S>): BeanDefinition<P> {
    secondaryTypes = secondaryTypes + clazz
    return this
}

/**
 * Add a compatible type to match for definition
 *
 * Type-safety may be checked by "checkModules" from "koin-test" module.
 */
inline fun <reified T> BeanDefinition<*>.bind(): BeanDefinition<*> {
    secondaryTypes = secondaryTypes + T::class
    return this
}

/**
 * Add compatible types to match for definition
 * @param classes
 */
infix fun BeanDefinition<*>.binds(classes: Array<KClass<*>>): BeanDefinition<*> {
    secondaryTypes = secondaryTypes + classes
    return this
}

/**
 * Callback when closing instance
 */
infix fun <T> BeanDefinition<T>.onClose(onClose: OnCloseCallback<T>): BeanDefinition<T> {
    callbacks = Callbacks(onClose)
    return this
}