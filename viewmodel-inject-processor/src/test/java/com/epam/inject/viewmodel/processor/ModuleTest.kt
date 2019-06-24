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

import com.epam.inject.viewmodel.AssistedViewModel
import com.epam.inject.viewmodel.processor.source.SourceCode
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Before
import org.junit.Test
import javax.tools.JavaFileObject

class ModuleTest {

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

    private val defaultModuleName = "com.epam.inject.viewmodel.ViewModelInjectModule"

    @Test
    fun `module with one viewmodel`() {
        val expectedModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.ViewModelInjectModule",
            SourceCode.ModuleTest.moduleWithOneViewModelResult.trimIndent()
        )

        compileAndAssertSuccess(expectedModule, defaultModuleName, viewModelClass)
    }

    @Test
    fun `module with many viewmodels`() {
        val secondViewModelClass = JavaFileObjects
            .forSourceString(
                "test.SecondViewModelClass",
                SourceCode.ModuleTest.additionaViewModelClassSource
            )

        val expectedModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.ViewModelInjectModule",
            SourceCode.ModuleTest.moduleWithManyViewModelsResult.trimIndent()
        )

        compileAndAssertSuccess(
            expectedModule,
            defaultModuleName,
            viewModelClass,
            secondViewModelClass
        )
    }

    @Test
    fun `viewmodel with one dependency`() {
        val dependencyViewModel = JavaFileObjects
            .forSourceString(
                "test.DependencyViewModelClass",
                SourceCode.ModuleTest.viewModelWithOneDependencySource
            )

        val expectedModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.ViewModelInjectModule",
            SourceCode.ModuleTest.viewModelWithOneDependencyResult.trimIndent()
        )

        compileAndAssertSuccess(expectedModule, defaultModuleName, dependencyViewModel)
    }

    @Test
    fun `viewmodel with many dependencies`() {
        val dependenciesViewModel = JavaFileObjects
            .forSourceString(
                "test.DependenciesViewModelClass",
                SourceCode.ModuleTest.viewModelWithManyDependencySource
            )

        val expectedModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.ViewModelInjectModule",
            SourceCode.ModuleTest.viewModelWithManyDependencyResult.trimIndent()
        )

        compileAndAssertSuccess(expectedModule, defaultModuleName, dependenciesViewModel)
    }

    @Test
    fun `viewmodel with several annotated constructors`() {
        val dependenciesViewModel = JavaFileObjects
            .forSourceString(
                "test.DependenciesViewModelClass",
                SourceCode.ModuleTest.viewModelWithSeveralAnnotatedConstructorsSource
            )

        val compilation = Compiler.javac().withProcessors(processor).compile(dependenciesViewModel)
        CompilationSubject.assertThat(compilation)
            .hadErrorContainingMatch(
                "Class .+ has more then one constructor marked " +
                        "with AssistedViewModel annotation"
            )
    }

    @Test
    fun `viewmodel with one annotated constructor`() {
        val dependenciesViewModel = JavaFileObjects
            .forSourceString(
                "test.DependenciesViewModelClass",
                SourceCode.ModuleTest.viewModelWithOneAnnotatedConstructorSource
            )

        val expectedModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.ViewModelInjectModule",
            SourceCode.ModuleTest.viewModelWithOneAnnotatedConstructorResult.trimIndent()
        )

        compileAndAssertSuccess(expectedModule, defaultModuleName, dependenciesViewModel)
    }

    @Test
    fun `module for ViewModels with same name but different package`() {
        val secondViewModelClass = JavaFileObjects
            .forSourceString(
                "prod.ViewModelClass",
                SourceCode.ModuleTest.viewModelWithDifferentPackageSource
            )

        val expectedModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.ViewModelInjectModule",
            SourceCode.ModuleTest.viewModelWithDifferentPackagesResult.trimIndent()
        )

        compileAndAssertSuccess(
            expectedModule,
            defaultModuleName,
            viewModelClass,
            secondViewModelClass
        )
    }

    @Test
    fun `viewModel with scoped constructor`() {
        val scopedViewModelClass = JavaFileObjects
            .forSourceString(
                "test.ViewModelClass",
                SourceCode.scopedViewModelSource.trimIndent()
            )

        val expectedResultModule = JavaFileObjects.forSourceString(
            "com.epam.inject.viewmodel.CustomScopeViewModelInjectModule",
            SourceCode.ModuleTest.scopedViewModelResult.trimIndent()
        )

        val customScope = JavaFileObjects.forSourceString(
            "test.CustomScope",
            SourceCode.customScope
        )

        compileAndAssertSuccess(
            expectedResultModule,
            "com.epam.inject.viewmodel.CustomScopeViewModelInjectModule",
            scopedViewModelClass,
            customScope
        )
    }

    @Test
    fun `viewModel with poorly written scope`() {
        val scopedViewModelClass = JavaFileObjects
            .forSourceString(
                "test.ViewModelClass",
                SourceCode.ModuleTest.scopedViewModelWithWrongScopeSource
            )

        val customScope = JavaFileObjects.forSourceString(
            "test.CustomScope",
            SourceCode.customScope
        )

        val compilation =
            Compiler.javac().withProcessors(processor).compile(scopedViewModelClass, customScope)

        with(CompilationSubject.assertThat(compilation)) {
            failed()
            hadErrorCount(1)
            hadErrorContaining(
                "Element provided to the ${AssistedViewModel::class.java.simpleName} " +
                        "is not marked as Scope"
            )
        }
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
