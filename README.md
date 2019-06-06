# Assisted ViewModel

## Existing solution

When we work with [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel) from the [Architecture Components](https://developer.android.com/topic/libraries/architecture) we spend many efforts to write factory for `ViewModel` when it has non default constructor. Fow now it's look like:

```kotlin
class SampleViewModel(private val repository: Repository): ViewModel {

    class Factory @Inject constructor(
        repository:Provider<Repository>
    ): ViewModelProvider.Factory {

        override fun <T : ViewModel> get(modelClass: Class<T>): T {
            if (modelClass == SampleViewModel::class.java) {
                return SampleViewModel(mRepository.get()) as T
            }
            throw IllegalArgumentException(
                "ViewModel of type ${modelClass.simpleName} isn't supported by the factory."
            )
        }
    }
}
```

```kotlin
class SampleFragment : Fragment {

    private lateinit var viewModel: SampleViewModel

    override fun onAttach(context:Context) {
        super.onAttach(context)
        // Inject dependencies
    }

    @Inject
    fun initViewModel(viewModelFactory: SampleViewModel.Factory) {
        mViewModel = 
            ViewModelProviders.of(this, viewModelFactory).get(SampleViewModel::class.java)
    }
}
```

Although this approach is robust, it requires writing a large number of boilerplate code.

## Usage of ViewModelInject
In order to use ViewModel Inject library in the code a few simple steps should be done:

* Add gradle dependency, also you can use `kapt` instead of `annotationProcessor`:

```groovy
dependencies {
    implementation 'com.epam.inject.viewmodel:annotation:1.0.0-alpha02'
    annotationProcessor 'com.epam.inject.viewmodel:processor:1.0.0-alpha02'
}
```

* Mark corresponding constructors in the ViewModel implementation with @AssistedViewModel annotation:

```kotlin
class SampleViewModel @AssistedViewModel constructor(repository:Repository): ViewModel
```

* Perform the build to generate corresponding dagger module and ViewModel factory.
* Add generated module to the root (app) module:

```kotlin
@Module(include = [ViewModelInjectModule::class])
class AppModule
```
* Inject a `AssistedViewModelFactory' implementation in the usual way:

```kotlin
class SampleFragment: Fragment {

    private lateinit var viewModel: SampleViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Inject dependencies
    }

    @Inject
    fun initViewModel(viewModelFactory: AssistedViewModelFactory) {
        mViewModel = 
            ViewModelProviders.of(this, viewModelFactory).get(SampleViewModel::class.java)
    }
}
```


## Current limitations
* For the proper work minimum dagger version should be 2.11.
* Constructor's parameters that aren't in DI graph aren't supported.
* ViewModel from non application (root) scope aren't supported.
* For the kotlin primary constructor which has default values for all parameters @AssistedViewModel annotation can't be used.
* In case of kapt usage generated module should be specified in the dagger's [Module] annotation with full name, e.g. 

```kotlin
@Module(include = [com.epam.inject.viewmodel.ViewModelInjectModule::class])
```