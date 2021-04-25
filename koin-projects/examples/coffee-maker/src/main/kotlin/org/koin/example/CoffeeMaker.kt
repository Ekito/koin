package org.koin.example

import org.koin.ksp.KoinInject

@KoinInject
class CoffeeMaker(private val pump: Pump, private val heater: Heater) {

    fun brew() {
        heater.on()
        pump.pump()
        println(" [_]P coffee! [_]P ")
        heater.off()
    }
}