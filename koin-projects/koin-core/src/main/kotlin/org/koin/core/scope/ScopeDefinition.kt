package org.koin.core.scope

import org.koin.core.definition.BeanDefinition
import org.koin.core.instance.InstanceContext
import org.koin.core.qualifier.Qualifier

/**
 * Internal Scope Definition
 */
data class ScopeDefinition(val qualifier: Qualifier, val validateParentScope: Boolean, val parent: ScopeDefinition?) {

    val definitions: HashSet<BeanDefinition<*, *>> = hashSetOf()

    internal fun release(instance: Scope) {
        definitions
                .forEach { it.instance?.release(InstanceContext(scope = instance)) }
    }
}