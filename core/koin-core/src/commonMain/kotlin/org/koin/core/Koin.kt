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
package org.koin.core

import org.koin.core.annotation.KoinInternalApi
import org.koin.core.error.ScopeNotCreatedException
import org.koin.core.logger.EmptyLogger
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.registry.PropertyRegistry
import org.koin.core.registry.ScopeRegistry
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeID
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.mp.KoinPlatformTools
import kotlin.reflect.KClass

/**
 * Koin
 *
 * Gather main features to use on Koin context
 *
 * @author Arnaud Giuliani
 */
@OptIn(KoinInternalApi::class)
class Koin {
    @KoinInternalApi
    val scopeRegistry = ScopeRegistry(this)

    @KoinInternalApi
    val propertyRegistry = PropertyRegistry(this)

    var logger: Logger = EmptyLogger()
        private set

    @KoinInternalApi
    fun setupLogger(logger: Logger) {
        this.logger = logger
    }

    private val modules = hashSetOf<Module>()

    @KoinInternalApi
    fun getRootScope() = scopeRegistry.rootScope

    /**
     * Lazy inject a Koin instance
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return Lazy instance of type T
     */
    inline fun <reified T : Any> inject(
            qualifier: Qualifier? = null,
            mode: LazyThreadSafetyMode = KoinPlatformTools.defaultLazyMode(),
            noinline parameters: ParametersDefinition? = null
    ): Lazy<T> = scopeRegistry.rootScope.inject(qualifier, mode, parameters)

    /**
     * Lazy inject a Koin instance if available
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return Lazy instance of type T or null
     */
    inline fun <reified T : Any> injectOrNull(
            qualifier: Qualifier? = null,
            mode: LazyThreadSafetyMode = KoinPlatformTools.defaultLazyMode(),
            noinline parameters: ParametersDefinition? = null
    ): Lazy<T?> = scopeRegistry.rootScope.injectOrNull(qualifier, mode, parameters)

    /**
     * Get a Koin instance
     * @param qualifier
     * @param scope
     * @param parameters
     */
    inline fun <reified T : Any> get(
            qualifier: Qualifier? = null,
            noinline parameters: ParametersDefinition? = null
    ): T = scopeRegistry.rootScope.get(qualifier, parameters)

    /**
     * Get a Koin instance if available
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return instance of type T or null
     */
    inline fun <reified T : Any> getOrNull(
            qualifier: Qualifier? = null,
            noinline parameters: ParametersDefinition? = null
    ): T? = scopeRegistry.rootScope.getOrNull(qualifier, parameters)

    /**
     * Get a Koin instance
     * @param clazz
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return instance of type T
     */
    fun <T> get(
            clazz: KClass<*>,
            qualifier: Qualifier? = null,
            parameters: ParametersDefinition? = null
    ): T = scopeRegistry.rootScope.get(clazz, qualifier, parameters)

    /**
     * Get a Koin instance if available
     * @param clazz
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return instance of type T or null
     */
    fun <T> getOrNull(
            clazz: KClass<*>,
            qualifier: Qualifier? = null,
            parameters: ParametersDefinition? = null
    ): T? = scopeRegistry.rootScope.getOrNull(clazz, qualifier, parameters)


    /**
     * Declare a component definition from the given instance
     * This result of declaring a single definition of type T, returning the given instance
     *
     * @param instance The instance you're declaring.
     * @param qualifier Qualifier for this declaration
     * @param secondaryTypes List of secondary bound types
     * @param override Allows to override a previous declaration of the same type (default to false).
     */
    inline fun <reified T> declare(
            instance: T,
            qualifier: Qualifier? = null,
            secondaryTypes: List<KClass<*>> = emptyList(),
            override: Boolean = false
    ) {
        val firstType = listOf(T::class)
        scopeRegistry.rootScope.declare(instance, qualifier, firstType + secondaryTypes, override)
    }

    /**
     * Get a all instance for given inferred class (in primary or secondary type)
     *
     * @return list of instances of type T
     */
    inline fun <reified T> getAll(): List<T> = scopeRegistry.rootScope.getAll()

    /**
     * Get instance of primary type P and secondary type S
     * (not for scoped instances)
     *
     * @return instance of type S
     */
    inline fun <reified S : Any, reified P : S> bind(noinline parameters: ParametersDefinition? = null): S =
            scopeRegistry.rootScope.bind<S, P>(parameters)

    /**
     * Get instance of primary type P and secondary type S
     * (not for scoped instances)
     *
     * @return instance of type S
     */
    fun <S : Any, P : S> bind(
            primaryType: KClass<P>,
            secondaryType: KClass<S>,
            parameters: ParametersDefinition? = null
    ): S = scopeRegistry.rootScope.bind(primaryType, secondaryType, parameters)

    internal fun createEagerInstances() {
        scopeRegistry.rootScope.createEagerInstances()
    }

    /**
     * Create a Scope instance
     * @param scopeId
     * @param scopeDefinitionName
     */
    fun createScope(scopeId: ScopeID, qualifier: Qualifier, source: Any? = null): Scope {
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("!- create scope - id:'$scopeId' q:$qualifier")
        }
        return scopeRegistry.createScope(scopeId, qualifier, source)
    }

    /**
     * Create a Scope instance
     * @param scopeId
     */
    inline fun <reified T : Any> createScope(scopeId: ScopeID, source: Any? = null): Scope {
        val qualifier = TypeQualifier(T::class)
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("!- create scope - id:'$scopeId' q:$qualifier")
        }
        return scopeRegistry.createScope(scopeId, qualifier, source)
    }

    /**
     * Create a Scope instance
     * @param scopeDefinitionName
     */
    inline fun <reified T : Any> createScope(scopeId: ScopeID = KoinPlatformTools.generateId()): Scope {
        val qualifier = TypeQualifier(T::class)
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("!- create scope - id:'$scopeId' q:$qualifier")
        }
        return scopeRegistry.createScope(scopeId, qualifier, null)
    }

    /**
     * Create a Scope instance
     * @param scopeDefinitionName
     */
    fun <T : KoinScopeComponent> createScope(t: T): Scope {
        val scopeId = t.getScopeId()
        val qualifier = t.getScopeName()
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("!- create scope - id:'$scopeId' q:$qualifier")
        }
        return scopeRegistry.createScope(scopeId, qualifier, null)
    }

    /**
     * Get or Create a Scope instance
     * @param scopeId
     * @param qualifier
     * @param source
     */
    fun getOrCreateScope(scopeId: ScopeID, qualifier: Qualifier, source: Any? = null): Scope {
        return scopeRegistry.getScopeOrNull(scopeId) ?: createScope(scopeId, qualifier, source)
    }

    /**
     * Get or Create a Scope instance
     * @param scopeId
     * @param qualifier
     */
    inline fun <reified T : Any> getOrCreateScope(scopeId: ScopeID): Scope {
        val qualifier = TypeQualifier(T::class)
        return scopeRegistry.getScopeOrNull(scopeId) ?: createScope(scopeId, qualifier)
    }

    /**
     * get a scope instance
     * @param scopeId
     */
    fun getScope(scopeId: ScopeID): Scope {
        return scopeRegistry.getScopeOrNull(scopeId)
                ?: throw ScopeNotCreatedException("No scope found for id '$scopeId'")
    }

    /**
     * get a scope instance
     * @param scopeId
     */
    fun getScopeOrNull(scopeId: ScopeID): Scope? {
        return scopeRegistry.getScopeOrNull(scopeId)
    }

    /**
     * Delete a scope instance
     */
    fun deleteScope(scopeId: ScopeID) {
        scopeRegistry.deleteScope(scopeId)
    }

    /**
     * Retrieve a property
     * @param key
     * @param defaultValue
     */
    fun <T : Any> getProperty(key: String, defaultValue: T): T {
        return propertyRegistry.getProperty(key) ?: defaultValue
    }

    /**
     * Retrieve a property
     * @param key
     */
    fun <T : Any> getProperty(key: String): T? {
        return propertyRegistry.getProperty(key)
    }

    /**
     * Save a property
     * @param key
     * @param value
     */
    fun setProperty(key: String, value: Any) {
        propertyRegistry.saveProperty(key, value)
    }

    /**
     * Delete a property
     * @param key
     */
    fun deleteProperty(key: String) {
        propertyRegistry.deleteProperty(key)
    }

    /**
     * Close all resources from context
     */
    fun close() {
        modules.forEach { it.isLoaded = false }
        modules.clear()
        scopeRegistry.close()
        propertyRegistry.close()
    }

    fun loadModules(modules: List<Module>, createEagerInstances: Boolean = false) {
        this.modules.addAll(modules)
        scopeRegistry.loadModules(modules)
        if (createEagerInstances) {
            createEagerInstances()
        }
    }


    fun unloadModules(modules: List<Module>, createEagerInstances: Boolean = false) {
        scopeRegistry.unloadModules(modules)
        this.modules.removeAll(modules)
        if (createEagerInstances) {
            createEagerInstances()
        }
    }
}