package org.koin.ksp

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

/**
 * @author Fabio de Matos
 * @since 05/04/2021.
 */
class InjectorWriter(private val codeGenerator: CodeGenerator) {

    fun writeKoinInjectors(activityInjectors: MutableMap<KSClassDeclaration, KSPropertyDeclaration>) {

        activityInjectors
            .map {
                writeKoinInjector(it.key, it.value)
            }
    }

    private fun writeKoinInjector(
        enclosingClass: KSClassDeclaration,
        injectedField: KSPropertyDeclaration
    ) {


        val classPackageName = enclosingClass
            .closestClassDeclaration()
            ?.let {
                " ${it.qualifiedName?.getQualifier()}"
            }
            ?: "org.koin.ksp"

        val fullClassName = enclosingClass
            .closestClassDeclaration()
            ?.let {
                " ${it.qualifiedName?.getQualifier()}.$it"
            }


        val fileKt = codeGenerator.createNewFile(
            Dependencies(false),
            classPackageName,
            "CoffeeAppKspInjector",
            "kt"
        )


        fileKt.appendText("package $classPackageName")
        fileKt.appendText("\n")
        fileKt.appendText("\n")
        fileKt.appendText("import $fullClassName\n")
        fileKt.appendText("\n")
        fileKt.appendText("\n")
        fileKt.appendText("class CoffeeAppKspInjector {\n\n")

        fileKt.appendText("fun inject(injectee: $enclosingClass)  {\n", "\t")

        fileKt.appendText(
            "injectee.$injectedField = injectee.getKoin().get() \n",
            identation = "\t\t"
        )

        fileKt.appendText("}\n", "\t")
        fileKt.appendText("}\n")

        fileKt.close()
    }

}