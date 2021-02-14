package org.ozinger.ika.annotation.processing

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

abstract class Generator(
    protected val processingEnv: ProcessingEnvironment,
    protected val kaptKotlinGeneratedDir: String?,
) {
    abstract fun process(elements: Set<Element>)

    protected fun note(message: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "$message\r\n")
    }
}