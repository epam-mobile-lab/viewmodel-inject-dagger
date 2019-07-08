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

package com.epam.inject.viewmodel.processor.filer

import com.epam.inject.viewmodel.processor.error
import com.epam.inject.viewmodel.processor.note
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.FilerException
import javax.annotation.processing.ProcessingEnvironment

/**
 * Writes generated classes to the destination files.
 * Files created with [JavaFile.builder] and wrote with [ProcessingEnvironment.getFiler].
 */
internal class FileWriter(processingEnvironment: ProcessingEnvironment) {

    private val messager = processingEnvironment.messager
    private val filer = processingEnvironment.filer

    /**
     * Creates source file from [TypeSpec] using [JavaFile.builder], also using Filer(from [ProcessingEnvironment]).
     * Path - [DEFAULT_PACKAGE], filename - [genClass] name.
     * @param genClass class spec to writing in the file.
     */
    fun writeToFile(genClass: TypeSpec) {
        val genFile: JavaFile = JavaFile.builder(DEFAULT_PACKAGE, genClass).build()

        try {
            genFile.writeTo(filer)
            messager.note("${genClass.name} was created.")
        } catch (e: FilerException) {
            messager.error("File can't be generated due to outlined exception:")
            messager.error(e.toString())
        }
    }

    private companion object {
        private const val DEFAULT_PACKAGE = "com.epam.inject.viewmodel"
    }
}
