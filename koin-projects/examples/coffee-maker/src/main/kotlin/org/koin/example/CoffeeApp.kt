package org.koin.example

import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.time.measureDuration
import org.koin.ksp.KoinInject

@OptIn(KoinApiExtension::class)
class CoffeeApp : KoinComponent {


    val maker: CoffeeMaker by inject()

    @KoinInject
    lateinit var maker2: CoffeeMaker

    init {
        CoffeeAppKspInjector().inject(this)
    }
}

fun main() {
    startKoin {
        printLogger()
        modules(listOf(coffeeAppModule))
    }

    val coffeeShop = CoffeeApp()
    measureDuration("Got Coffee") {
        coffeeShop.maker.brew()
    }
    stopKoin()
}