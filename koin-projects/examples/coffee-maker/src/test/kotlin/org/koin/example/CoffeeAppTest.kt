package org.koin.example

import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.ksp.generated.*

/**
 * @author Fabio de Matos
 * @since 05/04/2021.
 */
class CoffeeAppTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(
            KoinKspModule().module
        )
    }


    @Test
    fun testKsp() {

        CoffeeApp()
            .maker2
            .let {
                assertNotNull("KSP failed ", it)
            }
    }
}