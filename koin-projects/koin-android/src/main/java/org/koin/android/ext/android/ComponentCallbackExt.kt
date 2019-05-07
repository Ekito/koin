package org.koin.android.ext.android

import android.content.ComponentCallbacks
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope


/**
 * Get Koin context
 */
fun ComponentCallbacks.getKoin() = when (this) {
    is KoinComponent -> this.getKoin()
    else -> GlobalContext.get().koin
}

/**
 * inject lazily given dependency for Android koincomponent
 * @param qualifier - bean qualifier / optional
 * @param scope
 * @param parameters - injection parameters
 */
inline fun <reified T : Any> ComponentCallbacks.inject(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
) = lazy { get<T>(qualifier, parameters) }

/**
 * get given dependency for Android koincomponent
 * @param name - bean name
 * @param scope
 * @param parameters - injection parameters
 */
inline fun <reified T : Any> ComponentCallbacks.get(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
): T = getKoin().get(qualifier, parameters)