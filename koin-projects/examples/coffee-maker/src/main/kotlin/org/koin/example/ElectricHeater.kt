package org.koin.example

import org.koin.ksp.KoinInject

@KoinInject
class ElectricHeater : Heater {

    private var heating: Boolean = false

    override fun on() {
        println("~ ~ ~ heating ~ ~ ~")
        heating = true
    }

    override fun off() {
        heating = false
    }

    override fun isHot(): Boolean = heating
}