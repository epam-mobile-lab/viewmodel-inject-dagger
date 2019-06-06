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

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import org.junit.Before
import org.junit.Test

class ViewModelStoreTest {

    private lateinit var processor: AssistedViewModelProcessor

    @Before
    fun setUp() {
        processor = AssistedViewModelProcessor()
    }

    @Test
    fun `validate source is not subclass of ViewModel`() {

        val notViewModel = JavaFileObjects.forSourceString(
                "test.NotViewModelClass",
                """
                package test;
            import com.epam.inject.viewmodel.AssistedViewModel;

            public class NotViewModelClass {

                @AssistedViewModel
                public NotViewModelClass() {
                }

            }
            """
        )

        val compilation = javac().withProcessors(processor).compile(notViewModel)

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("is not assignable to androidx.lifecycle.ViewModel")
    }

    @Test
    fun `validate source has several marked constructors`() {

        val viewModelWithSeveralConstructors = JavaFileObjects.forSourceString(
            "test.fail.ViewModelClass",
            """
                package test.fail;
            import com.epam.inject.viewmodel.AssistedViewModel;
            import androidx.lifecycle.ViewModel;

            public class ViewModelClass extends ViewModel {

                @AssistedViewModel
                public ViewModelClass() {
                }

                @AssistedViewModel
                public ViewModelClass(Object object) {
                }

            }
            """
        )

        val compilation = javac().withProcessors(processor).compile(viewModelWithSeveralConstructors)

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("has more then one constructor marked with AssistedViewModel annotation")
    }

    @Test
    fun `validate source is subclass of ViewModel`() {

        val viewModelClass = JavaFileObjects
                .forSourceString(
                        "test.ViewModelClass",
                        """
                package test;

            import com.epam.inject.viewmodel.AssistedViewModel;
            import androidx.lifecycle.ViewModel;

            public class ViewModelClass extends ViewModel {

                @AssistedViewModel
                public ViewModelClass(){
                }

            }
            """
                )

        val compilation = javac().withProcessors(processor).compile(viewModelClass)

        assertThat(compilation)
                .succeeded()
    }
}
