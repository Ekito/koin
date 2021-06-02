package org.koin.test

import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito

class CheckModulesTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Test
    fun `check a scoped module`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    scope(named("scope")) {
                        scoped { Simple.ComponentA() }
                        scoped { Simple.ComponentB(get()) }
                    }
                }
            )
        }.checkModules()
    }

    @Test
    fun `check a scoped module and ext deps - failed `() {
        try {
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                    module {
                        single { Simple.ComponentB(get()) }
                        scope(named("scope")) {
                            scoped { Simple.ComponentA() }
                        }
                    }
                )
            }.checkModules()
            fail()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @Test
    fun `check a scoped module and ext scope - failed`() {
        try {
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                    module {
                        scope(named("scope2")) {
                            scoped { Simple.ComponentB(get()) }
                        }
                        scope(named("scope1")) {
                            scoped { Simple.ComponentA() }
                        }
                    }
                )
            }.checkModules()
            fail()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @Test
    fun `check a scoped module and ext scope - create scope`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    scope(named("scope2")) {
                        scoped {
                            val a = getScope("scopei1").get<Simple.ComponentA>()
                            Simple.ComponentB(a)
                        }
                    }
                    scope(named("scope1")) {
                        scoped { Simple.ComponentA() }
                    }
                }
            )
        }.checkModules {
            koin.createScope("scopei1", named("scope1"))
        }
    }

    @Test
    fun `check a scoped module and ext scope - inject scope`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    scope(named("scope2")) {
                        scoped { (scope1: Scope) -> Simple.ComponentB(scope1.get()) }
                    }
                    scope(named("scope1")) {
                        scoped { Simple.ComponentA() }
                    }
                }
            )
        }.checkModules {
            create<Simple.ComponentB> { parametersOf(koin.createScope("scopei1", named("scope1"))) }
        }
    }

    @Test
    fun `check a simple module`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { Simple.ComponentA() }
                }
            )
        }.checkModules()
    }

    @Test
    fun `check a module with link`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { Simple.ComponentA() }
                    single { Simple.ComponentB(get()) }
                }
            )
        }.checkModules()
    }

    @Test
    fun `check a broken module`() {
        try {
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                    module {
                        single { Simple.ComponentB(get()) }
                    }
                )
            }.checkModules()
            fail("should not pass with broken definitions")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun `check a module with params`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { (s: String) -> Simple.MyString(s) }
                    single(UpperCase) { (s: String) -> Simple.MyString(s.toUpperCase()) }
                }
            )
        }.checkModules {
            create<Simple.MyString> { parametersOf("param") }
            create<Simple.MyString>(UpperCase) { qualifier -> parametersOf(qualifier.toString()) }
        }
    }

    @Test
    fun `check a module with params using create method with KClass`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { (s: String) -> Simple.MyString(s) }
                    single(UpperCase) { (s: String) -> Simple.MyString(s.toUpperCase()) }
                }
            )
        }.checkModules {
            create(Simple.MyString::class) { parametersOf("param") }
            create(Simple.MyString::class, UpperCase) { qualifier ->
                parametersOf(qualifier.toString())
            }
        }
    }

    @Test
    fun `check a module with params - auto`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { (s: String) -> Simple.MyString(s) }
                    single { (a: Simple.ComponentA) -> Simple.ComponentB(a) }
                }
            )
        }.checkModules()
    }

    @Test
    fun `check a module with params - default value`() {
        val id = "_ID_"
        var injectedValue: String? = null
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { (s: String) ->
                        injectedValue = s
                        Simple.MyString(s)
                    }
                }
            )
        }.checkModules {
            defaultValue(id)
        }

        assert(injectedValue == id)
    }

    @Test
    fun `check a module with params - added default value in graph`() {
        val id = "_ID_"
        var _id = ""
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single {
                        _id = get()
                        Simple.MyString(_id)
                    }
                }
            )
        }
        app.checkModules {
            defaultValue(id)
        }

        assert(id == _id)
    }

    @Test
    fun `check a module with params - default value in graph`() {
        var _value: String? = null
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single {
                        _value = get()
                        Simple.MyString(_value!!)
                    }
                }
            )
        }
        app.checkModules()

        assert(_value == "")
    }

    @Test
    fun `check a module with complex params - default mocked value in graph`() {
        var _a: Simple.ComponentA? = null
        val app = koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single {
                        _a = get()
                        Simple.ComponentB(_a!!)
                    }
                }
            )
        }
        app.checkModules {
            defaultValue<Simple.ComponentA>()
        }

        assert(_a != null)
    }

    @Test
    fun `check a module with params - default value object`() {
        val a = Simple.ComponentA()
        var injectedValue: Simple.ComponentA? = null
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    single { (a: Simple.ComponentA) ->
                        injectedValue = a
                        Simple.ComponentB(a)
                    }
                }
            )
        }.checkModules {
            defaultValue(a)
        }

        assert(injectedValue == a)
    }

    @Test
    fun `check a module with params - auto scope`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                module {
                    scope<Simple.ComponentA> {
                        scoped { Simple.ComponentB(get()) }
                    }
                }
            )
        }.checkModules()
    }

    @Test
    fun `check a module with params - auto scope - error`() {
        try {
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                    module {
                        scope<Simple.ComponentA> {
                            scoped { Simple.ComponentC(get()) }
                        }
                    }
                )
            }.checkModules()
            fail()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun `check with qualifier`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(module {
                single(named("test")) { Simple.ComponentA() }
            })
        }.checkModules()
    }

    @Test
    fun `check a module with property`() {
        koinApplication {
            printLogger(Level.DEBUG)
            properties(hashMapOf("aValue" to "value"))
            modules(
                module {
                    single { Simple.MyString(getProperty("aValue")) }
                }
            )
        }.checkModules()
    }

    @Test
    fun `check a module with secondary type`() {
        koinApplication {
            printLogger(Level.DEBUG)
            modules(
                    module {
                        single { "the_string" }.bind<CharSequence>()
                        single { 42 } bind Number::class
                    }
            )
        }.checkModules()
    }

    @Test
    fun `check a module with wrong secondary type - error`() {
        try {
            koinApplication {
                printLogger(Level.DEBUG)
                modules(
                        module {
                            single { "the_string" }.bind<Int>()
                        }
                )
            }.checkModules()
            fail("should not pass with broken definitions")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}