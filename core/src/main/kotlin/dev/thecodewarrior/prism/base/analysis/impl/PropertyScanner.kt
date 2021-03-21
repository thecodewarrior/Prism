package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractImmutable
import dev.thecodewarrior.prism.annotation.RefractMutable
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.base.analysis.AnalysisError
import dev.thecodewarrior.prism.base.analysis.ObjectAnalysisException
import dev.thecodewarrior.prism.utils.allDeclaredMemberProperties
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

internal object PropertyScanner {
    fun <S: Serializer<*>> scan(prism: Prism<S>, type: ClassMirror): List<ObjectProperty<S>> {
        val candidates = mutableListOf<Candidate<S>>()
        val problems = mutableListOf<AnalysisError>()

        scanKotlin(prism, type, candidates, problems)
        scanAccessors(prism, type, candidates, problems)

        val namedCandidates = mutableMapOf<String, MutableList<Candidate<S>>>()

        for (candidate in candidates) {
            namedCandidates.getOrPut(candidate.name) { mutableListOf() }.add(candidate)
        }

        namedCandidates.filterValues { it.size > 1 }.forEach { (name, _) ->
            problems.add(AnalysisError("There are multiple candidates named `$name`", null))
        }

        val properties = namedCandidates.mapNotNull { (name, candidates) ->
            try {
                candidates[0].createProperty(prism)
            } catch (e: ObjectAnalysisException) {
                problems.add(AnalysisError("Exception analyzing property named `$name`", e))
            }
        }

        for (problem in problems) {

        }
    }

    private fun <S: Serializer<*>> scanKotlin(
        prism: Prism<S>,
        type: ClassMirror,
        candidates: MutableList<Candidate<S>>,
        errors: MutableList<AnalysisError>
    ) {
        for (property in type.kClass.allDeclaredMemberProperties) {
            val annotation = property.findAnnotation<Refract>() ?: continue
            if (annotation.value == "") {
                errors.add(AnalysisError("Property `${property.name}` has an empty @Refract name", null))
                continue
            }

            candidates.add(Candidate.Property(annotation.value, type, property))
        }
    }

    private fun <S: Serializer<*>> scanAccessors(
        prism: Prism<S>,
        type: ClassMirror,
        candidates: MutableList<Candidate<S>>,
        errors: MutableList<AnalysisError>
    ) {
        val scannedGetters = mutableMapOf<String, MutableList<MethodMirror>>()
        val scannedSetters = mutableMapOf<String, MutableList<MethodMirror>>()

        for (method in type.methods) {
            method.getAnnotation<RefractGetter>()?.also { annotation ->
                if (annotation.value == "") {
                    errors.add(AnalysisError("Getter `${method.name}` has an empty @RefractGetter name", null))
                } else {
                    scannedGetters.getOrPut(annotation.value) { mutableListOf() }.add(method)
                }
            }
            method.getAnnotation<RefractSetter>()?.also { annotation ->
                if (annotation.value == "") {
                    errors.add(AnalysisError("Setter `${method.name}` has an empty @RefractSetter name", null))
                } else {
                    scannedSetters.getOrPut(annotation.value) { mutableListOf() }.add(method)
                }
            }
        }

        scannedSetters.filterValues { it.size > 1 }.forEach { (name, _) ->
            errors.add(AnalysisError("Multiple setters have the same name: `$name`", null))
        }

        scannedGetters.filterValues { it.size > 1 }.forEach { (name, _) ->
            errors.add(AnalysisError("Multiple getters have the same name: `$name`", null))
        }

        scannedSetters.filterKeys { it !in scannedGetters }.forEach { (name, _) ->
            errors.add(AnalysisError("Setter named `$name` has no correspondingly named getter", null))
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
        abstract val declarationString: String
        abstract fun createProperty(prism: Prism<S>): ObjectProperty<S>

        class Accessor<S: Serializer<*>>(
            name: String,
            val getter: MethodMirror,
            val setter: MethodMirror?
        ): Candidate<S>(name) {

            override val declarationString: String
                get() = TODO("Not yet implemented")

            override fun createProperty(prism: Prism<S>): ObjectProperty<S> {
                return AccessorProperty(
                    name, getter.returnType, prism,
                    getter, setter
                )
            }
        }

        class Property<S: Serializer<*>>(
            name: String,
            val containingType: ClassMirror,
            val property: KProperty1<*, *>
        ): Candidate<S>(name) {

            override val declarationString: String
                get() = TODO("Not yet implemented")

            override fun createProperty(prism: Prism<S>): ObjectProperty<S> {
                val field = property.javaField?.let { containingType.getField(it) }
                val getter = property.getter.javaMethod?.let { containingType.getMethod(it) }
                val setter =
                    (property as? KMutableProperty<*>)?.setter?.javaMethod?.let { containingType.getMethod(it) }

                val propertyType = field?.type
                    ?: getter?.returnType
                    ?: throw ObjectAnalysisException("Property $property has no getter or backing field. How?")

                var isMutable = setter != null || field?.isFinal == false

                val refractMutable = property.hasAnnotation<RefractMutable>()
                val refractImmutable = property.hasAnnotation<RefractImmutable>()

                when {
                    refractMutable && refractImmutable -> {
                        throw ObjectAnalysisException(
                            "Property $property is annotated both @RefractMutable " +
                                "and @RefractImmutable"
                        )
                    }
                    refractMutable -> {
                        if (isMutable)
                            throw ObjectAnalysisException(
                                "Property $property is annotated @RefractMutable " +
                                    "but is already mutable"
                            )
                        if (field == null)
                            throw ObjectAnalysisException(
                                "Property $property is annotated @RefractMutable " +
                                    "but has no backing field"
                            )

                        isMutable = true
                    }
                    refractImmutable -> {
                        if (!isMutable)
                            throw ObjectAnalysisException(
                                "Property $property is annotated @RefractImmutable " +
                                    "but is already immutable"
                            )

                        isMutable = false
                    }
                }

                when {
                    getter != null -> {
                        // A Kotlin @RefractMutable property. Because it's a `val` it has no setter we have to use
                        // a hybrid property that reads using the getter but sets using the field
                        if (isMutable && setter == null) {
                            if (field == null)
                                throw ObjectAnalysisException(
                                    "Property $property has a getter, no setter, and is marked as mutable, " +
                                        "yet it has no backing field. How? This combination should " +
                                        "fail at the @RefractMutable stage."
                                )

                            return HybridMutableProperty(
                                name, propertyType, prism,
                                getter, field
                            )
                        }

                        return AccessorProperty(
                            name, propertyType, prism,
                            getter, if (isMutable) setter else null
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
        }
    }
}