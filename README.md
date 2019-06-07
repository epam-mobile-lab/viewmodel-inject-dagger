# Assisted ViewModel

## Existing solution

When we work with [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel) from the [Architecture Components](https://developer.android.com/topic/libraries/architecture) we spend many efforts to write factory for `ViewModel` when it has non default constructor. The solution requires writing a large number of boilerplate code.

## Usage of ViewModelInject
In order to use ViewModel Inject library in the code a few simple steps should be done:

```groovy
dependencies {
    implementation 'com.epam.inject.viewmodel:annotation:1.0.0-alpha02'
    kapt 'com.epam.inject.viewmodel:processor:1.0.0-alpha02' // or use annotationProcessor
}
```

* Mark corresponding constructors in the ViewModel implementation with `@AssistedViewModel` annotation:

```kotlin
class SampleViewModel @AssistedViewModel constructor(repository: Repository): ViewModel
```

* Perform the build to generate corresponding dagger module and ViewModel factory.
* Add generated module to the root (app) module:

```kotlin
@Module(include = [ViewModelInjectModule::class])
class AppModule
```
* Inject a `AssistedViewModelFactory` implementation in the usual way:

```kotlin
class SampleFragment: Fragment {

    @Inject
    lateinit var viewModelFactory: AssistedViewModelFactory

    private val viewModel: SampleViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SampleViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Inject dependencies
    }
}
```

With AndroidX Fragment 1.1.0

```kotlin
class SampleFragment: Fragment {

    @Inject
    lateinit var viewModelFactory: AssistedViewModelFactory

    private val viewModel: SampleViewModel by viewModel(::viewModelFactory)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Inject dependencies
    }
}
```

## Current limitations
* For the proper work minimum Dagger version should be 2.11.
* Constructor's parameters that aren't in a DI graph aren't supported.
* `ViewModel` from non application (root) scope aren't supported.
* For the Kotlin primary constructor which has default values for all parameters `@AssistedViewModel` annotation can't be used.
* In case of `kapt` usage generated module should be specified in the Dagger's `@Module` annotation with full name, e.g. 

```kotlin
@Module(include = [com.epam.inject.viewmodel.ViewModelInjectModule::class])
```
