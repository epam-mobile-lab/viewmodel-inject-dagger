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
import androidx.lifecycle.ViewModelProvider
import com.epam.inject.viewmodel.processor.AssistedViewModelProcessor.Companion.KEY_NAME_FACTORY_OPTION
import com.epam.inject.viewmodel.processor.AssistedViewModelProcessor.Companion.generateBaseComment
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.squareup.javapoet.WildcardTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.Modifier

/**
 * Generates implementation of [ViewModelProvider.Factory] which would contain created ViewModel.
 * This factory later would be used by the [ViewModelProvider] to provide created ViewModels to the
 * activity or fragment.
 */
internal class FactoryGenerator(processingEnvironment: ProcessingEnvironment) {

    /**
     * Defines name of the generated factory and come as processor parameter.
     * In case there was no name provided [DEFAULT_FACTORY_NAME] is used.
     */
    private val viewModelFactoryName =
        processingEnvironment.options[KEY_NAME_FACTORY_OPTION]?.substringAfterLast('.') ?: DEFAULT_FACTORY_NAME

    /**
     * Generates factory to which would store ViewModels created by dagger.
     * @return [TypeSpec] of the factory which can be later written to the file.
     */
    fun generate(): TypeSpec {
        val typeParameterizedProvider = generateTypeParameterizedProvider()
        val mapType = generateMapType(generateWildCardClass(), typeParameterizedProvider)
        val classParam = generateClassParam(generateParameterType())

        return TypeSpec.classBuilder(viewModelFactoryName)
            .addJavadoc(generateBaseComment())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ViewModelProvider.Factory::class.java)
            .addField(generateMapField(mapType))
            .addMethod(generateConstructor(mapType))
            .addMethod(generateCreateMethod(classParam, typeParameterizedProvider))
            .build()
    }

    /**
     * Defines the wildcard type would be used as key in the map stored created ViewModels.
     *
     * ```
     * Class<? extends ViewModel>.
     * ```
     *
     * @return [ParameterizedTypeName] of type for the map key.
     */
    private fun generateWildCardClass(): ParameterizedTypeName {
        return ParameterizedTypeName.get(
            ClassName.get(Class::class.java),
            WildcardTypeName.subtypeOf(ViewModel::class.java)
        )
    }

    /**
     * Defines the type of the value for the ViewModel map:
     *
     * ```
     * Provider<ViewModel>
     * ```
     *
     * @return [ParameterizedTypeName] which defines value's type.
     */
    private fun generateTypeParameterizedProvider(): ParameterizedTypeName {
        return ParameterizedTypeName.get(
            ClassName.get(Provider::class.java),
            ClassName.get(ViewModel::class.java)
        )
    }

    /**
     * Creates the type of the map for storing ViewModels:
     *
     * ```
     * Map<Class<? extends ViewModel>, Provider<ViewModel>>
     * ```
     *
     * @param wildCardClass type of the key for the map.
     * @param typeParameterizedProvider type of the value for the map.
     * @return [ParameterizedTypeName] for the type of the map.
     */
    private fun generateMapType(
        wildCardClass: ParameterizedTypeName,
        typeParameterizedProvider: ParameterizedTypeName
    ): ParameterizedTypeName {
        return ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            wildCardClass,
            typeParameterizedProvider
        )
    }

    /**
     * Defines the field which would store created ViewModels:
     *
     * ```
     * private final Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModelMap
     * ```
     *
     * @param mapType type of the generated field.
     * @return [FieldSpec] for the field of the map.
     */
    private fun generateMapField(mapType: ParameterizedTypeName): FieldSpec {
        return FieldSpec.builder(mapType, VIEWMODEL_FIELD_NAME)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build()
    }

    /**
     * Generated a constructor for the factory:
     *
     * ```
     * @Inject
     * public GeneratedViewModelFactory( Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModelMap)
     * {
     *    this.viewModelMap = viewModelMap;
     * }
     * ```
     *
     * @param mapType type of the parameter for the constructor.
     * @return [MethodSpec] for the constructor for the factory.
     */
    private fun generateConstructor(mapType: ParameterizedTypeName): MethodSpec {
        return MethodSpec.constructorBuilder()
            .addAnnotation(Inject::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapType, VIEWMODEL_FIELD_NAME)
            .addStatement("this.\$N = \$N", VIEWMODEL_FIELD_NAME, VIEWMODEL_FIELD_NAME)
            .build()
    }

    /**
     * Generates type of the parameter for the [ViewModelProvider.Factory] create method.
     *
     * ```
     * Class<T>
     * ```
     *
     * @return [ParameterizedTypeName] which defines the parameter type.
     */
    private fun generateParameterType(): ParameterizedTypeName {
        return ParameterizedTypeName.get(ClassName.get(Class::class.java), TypeVariableName.get("T"))
    }

    /**
     * Creates a parameter for the [ViewModelProvider.Factory] create method.
     *
     * ```
     * Class<T> modelClass
     * ```
     *
     * @param paramType type of the parameter.
     * @return [ParameterSpec] for the parameter of the create method.
     * @see [generateCreateMethod]
     */
    private fun generateClassParam(paramType: ParameterizedTypeName): ParameterSpec {
        return ParameterSpec.builder(paramType, "modelClass").build()
    }

    /**
     * Defines access method would be used by the [ViewModelProvider] to provide created ViewModels
     * to the target activity or fragment.
     *
     * ```
     * @Override
     * public <T extends ViewModel> T create(Class<T> modelClass) {
     *    final Provider<ViewModel> vmProvider = viewModelMap.get(modelClass);
     *    if(vmProvider == null) {
     *       throw new IllegalArgumentException("ViewModel isn't supported by the factory.");
     *    }
     *    final ViewModel viewModel = vmProvider.get();
     *    if(modelClass.isAssignableFrom(viewModel.getClass())) {
     *       return (T) viewModel;
     *    } else {
     *       throw new IllegalArgumentException("Another ViewModel implementation was expected.");
     *    }
     * }
     * ```
     *
     * @param classParam create method parameter representation
     * @param typeParameterizedProvider type of the
     * @return [MethodSpec] to provide ViewModel to the user
     */
    private fun generateCreateMethod(classParam: ParameterSpec, typeParameterizedProvider: Any): MethodSpec {
        return MethodSpec.methodBuilder("create")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(TypeVariableName.get("T", ViewModel::class.java))
            .returns(TypeVariableName.get("T"))
            .addParameter(classParam)
            .addStatement(
                "final \$T %s = %s.get(%s)".format(
                    "vmProvider",
                    VIEWMODEL_FIELD_NAME,
                    "modelClass"
                ), typeParameterizedProvider
            )
            .beginControlFlow("if(%s == null)".format("vmProvider"))
            .addStatement(
                "throw new \$T(\"%s\")".format("ViewModel isn't supported by the factory."),
                IllegalArgumentException::class.java
            )
            .endControlFlow()
            .addStatement(
                "final \$T %s = %s.get()".format("viewModel", "vmProvider"),
                ViewModel::class.java
            )
            .beginControlFlow(
                "if(%s.isAssignableFrom(%s.getClass()))".format(
                    "modelClass",
                    "viewModel"
                )
            )
            .addStatement("return (\$T) %s".format("viewModel"), TypeVariableName.get("T"))
            .nextControlFlow("else")
            .addStatement(
                "throw new \$T(\"%s\")".format("Another ViewModel implementation was expected."),
                IllegalArgumentException::class.java
            )
            .endControlFlow()
            .build()
    }

    private companion object {
        private const val DEFAULT_FACTORY_NAME = "GeneratedViewModelFactory"
        private const val VIEWMODEL_FIELD_NAME = "viewModelMap"
    }
}
