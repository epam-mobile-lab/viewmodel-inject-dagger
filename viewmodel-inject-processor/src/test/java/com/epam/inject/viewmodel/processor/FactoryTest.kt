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

import com.epam.inject.viewmodel.processor.source.SourceCode
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Before
import org.junit.Test
import javax.tools.JavaFileObject

class FactoryTest {

    private lateinit var processor: AssistedViewModelProcessor

    @Before
    fun setUp() {
        processor = AssistedViewModelProcessor()
    }

    private val viewModelClass = JavaFileObjects
        .forSourceString(
            "test.ViewModelClass",
            SourceCode.viewModelClassSource
        )

    @Test
    fun `factory generation`() {
        val expectedFactory = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.GeneratedViewModelFactory",
            SourceCode.FactoryTest.factoryResult.trimIndent()
        )

        compileAndAssertSuccess(
            expectedFactory,
            "com.epam.inject.viewmodel.GeneratedViewModelFactory",
            viewModelClass
        )
    }

    @Test
    fun `factory module generation`() {
        val expectedFactory = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.GeneratedViewModelFactoryModule",
            SourceCode.FactoryTest.factoryModuleResult.trimIndent()
        )

        compileAndAssertSuccess(
            expectedFactory,
            "com.epam.inject.viewmodel.GeneratedViewModelFactoryModule",
            viewModelClass
        )
    }

    @Test
    fun `scoped factory module generation`() {
        val scopedViewModelClass = JavaFileObjects
            .forSourceString(
                "test.ViewModelClass",
                SourceCode.scopedViewModelSource.trimIndent()
            )

        val customScope = JavaFileObjects.forSourceString(
            "test.CustomScope",
            SourceCode.customScope
        )

        val expectedScopedFactory = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.CustomScopeGeneratedViewModelFactory",
            SourceCode.FactoryTest.scopedFactoryResult.trimIndent()
        )

        compileAndAssertSuccess(
            expectedScopedFactory,
            "com.epam.inject.viewmodel.CustomScopeGeneratedViewModelFactory",
            scopedViewModelClass,
            customScope
        )
    }

    private fun compileAndAssertSuccess(
        expectedModule: JavaFileObject,
        expectedModuleName: String,
        vararg files: JavaFileObject
    ) {
        val compilation = Compiler.javac().withProcessors(processor).compile(*files)

        CompilationSubject.assertThat(compilation)
            .succeeded()
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile(expectedModuleName)
            .hasSourceEquivalentTo(expectedModule)
    }
}
