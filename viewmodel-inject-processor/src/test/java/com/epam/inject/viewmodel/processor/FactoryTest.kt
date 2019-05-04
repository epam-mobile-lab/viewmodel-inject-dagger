/*
 * Copyright 2019 EPAM Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.inject.viewmodel.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Before
import org.junit.Test

class FactoryTest {

    private lateinit var processor: AssistedViewModelProcessor

    @Before
    fun setUp() {
        processor = AssistedViewModelProcessor()
    }

    private val viewModelClass = JavaFileObjects
            .forSourceString(
                    "test.ViewModelClass",
                    """
                package test;

            import com.epam.inject.viewmodel.AssistedViewModel;
            import androidx.lifecycle.ViewModel;

            public class ViewModelClass extends ViewModel {

                @AssistedViewModel
                public ViewModelClass() {
                }

            }
            """
            )

    @Test
    fun `factory default path`() {

        val expectedFactory = JavaFileObjects.forSourceString(
                "generated.GeneratedViewModelFactory",
                """
            package generated;

            import androidx.lifecycle.ViewModel;
            import androidx.lifecycle.ViewModelProvider;
            import java.lang.Class;
            import java.lang.IllegalArgumentException;
            import java.lang.Override;
            import java.util.Map;
            import javax.inject.Inject;
            import javax.inject.Provider;

            /**
             * Generated by dagger-2-vm-inject
             * 17-May-2019 13:35
             */
            public final class GeneratedViewModelFactory implements ViewModelProvider.Factory {
              private final Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModelMap;

              @Inject
              public GeneratedViewModelFactory(
                  Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModelMap) {
                            this.viewModelMap = viewModelMap;
                          }

              @Override
              public <T extends ViewModel> T create(Class<T> modelClass) {
                final Provider<ViewModel> vmProvider = viewModelMap.get(modelClass);
                if(vmProvider == null) {
                  throw new IllegalArgumentException("ViewModel isn't supported by the factory.");
                }
                final ViewModel viewModel = vmProvider.get();
                if(modelClass.isAssignableFrom(viewModel.getClass())) {
                  return (T) viewModel;
                } else {
                  throw new IllegalArgumentException("Another ViewModel implementation was expected.");
                }
              }
            }
        """.trimIndent()
        )

        val compilation = Compiler.javac().withProcessors(processor).compile(viewModelClass)

        CompilationSubject.assertThat(compilation)
                .succeeded()
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("generated.GeneratedViewModelFactory")
                .hasSourceEquivalentTo(expectedFactory)
    }

    @Test
    fun `factory defined path`() {

        val factoryName = "com.name.test.TestViewModelFactory"
        val compileOption = "-AgeneratedViewModelFactory=$factoryName"

        val expectedFactory = JavaFileObjects.forSourceString(
                factoryName,
                """
            package com.name.test;

            import androidx.lifecycle.ViewModel;
            import androidx.lifecycle.ViewModelProvider;
            import java.lang.Class;
            import java.lang.IllegalArgumentException;
            import java.lang.Override;
            import java.util.Map;
            import javax.inject.Inject;
            import javax.inject.Provider;

            /**
             * Generated by dagger-2-vm-inject
             * 17-May-2019 13:35
             */
            public final class TestViewModelFactory implements ViewModelProvider.Factory {
              private final Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModelMap;

              @Inject
              public TestViewModelFactory(
                  Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModelMap) {
                            this.viewModelMap = viewModelMap;
                          }

              @Override
              public <T extends ViewModel> T create(Class<T> modelClass) {
                final Provider<ViewModel> vmProvider = viewModelMap.get(modelClass);
                if(vmProvider == null) {
                  throw new IllegalArgumentException("ViewModel isn't supported by the factory.");
                }
                final ViewModel viewModel = vmProvider.get();
                if(modelClass.isAssignableFrom(viewModel.getClass())) {
                  return (T) viewModel;
                } else {
                  throw new IllegalArgumentException("Another ViewModel implementation was expected.");
                }
              }
            }
        """.trimIndent()
        )

        val compilation = Compiler.javac()
                .withProcessors(processor)
                .withOptions(compileOption)
                .compile(viewModelClass)

        CompilationSubject.assertThat(compilation)
                .succeeded()
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile(factoryName)
                .hasSourceEquivalentTo(expectedFactory)
    }
}