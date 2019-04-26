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
package org.koin.android.viewmodel.ext.android

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.content.ComponentCallbacks
import org.koin.android.ext.android.getKoin
import org.koin.android.viewmodel.ViewModelParameters
import org.koin.android.viewmodel.getViewModel
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

/**
 * LifecycleOwner extensions to help for ViewModel
 *
 * @author Arnaud Giuliani
 */


/**
 * Lazy getByClass a viewModel instance
 *
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param scope - used scope Instance
 * @param parameters - parameters to pass to the BeanDefinition
 */
inline fun <reified T : ViewModel> LifecycleOwner.viewModel(
        qualifier: Qualifier? = null,
        scope: Scope? = null,
        noinline parameters: ParametersDefinition? = null
): Lazy<T> = lifecycleAwareLazy(this) { getViewModel<T>(qualifier, scope, parameters) }

/**
 * Get a viewModel instance
 *
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param parameters - parameters to pass to the BeanDefinition
 */
inline fun <reified T : ViewModel> LifecycleOwner.getViewModel(
        qualifier: Qualifier? = null,
        scope: Scope? = null,
        noinline parameters: ParametersDefinition? = null
): T {
    val koin = getKoin()
    return koin.getViewModel(
            ViewModelParameters(
                    T::class,
                    this@getViewModel,
                    scope ?: koin.defaultScope,
                    qualifier,
                    parameters = parameters
            )
    )
}

fun LifecycleOwner.getKoin() = when (this) {
    is KoinComponent -> this.getKoin()
    is ComponentCallbacks -> (this as ComponentCallbacks).getKoin()
    else -> GlobalContext.get().koin
}

/**
 * Lazy getByClass a viewModel instance
 *
 * @param clazz - Class of the BeanDefinition to retrieve
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param scope - used scope Instance
 * @param parameters - parameters to pass to the BeanDefinition
 */
fun <T : ViewModel> LifecycleOwner.getViewModel(
        clazz: KClass<T>,
        qualifier: Qualifier? = null,
        scope: Scope? = null,
        parameters: ParametersDefinition? = null
): T {
    val koin = getKoin()
    return koin.getViewModel(
            ViewModelParameters(
                    clazz,
                    this@getViewModel,
                    scope ?: koin.defaultScope,
                    qualifier,
                    parameters = parameters
            )
    )
}

