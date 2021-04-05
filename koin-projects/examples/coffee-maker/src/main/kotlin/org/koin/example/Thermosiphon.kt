package org.koin.example

import org.koin.ksp.KoinInject

@KoinInject
class Thermosiphon(private val heater: Heater) : Pump {

    val a = "a"

    override fun pump() {
        if (heater.isHot()) {
            println("=> => pumping => =>")
        }
    }
}