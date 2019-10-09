package org.koin.androidx.viewmodel

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KClass

class ViewModelParameters<T : Any>(
    val clazz: KClass<T>,
    val defaultArguments: () -> Bundle? = { null },
    val owner: LifecycleOwner,
    val qualifier: Qualifier? = null,
    val from: ViewModelStoreOwnerDefinition? = null,
    val parameters: ParametersDefinition? = null
)

typealias ViewModelStoreOwnerDefinition = () -> ViewModelStoreOwner