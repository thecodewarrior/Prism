package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.base.analysis.AnalysisError
import dev.thecodewarrior.prism.base.analysis.ObjectAnalysisException
import dev.thecodewarrior.prism.utils.allDeclaredMemberProperties
import dev.thecodewarrior.prism.utils.annotation
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

internal object PropertyScanner {
    private val log = LogManager.getLogger()

    fun <S: Serializer<*>> scan(prism: Prism<S>, type: ClassMirror): List<ObjectProperty<S>> {
        val candidates = mutableListOf<Candidate<S>>()
        val problems = mutableListOf<AnalysisError>()

        scanKotlin(type, candidates, problems)
        scanAccessors(type, candidates, problems)

        val namedCandidates = mutableMapOf<String, MutableList<Candidate<S>>>()

        for (candidate in candidates) {
            namedCandidates.getOrPut(candidate.name) { mutableListOf() }.add(candidate)
        }

        namedCandidates.filterValues { it.size > 1 }.forEach { (name, candidates) ->
            problems.add(AnalysisError("There are multiple candidates named `$name`: ${candidates.joinToString { "$it" }}", null))
        }

        val properties = namedCandidates.mapNotNull { (name, candidates) ->
            try {
                candidates.singleOrNull()?.createProperty(prism)
            } catch (e: ObjectAnalysisException) {
                problems.add(AnalysisError("Exception creating property named `$name`", e))
                null
            }
        }

        if(problems.isNotEmpty()) {
            log.error("Error(s) scanning for properties on $type:")
            problems.forEach { problem ->
                log.error(problem.message, problem.cause)
            }
            throw ObjectAnalysisException("Error(s) scanning for properties on $type.")
        }

        return properties
    }

    private fun <S: Serializer<*>> scanKotlin(
        type: ClassMirror,
        candidates: MutableList<Candidate<S>>,
        errors: MutableList<AnalysisError>
    ) {
        for (property in type.kClass.allDeclaredMemberProperties) {
            val annotation = property.findAnnotation<Refract>() ?: continue
            candidates.add(Candidate.Property(annotation.value, type, property))
        }
    }

    private fun <S: Serializer<*>> scanAccessors(
        type: ClassMirror,
        candidates: MutableList<Candidate<S>>,
        errors: MutableList<AnalysisError>
    ) {
        val scannedGetters = mutableMapOf<String, MutableList<MethodMirror>>()
        val scannedSetters = mutableMapOf<String, MutableList<MethodMirror>>()

        for (method in type.methods) {
            method.annotation<RefractGetter>()?.also { annotation ->
                scannedGetters.getOrPut(annotation.value) { mutableListOf() }.add(method)
            }
            method.annotation<RefractSetter>()?.also { annotation ->
                scannedSetters.getOrPut(annotation.value) { mutableListOf() }.add(method)
            }
        }

        scannedSetters.filterValues { it.size > 1 }.forEach { (name, setters) ->
            errors.add(AnalysisError("Multiple setters for property `$name`: [${setters.joinToString { it.name }}]", null))
        }

        scannedGetters.filterValues { it.size > 1 }.forEach { (name, getters) ->
            errors.add(AnalysisError("Multiple getters for property `$name`: [${getters.joinToString { it.name }}]", null))
        }

        scannedSetters.filterKeys { it !in scannedGetters }.forEach { (name, setters) ->
            errors.add(AnalysisError("Setter for property `$name` (${setters.single().name}) has no corresponding getter", null))
        }

        for ((name, getters) in scannedGetters) {
            val setters = scannedSetters[name]
            if (getters.size != 1 || (setters != null && setters.size != 1)) {
                continue // these errors will have been handled by the previous filter loops
            }
            candidates.add(Candidate.Accessor(name, getters.single(), setters?.single()))
        }
    }

    private sealed class Candidate<S: Serializer<*>>(val name: String) {
        abstract fun createProperty(prism: Prism<S>): ObjectProperty<S>

        class Accessor<S: Serializer<*>>(
            name: String,
            val getter: MethodMirror,
            val setter: MethodMirror?
        ): Candidate<S>(name) {

            override fun createProperty(prism: Prism<S>): ObjectProperty<S> {
                return AccessorProperty(
                    name, getter.returnType, prism,
                    getter, setter
                )
            }

            override fun toString(): String {
                return setter?.let { "${getter.name}()+${setter.name}()" } ?: getter.name
            }
        }

        class Property<S: Serializer<*>>(
            name: String,
            val containingType: ClassMirror,
            val property: KProperty1<*, *>
        ): Candidate<S>(name) {

            override fun createProperty(prism: Prism<S>): ObjectProperty<S> {
                val field = property.javaField?.let { containingType.getField(it) }
                val getter = property.getter.javaMethod?.let { containingType.getMethod(it) }
                val setter =
                    (property as? KMutableProperty<*>)?.setter?.javaMethod?.let { containingType.getMethod(it) }

                val propertyType = field?.type
                    ?: getter?.returnType
                    ?: throw ObjectAnalysisException("Property $property has no getter or backing field. How?")

                val isMutable = setter != null || field?.isFinal == false

                when {
                    getter != null -> {
                        return AccessorProperty(
                            name, propertyType, prism,
                            getter, setter
                        )
                    }
                    field != null -> {
                        return FieldProperty(
                            name, propertyType, prism,
                            !isMutable, field
                        )
                    }
                    else -> {
                        throw ObjectAnalysisException("Property $property has no getter and no backing field. How?")
                    }
                }
            }

            override fun toString(): String {
                return (if(property is KMutableProperty<*>) "var " else "val ") + property.name
            }
        }
    }
}