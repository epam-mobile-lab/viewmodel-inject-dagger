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

package com.epam.inject.viewmodel.processor.store

import com.epam.inject.viewmodel.AssistedViewModel
import com.epam.inject.viewmodel.processor.error
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Scope
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Analyzes if for the current round exist [ViewModel] implementations with constructor marked with
 * [AssistedViewModel] annotation. Such [ViewModel]s stored in the [foundViewModels] collection to
 * be processed.
 */
internal class AssistedViewModelStore(processingEnv: ProcessingEnvironment) {
    /**
     * Storage for ViewModels which should be processed by the processor.
     */
    val viewModels = mutableMapOf<TypeMirror?, MutableList<TypeElement>>()

    private val messager = processingEnv.messager
    private val elementUtils = processingEnv.elementUtils
    private val typeUtils = processingEnv.typeUtils

    /**
     * Analyze files provided for this for this round of generation. Classes that satisfy all
     * requirements stores in the ViewModels collection for further processing.
     * @param roundEnvironment container for the information about the current round of the generation.
     * @return true - if there are ViewModel implementations for processing
     *         false - otherwise.
     */
    fun process(roundEnvironment: RoundEnvironment): Boolean {
        val viewModelType: TypeElement =
            elementUtils.getTypeElement("androidx.lifecycle.ViewModel")
                ?: error("androidx.lifecycle.ViewModel type was not found")

        roundEnvironment.rootElements.forEach { element ->
            val annotatedConstructors = findConstructors(element as TypeElement)

            if (annotatedConstructors.isNotEmpty()) {
                val annotationMirror = annotatedConstructors
                    .first()
                    .annotationMirrors
                    .first()

                val currentScope = if (annotationMirror.elementValues.isEmpty())
                    null
                else annotationMirror
                    .elementValues
                    .values
                    .first()
                    .value as TypeMirror

                if (!validateAnnotatedElements(
                        element,
                        viewModelType,
                        annotatedConstructors,
                        currentScope
                    )
                ) {
                    clear()
                    return false
                }

                if (!viewModels.containsKey(currentScope)) {
                    viewModels[currentScope] = mutableListOf()
                }

                viewModels[currentScope]?.add(element)
            }
        }

        return viewModels.isNotEmpty()
    }

    /**
     * Collect all constructors that marked [AssistedViewModel] annotation
     * @param element the type which constructor annotated
     * @return list of annotated constructors
     */
    private fun findConstructors(element: TypeElement): List<Element> {
        return elementUtils.getAllMembers(element)
            .filter { it.getAnnotation(AssistedViewModel::class.java) != null }
            .toList()
    }

    /**
     * Remove all ViewModels from [foundViewModels].
     */
    fun clear() {
        /*foundViewModels.clear()*/
        viewModels.clear()
    }

    /**
     * Validate if the elementType inherit from ViewModel and marked by [AssistedViewModel] annotation.
     * @param elementType type which contains constructors annotated with [AssistedViewModel].
     * @param expectedSuperType expected supertype.
     * @param constructors list of the constructors marked with [AssistedViewModel] for the current type.
     * @return true - if elementType inherits from ViewModel and marked by [AssistedViewModel], false otherwise.
     */
    private fun validateAnnotatedElements(
        elementType: TypeElement,
        expectedSuperType: TypeElement,
        constructors: List<Element>,
        scope: TypeMirror?
    ): Boolean = if (constructors.size > 1) {
        messager.error(
            "Class ${elementType.qualifiedName} has more then one constructor " +
                    "marked with ${com.epam.inject.viewmodel.AssistedViewModel::class.java.simpleName} " +
                    "annotation"
        )
        false
    } else if (scope !== null && typeUtils.asElement(scope).getAnnotation(Scope::class.java) === null) {
        messager.error(
            "Element provided to the ${AssistedViewModel::class.java.simpleName} is not marked " +
                    "as ${Scope::class.java.simpleName}"
        )
        false
    } else if (!typeUtils.isAssignable(elementType.asType(), expectedSuperType.asType())) {
        messager.error("Class ${elementType.qualifiedName} is not assignable to ${expectedSuperType.qualifiedName}")
        false
    } else {
        true
    }
}
