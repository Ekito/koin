import android.app.Activity
import org.junit.Test
import org.junit.experimental.categories.Category
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.sample.androidx.di.allModules
import org.koin.test.category.CheckModuleTest
import org.koin.test.check.checkModules
import org.mockito.Mockito

@Category(CheckModuleTest::class)
class CheckModulesTest {

    @Test
    fun `test DI modules`() =
        checkModules {
            printLogger(Level.DEBUG)
            modules(allModules)
        }
}