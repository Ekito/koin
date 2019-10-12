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
package org.koin.androidx.viewmodel.ext.android

import android.content.ComponentCallbacks
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import org.koin.android.ext.android.getKoin
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ViewModelParameters
import org.koin.androidx.viewmodel.getViewModel
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
 * Lazy get a viewModel using lazy evaluated scope
 *
 * @param scopeProvider scope instance wrapped in a function for resolving the provided lazily
 * @param owner - specify the viewmodel store owner
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param defaultArguments - Default arguments for SavedStateHandle if useState = true
 * @param parameters - parameters to pass to the BeanDefinition
 */
inline fun <reified T : ViewModel> LifecycleOwner.scopeViewModel(
        crossinline scopeProvider: () -> Scope,
        owner: LifecycleOwner = this,
        qualifier: Qualifier? = null,
        noinline defaultArguments: () -> Bundle? = { null },
        noinline parameters: ParametersDefinition? = null
): Lazy<T> = lazy { scopeProvider().getViewModel<T>(owner, qualifier, defaultArguments, parameters) }

/**
 * Lazy get a viewModel using the current scope instance
 *
 * @param owner - specify the viewmodel store owner
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param defaultArguments - Default arguments for SavedStateHandle if useState = true
 * @param parameters - parameters to pass to the BeanDefinition
 */
inline fun <reified T : ViewModel> LifecycleOwner.currentScopeViewModel(
        owner: LifecycleOwner = this,
        qualifier: Qualifier? = null,
        noinline defaultArguments: () -> Bundle? = { null },
        noinline parameters: ParametersDefinition? = null
): Lazy<T> = scopeViewModel({ currentScope }, owner, qualifier, defaultArguments, parameters)

/**
 * Lazy get a viewModel instance
 *
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param defaultArguments - Default arguments for SavedStateHandle if useState = true
 * @param parameters - parameters to pass to the BeanDefinition
 * @param clazz
 */
fun <T : ViewModel> LifecycleOwner.viewModel(
        clazz: KClass<T>,
        qualifier: Qualifier? = null,
        defaultArguments: () -> Bundle? = { null },
        parameters: ParametersDefinition? = null
): Lazy<T> = lazy { getViewModel(clazz, qualifier, defaultArguments, parameters) }

/**
 * Lazy getByClass a viewModel instance
 *
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param defaultArguments - Default arguments for SavedStateHandle if useState = true
 * @param parameters - parameters to pass to the BeanDefinition
 */
inline fun <reified T : ViewModel> LifecycleOwner.viewModel(
        qualifier: Qualifier? = null,
        noinline defaultArguments: () -> Bundle? = { null },
        noinline parameters: ParametersDefinition? = null
): Lazy<T> = lazy { getViewModel<T>(qualifier, defaultArguments, parameters) }

/**
 * Get a viewModel instance
 *
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param defaultArguments - Default arguments for SavedStateHandle if useState = true
 * @param parameters - parameters to pass to the BeanDefinition
 */
inline fun <reified T : ViewModel> LifecycleOwner.getViewModel(
        qualifier: Qualifier? = null,
        noinline defaultArguments: () -> Bundle? = { null },
        noinline parameters: ParametersDefinition? = null
): T {
    return getViewModel(T::class, qualifier, defaultArguments, parameters)
}

private fun LifecycleOwner.getKoin() = (this as ComponentCallbacks).getKoin()

/**
 * Lazy getByClass a viewModel instance
 *
 * @param clazz - Class of the BeanDefinition to retrieve
 * @param qualifier - Koin BeanDefinition qualifier (if have several ViewModel beanDefinition of the same type)
 * @param defaultArguments - Default arguments for SavedStateHandle if useState = true
 * @param parameters - parameters to pass to the BeanDefinition
 */
fun <T : ViewModel> LifecycleOwner.getViewModel(
        clazz: KClass<T>,
        qualifier: Qualifier? = null,
        defaultArguments: () -> Bundle? = { null },
        parameters: ParametersDefinition? = null
): T {
    return getKoin().getViewModel(
            ViewModelParameters(
                    clazz,
                    defaultArguments,
                    this@getViewModel,
                    qualifier,
                    parameters = parameters
            )
    )
}