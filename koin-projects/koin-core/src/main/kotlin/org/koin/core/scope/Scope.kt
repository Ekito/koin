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
package org.koin.core.scope

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.definition.*
import org.koin.core.error.MissingPropertyException
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.error.ParentScopeMismatchException
import org.koin.core.instance.InstanceContext
import org.koin.core.logger.Level
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.registry.BeanRegistry
import org.koin.core.scope.RootScope.Companion.RootScopeId
import org.koin.core.time.measureDuration
import org.koin.ext.getFullName
import kotlin.reflect.KClass

abstract class Scope(val id: ScopeID, internal val _koin: Koin, val scopeDefinition: ScopeDefinition? = null) {
    val beanRegistry = BeanRegistry()
    protected val callbacks = arrayListOf<ScopeCallback>()

    private var isClosed: Boolean = false

    /**
     * Lazy inject a Koin instance
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return Lazy instance of type T
     */
    @JvmOverloads
    inline fun <reified T> inject(
            qualifier: Qualifier? = null,
            noinline parameters: ParametersDefinition? = null
    ): Lazy<T> =
            lazy { get<T>(qualifier, parameters) }

    /**
     * Lazy inject a Koin instance if available
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return Lazy instance of type T or null
     */
    @JvmOverloads
    inline fun <reified T> injectOrNull(
            qualifier: Qualifier? = null,
            noinline parameters: ParametersDefinition? = null
    ): Lazy<T?> =
            lazy { getOrNull<T>(qualifier, parameters) }

    /**
     * Get a Koin instance
     * @param qualifier
     * @param scope
     * @param parameters
     */
    @JvmOverloads
    inline fun <reified T> get(
            qualifier: Qualifier? = null,
            noinline parameters: ParametersDefinition? = null
    ): T {
        return get(T::class, qualifier, parameters)
    }

    /**
     * Get a Koin instance if available
     * @param qualifier
     * @param scope
     * @param parameters
     *
     * @return instance of type T or null
     */
    @JvmOverloads
    inline fun <reified T> getOrNull(
            qualifier: Qualifier? = null,
            noinline parameters: ParametersDefinition? = null
    ): T? {
        return try {
            get(T::class, qualifier, parameters)
        } catch (e: Exception) {
            KoinApplication.logger.error("Can't get instance for ${T::class.getFullName()}")
            null
        }
    }

    /**
     * Get a Koin instance
     * @param clazz
     * @param qualifier
     * @param parameters
     *
     * @return instance of type T
     */
    fun <T> get(
            clazz: KClass<*>,
            qualifier: Qualifier?,
            parameters: ParametersDefinition?
    ): T = synchronized(this) {
        return if (KoinApplication.logger.isAt(Level.DEBUG)) {
            KoinApplication.logger.debug("+- get '${clazz.getFullName()}'")
            val (instance: T, duration: Double) = measureDuration {
                resolveInstance<T>(qualifier, clazz, parameters)
            }
            KoinApplication.logger.debug("+- got '${clazz.getFullName()}' in $duration ms")
            return instance
        } else {
            resolveInstance(qualifier, clazz, parameters)
        }
    }

    /**
     * Get a Koin instance
     * @param java class
     * @param qualifier
     * @param parameters
     *
     * @return instance of type T
     */
    @JvmOverloads
    fun <T> get(
            clazz: Class<*>,
            qualifier: Qualifier? = null,
            parameters: ParametersDefinition? = null
    ): T = synchronized(this) {
        val kClass = clazz.kotlin
        return if (KoinApplication.logger.isAt(Level.DEBUG)) {
            KoinApplication.logger.debug("+- get '${kClass.getFullName()}'")
            val (instance: T, duration: Double) = measureDuration {
                resolveInstance<T>(qualifier, kClass, parameters)
            }
            KoinApplication.logger.debug("+- got '${kClass.getFullName()}' in $duration ms")
            return instance
        } else {
            resolveInstance(qualifier, kClass, parameters)
        }
    }

    internal abstract fun <T> resolveInstance(
            qualifier: Qualifier?,
            clazz: KClass<*>,
            parameters: ParametersDefinition?
    ): T

    internal abstract fun findDefinition(qualifier: Qualifier?, clazz: KClass<*>): BeanDefinition<*, *>?
    internal abstract fun createEagerInstances()

    /**
     * Declare a component definition from the given instance
     * This result of declaring a scoped/single definition of type T, returning the given instance
     * (single definition of th current scope is root)
     *
     * @param instance The instance you're declaring.
     * @param qualifier Qualifier for this declaration
     * @param secondaryTypes List of secondary bound types
     * @param override Allows to override a previous declaration of the same type (default to false).
     */
    inline fun <reified T> declare(
            instance: T,
            qualifier: Qualifier? = null,
            secondaryTypes: List<KClass<*>>? = null,
            override: Boolean = false
    ) {
        saveScopedDefinition(qualifier, scopeDefinition?.qualifier, { instance }) {
            secondaryTypes?.let { this.secondaryTypes.addAll(it) }
            this.options.override = override
        }
    }

    /**
     * Get current Koin instance
     */
    fun getKoin() = _koin

    /**
     * Get Scope
     * @param scopeID
     */
    fun getScope(scopeID: ScopeID) = getKoin().getScope(scopeID)

    /**
     * Register a callback for this Scope Instance
     * @return false, when the scope is already closed. Callback is not registered in this case.
     */
    fun registerCallback(callback: ScopeCallback): Boolean {
        if (isClosed) {
            return false
        }
        callbacks += callback
        return true
    }

    /**
     * Get a all instance for given inferred class (in primary or secondary type)
     *
     * @return list of instances of type T
     */
    inline fun <reified T> getAll(): List<T> = getAll(T::class)

    /**
     * Get a all instance for given class (in primary or secondary type)
     * @param clazz T
     *
     * @return list of instances of type T
     */
    fun <T> getAll(clazz: KClass<*>): List<T> = beanRegistry.getDefinitionsForClass(clazz)
            .map { it.instance!!.get<T>((InstanceContext(this))) }

    /**
     * Get instance of primary type P and secondary type S
     * (not for scoped instances)
     *
     * @return instance of type S
     */
    inline fun <reified S, reified P> bind(noinline parameters: ParametersDefinition? = null): S {
        val secondaryType = S::class
        val primaryType = P::class
        return bind(primaryType, secondaryType, parameters)
    }

    /**
     * Get instance of primary type P and secondary type S
     * (not for scoped instances)
     *
     * @return instance of type S
     */
    fun <S> bind(
            primaryType: KClass<*>,
            secondaryType: KClass<*>,
            parameters: ParametersDefinition?
    ): S {
        return beanRegistry.getAllDefinitions().first {
            it.primaryType == primaryType && it.secondaryTypes.contains(secondaryType)
        }
                .instance!!.get((InstanceContext( this, parameters))) as S
    }


    /**
     * Retrieve a property
     * @param key
     * @param defaultValue
     */
    fun <T> getProperty(key: String, defaultValue: T): T = _koin.getProperty(key, defaultValue)

    /**
     * Retrieve a property
     * @param key
     */
    fun <T> getPropertyOrNull(key: String): T? = _koin.getProperty(key)

    /**
     * Retrieve a property
     * @param key
     */
    fun <T> getProperty(key: String): T = _koin.getProperty(key)
            ?: throw MissingPropertyException("Property '$key' not found")

    internal fun declareDefinitionsFromScopeSet() {
        scopeDefinition.let {
            it?.definitions?.forEach { definition ->
                beanRegistry.saveDefinition(definition)
            }
        }
    }

    /**
     * Close all instances from this scope
     */
    open fun close() = synchronized(this) {
        release()
        beanRegistry.close()
    }

    open fun tearDown() = synchronized(this) {
        release()
        beanRegistry.tearDown()
    }

    private fun release() {
        if (isClosed) {
            return
        }
        if (KoinApplication.logger.isAt(Level.DEBUG)) {
            KoinApplication.logger.info("closing scope:'$id'")
        }
        // call on close from callbacks
        callbacks.forEach { it.onScopeClose(this) }
        callbacks.clear()

        scopeDefinition?.release(this)
        _koin.deleteScope(this.id)
        isClosed = true
    }

    override fun toString(): String {
        val scopeDef = scopeDefinition.let { ",set:'${it?.qualifier}'" }
        return "Scope[id:'$id'$scopeDef]"
    }

    inline fun <reified T> saveScopedDefinition(qualifier: Qualifier? = null, scopeName: Qualifier? = null, noinline definition: Definition<Scope, T>, noinline block: (BeanDefinition<Scope, T>.() -> Unit)? = null) {
        val beanDefinition = this.createScoped(qualifier, scopeName, definition)
        block?.let { block(beanDefinition) }
        this.beanRegistry.saveDefinition(beanDefinition)
    }

    inline fun <reified T> saveFactoryDefinition(qualifier: Qualifier? = null, scopeName: Qualifier? = null, noinline definition: Definition<Scope, T>, noinline block: (BeanDefinition<Scope, T>.() -> Unit)? = null) {
        val beanDefinition = this.createFactory(qualifier, scopeName, definition)
        block?.let { block(beanDefinition) }
        this.beanRegistry.saveDefinition(beanDefinition)
    }
}

class RootScope internal constructor(_koin: Koin) : Scope(RootScopeId, _koin) {

    companion object {
        internal const val RootScopeId: ScopeID = "-Root-"
    }

    override fun createEagerInstances() {
        val definitions = beanRegistry.findAllCreatedAtStartDefinition()
        if (definitions.isNotEmpty()) {
            definitions.forEach {
                it.resolveInstance(InstanceContext(scope = this))
            }
        }
    }

    override fun findDefinition(qualifier: Qualifier?, clazz: KClass<*>): BeanDefinition<*, *> {
        return beanRegistry.findDefinition(qualifier, clazz)
                ?: throw NoBeanDefFoundException("No definition for '${clazz.getFullName()}' has been found. Check your module definitions.")
    }

    override fun <T> resolveInstance(qualifier: Qualifier?, clazz: KClass<*>, parameters: ParametersDefinition?): T {
        val definition = findDefinition(qualifier, clazz)
        return definition.resolveInstance(InstanceContext(this, parameters))
    }
}

open class DefaultScope(id: ScopeID, _koin: Koin, scopeDefinition: ScopeDefinition, val parentScope: Scope) : Scope(id, _koin, scopeDefinition) {

    override fun findDefinition(qualifier: Qualifier?, clazz: KClass<*>): BeanDefinition<*, *>? {
        return beanRegistry.findDefinition(qualifier, clazz)
    }

    override fun createEagerInstances() {}

    override fun <T> resolveInstance(qualifier: Qualifier?, clazz: KClass<*>, parameters: ParametersDefinition?): T {
        val definition = findDefinition(qualifier, clazz)
                ?: return parentScope.resolveInstance(qualifier, clazz, parameters)
                ?: throw ParentScopeMismatchException("parent scope not set for non root scope")
        return definition.resolveInstance(InstanceContext(this, parameters))
    }
}

class ObjectScope<T>(
        id: ScopeID,
        _koin: Koin,
        parentScope: Scope,
        scopeDefinition: ScopeDefinition,
        @PublishedApi internal val instance: T
) : DefaultScope(id, _koin, scopeDefinition, parentScope) {

    override fun close() {
        performClose { super.close() }
    }

    override fun tearDown() {
        performClose { super.tearDown() }
    }

    private fun performClose(block: () -> Unit) {
        val callback = this.instance as? ScopeCallback
        val shouldCallback = callback != null && !callbacks.contains(callback)
        block()
        if (shouldCallback) {
            callback?.onScopeClose(this)
        }
    }


}

typealias ScopeID = String
data class ScopeDescriptor(val scopeId: ScopeID, val scopeName: Qualifier, val parentId: ScopeID = RootScopeId)
fun Any.getScopeName() = TypeQualifier(this::class)
fun Any.createScopeId() = this::class.getFullName() + "@" + System.identityHashCode(this)