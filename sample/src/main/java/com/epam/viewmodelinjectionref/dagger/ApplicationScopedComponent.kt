package com.epam.viewmodelinjectionref.dagger

import com.epam.viewmodelinjectionref.MainActivity
import dagger.Component

@Component(modules = [ModelModule::class, com.epam.inject.viewmodel.CustomScopeViewModelInjectModule::class])
@CustomScope
interface ApplicationScopedComponent {
    fun inject(activity: MainActivity)
}
