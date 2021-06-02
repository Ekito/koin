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
package org.koin.test.check

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.logger.Level
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeDefinition
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.koinApplication
import org.koin.mp.KoinPlatformTools
import org.koin.test.mock.MockProvider
import org.koin.test.parameter.MockParameter

/**
 * Check all definition's dependencies - start all modules and check if definitions can run
 */
fun KoinApplication.checkModules(parameters: CheckParameters? = null) = koin.checkModules(parameters)

/**
 *
 */
fun checkModules(level: Level = Level.INFO, parameters: CheckParameters? = null, appDeclaration: KoinAppDeclaration) {
    koinApplication(appDeclaration)
        .logger(KoinPlatformTools.defaultLogger(level))
        .checkModules(parameters)
}

/**
 * Check all definition's dependencies - start all modules and check if definitions can run
 */
fun Koin.checkModules(parametersDefinition: CheckParameters? = null) {
    logger.info("[Check] checking current modules ...")

    checkScopedDefinitions(
        declareParameterCreators(parametersDefinition)
    )

    logger.info("[Check] modules checked")
    close()
}

private fun Koin.declareParameterCreators(
    parametersDefinition: CheckParameters?
) = ParametersBinding(this).also { binding ->
    parametersDefinition?.invoke(binding)
}

@OptIn(KoinInternalApi::class)
private fun Koin.checkScopedDefinitions(allParameters: ParametersBinding) {
    scopeRegistry.scopeDefinitions.values.forEach { scopeDefinition ->
        checkScope(scopeDefinition, allParameters)
    }
}

@OptIn(KoinInternalApi::class)
private fun Koin.checkScope(
    scopeDefinition: ScopeDefinition,
    allParameters: ParametersBinding
) {
    val qualifier = scopeDefinition.qualifier
    val sourceScopeValue = mockSourceValue(qualifier)
    val scope = getOrCreateScope(qualifier.value, qualifier, sourceScopeValue)
    scope._scopeDefinition.definitions.forEach {
        logger.info("Checking: $it ...")
        checkDefinition(allParameters, it, scope)
    }
}

private fun mockSourceValue(qualifier: Qualifier): Any? {
    return if (qualifier is TypeQualifier) {
        MockProvider.makeMock(qualifier.type)
    } else null
}

@OptIn(KoinInternalApi::class)
private fun checkDefinition(
    allParameters: ParametersBinding,
    definition: BeanDefinition<*>,
    scope: Scope
) {
    val parameters = allParameters.parametersCreators[CheckedComponent(
        definition.qualifier,
        definition.primaryType
    )]?.invoke(
        definition.qualifier
    )
        ?: MockParameter(scope, allParameters.defaultValues)
    val scopeQualifier = scope._scopeDefinition.qualifier
    if (scopeQualifier is TypeQualifier) {
        scope.setSource(MockProvider.makeMock(scopeQualifier.type))
    }
    scope.addParameters(parameters)
    scope.get<Any>(definition.primaryType, definition.qualifier) { parameters }
    for (secondaryType in definition.secondaryTypes) {
        val valueAsSecondary = scope.get<Any>(secondaryType, definition.qualifier) { parameters }
        require(secondaryType.isInstance(valueAsSecondary))
    }
    scope.clearParameters()
}
