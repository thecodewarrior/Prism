package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.base.analysis.InvalidRefractAnnotationException
import dev.thecodewarrior.prism.base.analysis.InvalidRefractSignatureException
import dev.thecodewarrior.prism.base.analysis.ObjectAnalysisException
import dev.thecodewarrior.prism.utils.annotation

internal object ConstructorScanner {

    fun findConstructor(type: ClassMirror, properties: List<ObjectProperty<*>>): ObjectConstructor {
        val refractConstructors = type.declaredConstructors.filter { it.annotations.isPresent<RefractConstructor>() }
        if (refractConstructors.isEmpty())
            throw DeserializationException("No @RefractConstructor exists to create new instances")

        if (refractConstructors.size > 1)
            throw ObjectAnalysisException(
                "${type.simpleName} has multiple @RefractConstructor constructors: " +
                        "[${refractConstructors.joinToString(", ")}]"
            )
        val constructor = refractConstructors.single()

        val annot = constructor.annotation<RefractConstructor>()!!
        var names = annot.value.toList()
        if (names.isEmpty()) {
            if (constructor.parameters.any { !it.hasName }) {
                throw InvalidRefractAnnotationException(
                    "$constructor was compiled without parameter names and had no names specified in its " +
                            "@RefractConstructor annotation"
                )
            }
            names = constructor.parameters.map { it.name }
            if (names.size != constructor.parameters.size) {
                throw InvalidRefractAnnotationException(
                    "$constructor's @RefractConstructor annotation has ${names.size} parameter names, but the " +
                            "constructor has ${constructor.parameters.size} parameters"
                )
            }
        }

        val propertyMap = properties.associateBy { it.name }
        val immutableNames = properties.filter { it.isImmutable }.map { it.name }

        val unknownNames = names.filter { it !in propertyMap }
        if (unknownNames.isNotEmpty()) {
            throw InvalidRefractSignatureException(
                "$constructor has parameter names with no corresponding property: " +
                        "[${unknownNames.joinToString(", ")}]"
            )
        }

        val missingImmutableNames = immutableNames.filter { it !in names }
        if (missingImmutableNames.isNotEmpty()) {
            throw InvalidRefractSignatureException(
                "$constructor is missing parameters for some immutable properties: " +
                        "[${missingImmutableNames.joinToString(", ")}]"
            )
        }

        val mismatchedTypes =
            names.filterIndexed { i, name -> propertyMap.getValue(name).type != constructor.parameterTypes[i] }
        if (mismatchedTypes.isNotEmpty()) {
            throw InvalidRefractSignatureException(
                "$constructor has parameter types that don't match their corresponding property: " +
                        "[${mismatchedTypes.joinToString(", ")}]"
            )
        }

        return ObjectConstructor(constructor, names)
    }
}

public class ObjectConstructor(public val mirror: ConstructorMirror, public val parameters: List<String>) {
    public fun createInstance(values: Array<Any?>): Any {
        return mirror.call(*values)
    }
}
