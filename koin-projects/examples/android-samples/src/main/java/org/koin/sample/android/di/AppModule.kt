package org.koin.sample.android.di

import org.koin.android.experimental.dsl.viewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.ObjectScope
import org.koin.dsl.module
import org.koin.dsl.onRelease
import org.koin.sample.android.compat.JavaActivity
import org.koin.sample.android.components.Counter
import org.koin.sample.android.components.SCOPE_ID
import org.koin.sample.android.components.SCOPE_SESSION
import org.koin.sample.android.components.compat.CompatSimpleViewModel
import org.koin.sample.android.components.dynamic.DynScoped
import org.koin.sample.android.components.dynamic.DynSingle
import org.koin.sample.android.components.main.DumbServiceImpl
import org.koin.sample.android.components.main.RandomId
import org.koin.sample.android.components.main.Service
import org.koin.sample.android.components.main.ServiceImpl
import org.koin.sample.android.components.mvp.FactoryPresenter
import org.koin.sample.android.components.mvp.ScopedPresenter
import org.koin.sample.android.components.mvvm.ExtSimpleViewModel
import org.koin.sample.android.components.mvvm.SimpleViewModel
import org.koin.sample.android.components.scope.Controller
import org.koin.sample.android.components.scope.Session
import org.koin.sample.android.mvp.MVPActivity
import org.koin.sample.android.mvvm.MVVMActivity
import org.koin.sample.android.mvvm.MVVMFragment
import org.koin.sample.android.scope.ScopedActivityA

val appModule = module {

    single<Service> { ServiceImpl() }
    single<Service>(named("dumb")) { DumbServiceImpl() }

    factory { RandomId() }
}

val mvpModule = module {
    factory { (id: String) -> FactoryPresenter(id, get()) }

    scope(named<MVPActivity>()) {
        scoped { (id: String) -> ScopedPresenter(id, get()) }
    }
}

val mvvmModule = module {
    viewModel { (id: String) -> SimpleViewModel(id, get()) }

    viewModel(named("vm1")) { (id: String) -> SimpleViewModel(id, get()) }
    viewModel(named("vm2")) { (id: String) -> SimpleViewModel(id, get()) }

    objectScope<MVVMActivity>() {
        scoped { Session() }
        scoped { Controller(get<MVVMActivity>()) }
        viewModel { ExtSimpleViewModel(get()) }
        viewModel<ExtSimpleViewModel>(named("ext"))

        childObjectScope<MVVMFragment>()
    }
}

val scopeModule = module {
    scope(named(SCOPE_ID)) {
        scoped(named(SCOPE_SESSION)) { Session() } onRelease {
            // onRelease, count it
            Counter.released++
            println("Scoped -SCOPE_SESSION- release = ${Counter.released}")
        }
    }
    objectScope<ScopedActivityA>() {
        scoped { Session() }
        scoped { Controller(get<ScopedActivityA>()) }
    }
}

val dynamicModule = module {
    single { DynSingle() }
    scope(named("dynamic_scope")) {
        scoped { DynScoped() }
    }
}

val javaModule = module {
    scope(named<JavaActivity>()) {
        scoped { Session() }
        viewModel { CompatSimpleViewModel(get()) }
    }
}