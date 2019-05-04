# Dagger 2 ViewModel Inject

## Existing solution

When we work with [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel) from the [Architecture Components](https://developer.android.com/topic/libraries/architecture) we spend many efforts to write factory for `ViewModel` when it has non default constructor. Fow now it's look like:

```java
public class SampleViewModel extends ViewModel {

    private final Repository mRepository;

    public SampleViewModel(Repository repository) {
        mRepository = repository;
    }

    public class Factory extends ViewModelProvider.Factory {

        private final Provider<Repository> mRepository;

        @Inject
        public Factory(Provider<Repository> repository) {
            mRepository = repository;
        }

        @Override
        public <T extends ViewModel> T get(Class<T> modelClass) {
            if (modelClass == SampleViewModel.class) {
                return (T)SampleViewModel(mRepository.get());
            }
            throw new IllegalArgumentException(
                "ViewModel of type " + modelClass.getSimpleName() + "isn't supported by the factory.");
        }
    }
}
```
<br/>

```java
public class SampleFragment extends Fragment {

    private SampleViewModel mViewModel;


    @Override
    void onAttach(Context context) {
        super.onAttach(context);
        // Inject dependencies
    }

    @Inject
    void initViewModel(SampleViewModel.Factory viewModelFactory) {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SampleViewModel.class);
    }
}
```

## Expected solution

We want to remove write factories for `ViewModel` by hands and generate them. For that we will add `@ViewModelInject` annotation to the implementation of the `ViewModel` class . The constructor with parameters will be used to create the `ViewModel` from the generated factory.

```java
@ViewModelInject
public class SampleViewModel extends ViewModel {

    private final Repository mRepository;

    public SampleViewModel(Repository repository) {
        mRepository = repository;
    }
}
```

Then generate Dagger 2 Module that will provide all `ViewModel`s that are annotated with @ViewModelInject:

```java
@Generated
@Module
public abstract class ViewModelInjectModule {

    @Provides
    @IntoMap
    @ClassKey(SampleViewModel.class)
    public static ViewModel provideSampleViewModel(Repository repository) {
        return new SampleViewModel(repository);
    }
}
```

All `ViewModel` implementations will be collected into map with the key equal to class name and stored in generated subclass of `ViewModeProvider.Factory`. Full class name will be specified via Annotation Processor argument `viewModelsFactory`.

```java
@Generated
public class SampleViewModelsFactory extends ViewModelProvider.Factory {

    private final Map<Class<ViewModel>, Provider<ViewModel>> mFactories;

    @Inject
    ViewModelsProvider_Factory(@NonNull Map<Class<ViewModel>, Provider<ViewModel>> factories) {
        mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T get(Class<T> modelClass) {
        final Provider<ViewModel> factory = mFactories.get(modelClass);
        if (factory == null) {
            throw new IllegalArgumentException("ViewModel of type " + modelClass.getSimpleName() + "isn't supported by the factory.");
        }
        return (T) factory.get();
    }
}
```

Injection of a `ViewModel` will look the same:

```java
public class SampleFragment extends Fragment {

    private SampleViewModel mViewModel;

    @Override
    void onAttach(Context context) {
        super.onAttach(context);
        // Inject dependencies
    }

    @Inject
    void initViewModel(SampleViewModelsFactory viewModelFactory) {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SampleViewModel.class);
    }
}
```

And we need to add the generated Dagger module to the root (app) module:

```java
@Module(include = [ViewModelInjectModule.class])
public class AppModule {

}
```

## Requirements for version of the library
1. Works based on Java annotation processing and generate code (JavaPoet library can be used)
2. Doesn't use reflection in an application runtime
3. Generated code must be readable
4. Client module must be written in Java
5. The annotation processor can be written in Kotlin, but still need to generate Java code

## Current limitations
1. Params of constrcutor that aren't in DI graph aren't supported
2. ViewModel from non application (root) scope aren't supported



