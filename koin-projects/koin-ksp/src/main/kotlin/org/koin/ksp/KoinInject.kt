package org.koin.ksp

/**
 * @author Fabio de Matos
 * @since 04/04/2021.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
annotation class KoinInject()