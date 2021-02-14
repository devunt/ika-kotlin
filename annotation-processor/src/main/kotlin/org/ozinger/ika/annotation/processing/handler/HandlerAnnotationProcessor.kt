package org.ozinger.ika.annotation.processing.handler

import com.google.auto.service.AutoService
import org.ozinger.ika.annotation.Handler
import org.ozinger.ika.annotation.processing.Generator
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class HandlerAnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private var kaptKotlinGeneratedDir: String? = null

    private lateinit var providerGenerator: Generator
    private lateinit var moduleGenerator: Generator

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        providerGenerator = ProviderGenerator(processingEnv, kaptKotlinGeneratedDir)
        moduleGenerator = ModuleGenerator(processingEnv, kaptKotlinGeneratedDir)
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        if (kaptKotlinGeneratedDir == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return false
        }

        val set = roundEnv.getElementsAnnotatedWith(Handler::class.java)
        providerGenerator.process(set)
        moduleGenerator.process(set)

        return true
    }

    override fun getSupportedSourceVersion() = SourceVersion.latest()
    override fun getSupportedAnnotationTypes() = mutableSetOf(Handler::class.qualifiedName!!)
    override fun getSupportedOptions() = mutableSetOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)
}