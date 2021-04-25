package org.koin.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.visitor.KSTopDownVisitor

/**
 * @param lambdaClass used to detect class constructors that will be added to modules on "KoinKspModule"
 * @param lambdaProperty used to detect fields that need to be fulfilled by koin, through "KoinKspInjector"
 */
class KoinVisitor(
    val ksAnnotated: KSAnnotated,
    val lambdaClass: Function1<KSClassDeclaration, Unit>,
    val lambdaProperty: Function1<KSPropertyDeclaration, Unit>
) : KSTopDownVisitor<Unit, Unit>() {


    override fun defaultHandler(node: KSNode, data: Unit) {

        when (node) {
            is KSClassDeclaration -> lambdaClass(node)
            is KSPropertyDeclaration -> lambdaProperty(node)

        }

    }


}
