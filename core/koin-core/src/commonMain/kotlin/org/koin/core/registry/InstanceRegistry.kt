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
package org.koin.core.registry

import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.IndexKey
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
import org.koin.core.instance.FactoryInstanceFactory
import org.koin.core.instance.InstanceContext
import org.koin.core.instance.InstanceFactory
import org.koin.core.instance.SingleInstanceFactory
import org.koin.core.logger.Level
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

@OptIn(KoinInternalApi::class)
class InstanceRegistry(val _koin: Koin, val _scope: Scope) {

    private val _instances = HashMap<IndexKey, InstanceFactory<*>>()
    val instances: Map<IndexKey, InstanceFactory<*>>
        get() = _instances

    internal fun create(definitions: Set<BeanDefinition<*>>) {
        definitions.forEach { definition ->
            if (_koin.logger.isAt(Level.DEBUG)) {
                if (_scope._scopeDefinition.isRoot) {
                    _koin.logger.debug("- $definition")
                } else {
                    _koin.logger.debug("$_scope -> $definition")
                }
            }
            saveDefinition(definition, override = false)
        }
    }

    fun saveDefinition(definition: BeanDefinition<*>, override: Boolean) {
        val defOverride = definition.options.override || override
        val instanceFactory = createInstanceFactory(_koin, definition)
        saveInstance(
                indexKey(definition.primaryType, definition.qualifier),
                instanceFactory,
                defOverride
        )
        definition.secondaryTypes.forEach { clazz ->
            if (defOverride) {
                saveInstance(
                        indexKey(clazz, definition.qualifier),
                        instanceFactory,
                        defOverride
                )
            } else {
                saveInstanceIfPossible(
                        indexKey(clazz, definition.qualifier),
                        instanceFactory
                )
            }
        }
    }

    private fun createInstanceFactory(
            _koin: Koin,
            definition: BeanDefinition<*>
    ): InstanceFactory<*> {
        return when (definition.kind) {
            Kind.Single -> SingleInstanceFactory(_koin, definition)
            Kind.Factory -> FactoryInstanceFactory(_koin, definition)
        }
    }

    private fun saveInstance(key: IndexKey, factory: InstanceFactory<*>, override: Boolean) {
        if (_instances.contains(key) && !override) {
            error("InstanceRegistry already contains index '$key'")
        } else {
            _instances[key] = factory
        }
    }

    private fun saveInstanceIfPossible(key: IndexKey, factory: InstanceFactory<*>) {
        if (!_instances.contains(key)) {
            _instances[key] = factory
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T> resolveInstance(indexKey: IndexKey, parameters: ParametersDefinition?): T? {
        return _instances[indexKey]?.get(defaultInstanceContext(parameters)) as? T
    }

    private fun defaultInstanceContext(parameters: ParametersDefinition?) =
            InstanceContext(_koin, _scope, parameters)

    internal fun close() {
        _instances.values.forEach { it.drop() }
        _instances.clear()
    }

    internal fun createEagerInstances() {
        instances.values.filterIsInstance<SingleInstanceFactory<*>>()
                .filter { instance -> instance.beanDefinition.options.isCreatedAtStart }
                .forEach { instance ->
                    instance.get(
                            InstanceContext(_koin, _scope)
                    )
                }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T> getAll(clazz: KClass<*>): List<T> {
        val instances = instances.values.toSet()
        val potentialKeys: List<InstanceFactory<*>> =
                instances.filter { instance -> instance.beanDefinition.hasType(clazz) }
        return potentialKeys.mapNotNull {
            it.get(defaultInstanceContext(null)) as? T
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <P : S, S : Any> bind(
            primaryType: KClass<P>,
            secondaryType: KClass<S>,
            parameters: ParametersDefinition?
    ): S? {
        return instances.values.firstOrNull { instance ->
            instance.beanDefinition.canBind(
                    primaryType,
                    secondaryType
            )
        }?.get(defaultInstanceContext(parameters)) as? S
    }

    internal fun dropDefinition(definition: BeanDefinition<*>) {
        val ids = _instances.filter { it.value.beanDefinition == definition }.map { it.key }
        ids.forEach { _instances.remove(it) }
    }

    internal fun createDefinition(definition: BeanDefinition<*>) {
        saveDefinition(definition, definition.options.override)
    }

}