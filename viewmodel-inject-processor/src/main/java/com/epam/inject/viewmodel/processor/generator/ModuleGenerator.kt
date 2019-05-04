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

package com.epam.inject.viewmodel.processor.generator

import androidx.lifecycle.ViewModel
import com.epam.inject.viewmodel.AssistedViewModel
import com.epam.inject.viewmodel.ViewModelKey
import com.epam.inject.viewmodel.processor.AssistedViewModelProcessor.Companion.generateBaseComment
import com.epam.inject.viewmodel.processor.note
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Generates dagger [Module] with provide methods to ViewModels that you pass in the [generate] method.
 * Generated class will be named as [name] const.
 */
internal class ModuleGenerator(pe: ProcessingEnvironment) {

    private companion object {
        private const val name = "ViewModelInjectModule"
    }

    private val messager = pe.messager
    private val elementUtils = pe.elementUtils

    /**
     * Generates [TypeSpec] dagger module with set of ViewModels.
     * @param viewModels set of ViewModels that should be provided.
     * @return [TypeSpec] that represent dagger module with provide methods to ViewModels.
     */
    fun generate(viewModels: Set<TypeElement>): TypeSpec {
        return TypeSpec.classBuilder(name)
            .addJavadoc(generateBaseComment())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Module::class.java)
            .addMethods(generateProvideMethods(viewModels))
            .build()
    }

    /**
     * Defines list of provide methods.
     * @param viewModels the set of ViewModels for which should be created provide methods.
     * @return list of provide [MethodSpec].
     */
    private fun generateProvideMethods(viewModels: Set<TypeElement>): List<MethodSpec> {
        return viewModels.onEach { messager.note(" -> ${it.qualifiedName} added to the module") }
            .map { generateProvideMethod(it) }
    }

    /**
     * Generates provide method for type element.
     * @param vm the ViewModel type element.
     * @return [MethodSpec] that represents provide method.
     */
    private fun generateProvideMethod(vm: TypeElement): MethodSpec {
        val dependencies = getDependencies(vm)
        val params = generateMethodParams(dependencies)
        val methodName = "provide_${vm.qualifiedName.toString().replace(".", "_")}"

        return MethodSpec.methodBuilder(methodName)
            .addAnnotation(Provides::class.java)
            .addAnnotation(IntoMap::class.java)
            .addAnnotation(generateKeyAnnotation(vm))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ViewModel::class.java)
            .addParameters(params)
            .addStatement("return new \$T(%s)".format(dependencies.keys.joinToString()), vm)
            .build()
    }

    /**
     * Generates [ViewModelKey] annotation spec.
     * @param vm the source ViewModel
     * @return annotation spec [ViewModelKey] for ViewModel.
     */
    private fun generateKeyAnnotation(vm: TypeElement): AnnotationSpec {
        return AnnotationSpec.builder(ViewModelKey::class.java)
            .addMember("value", "${ClassName.get(vm)}.class").build()
    }

    /**
     * Generates list of parameters which would be created from dependencies.
     * @param dependencies the list of dependencies.
     * @return list of [ParameterSpec].
     */
    private fun generateMethodParams(dependencies: Map<String, TypeName>): List<ParameterSpec> {
        return dependencies.map { ParameterSpec.builder(it.value, it.key).build() }
    }

    /**
     * Detect dependencies in the constructors using [findConstructors] method, and create map of dependencies.
     * @param type the source type.
     * @return map, where key - parameter name, value - [TypeName] of dependency.
     */
    private fun getDependencies(type: TypeElement): Map<String, TypeName> {
        val viewModelDependencies = mutableMapOf<String, TypeName>()
        findConstructors(type).forEach { constructor ->
            constructor.parameters.forEach { parameter ->
                viewModelDependencies[parameter.simpleName.toString()] = TypeName.get(parameter.asType())
            }
        }
        return viewModelDependencies
    }

    /**
     * Search and return all constructors in the type element.
     * @param type the source type.
     * @return list of constructors.
     */
    private fun findConstructors(type: TypeElement): List<ExecutableElement> {
        return elementUtils.getAllMembers(type)
            .filter { it.getAnnotation(AssistedViewModel::class.java) != null }
            .map { it as ExecutableElement }
    }
}
