package org.ozinger.ika.annotation

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ExecutableType
import javax.lang.model.util.ElementScanner14
import javax.tools.Diagnostic
import kotlin.reflect.KCallable

@AutoService(Processor::class)
class HandlerAnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private val type = ClassName("kotlin.collections", "MutableMap").parameterizedBy(
        Pair::class.parameterizedBy(
            String::class, String::class
        ),
        ClassName("kotlin.collections", "MutableList").parameterizedBy(
            KCallable::class.parameterizedBy(Any::class),
        )
    )
    private val fileSpecBuilder = FileSpec.builder("org.ozinger.ika.event", "GeneratedHandlers")
        .addImport("kotlin.reflect.full", "callSuspend")
    private val classBuilder = TypeSpec.classBuilder("GeneratedHandlers")
        .addSuperinterface(ClassName("org.koin.core.component", "KoinComponent"))
        .addProperty(
            PropertySpec.builder("map", type, KModifier.PRIVATE)
                .initializer("mutableMapOf()")
                .build()
        )
        .addFunction(
            FunSpec.builder("add")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("originFqName", String::class)
                .addParameter("commandFqName", String::class)
                .addParameter("handler", KCallable::class.parameterizedBy(Any::class))
                .addCode(
                    """
                val key = Pair(originFqName, commandFqName)
                map.getOrPut(key, ::mutableListOf).add(handler)
            """.trimIndent()
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("collect")
                .addModifiers(KModifier.SUSPEND)
                .addCode(
                    """
                while (true) {
                    val packet = CentralEventBus.Incoming.get()
                    map[Pair(packet.origin::class.qualifiedName,
                        packet.command::class.qualifiedName)]?.forEach { method ->
                        method.callSuspend(packet.origin, packet.command)
                    }
                }
            """.trimIndent()
                )
                .build()
        )
    private val initBuilder = CodeBlock.builder()

    @KotlinPoetMetadataPreview
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return false
        }

        if (roundEnv.processingOver()) {
            fileSpecBuilder.addType(classBuilder.addInitializerBlock(initBuilder.build()).build()).build()
                .writeTo(File(kaptKotlinGeneratedDir))
        }

        roundEnv.getElementsAnnotatedWith(Handler::class.java)
            ?.filter { it.kind == ElementKind.CLASS }
            ?.forEach { clazz ->
                clazz.accept(object : ElementScanner14<Unit, Void>() {
                    override fun visitExecutable(e: ExecutableElement, p: Void?) {
                        if (e.kind != ElementKind.METHOD) {
                            return
                        }

                        if (e.getAnnotation(Handler::class.java) == null) {
                            return
                        }

                        val type = e.asType() as ExecutableType
//                        if (type.parameterTypes.size != 2) {
//                            processingEnv.messager.printMessage(
//                                Diagnostic.Kind.ERROR,
//                                "Method $e must have 2 parameters."
//                            )
//                            return
//                        }
                        // 주석 해제하면 suspend fun 관련해서 인자 개수 오류남

//                        printMessage(e.)
                        val packageName = processingEnv.elementUtils.getPackageOf(e).qualifiedName
                        val className = clazz.simpleName
                        val methodName = e.simpleName

                        initBuilder.addStatement(
                            "add(%S, %S, %L)",
                            type.parameterTypes[0].toString(),
                            type.parameterTypes[1].toString(),
                            "$packageName.$className.Companion::$methodName"
                        )
                    }
                }, null)


//                val metadata = KotlinClassMetadata.read(clazz.getAnnotation(Metadata::class.java).run {
//                    KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
//                }) as KotlinClassMetadata.Class
//                val kmClass = metadata.toImmutableKmClass()
            }
        return true
    }

    private fun printMessage(message: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "$message\r\n")
    }

    override fun getSupportedSourceVersion() = SourceVersion.latest()
    override fun getSupportedAnnotationTypes() = mutableSetOf(Handler::class.qualifiedName!!)
    override fun getSupportedOptions() = mutableSetOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)
}