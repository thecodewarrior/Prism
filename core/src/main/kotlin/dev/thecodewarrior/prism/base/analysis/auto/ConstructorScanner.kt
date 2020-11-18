package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.utils.annotation

internal object ConstructorScanner {

    fun findConstructor(type: ClassMirror, properties: List<ObjectProperty<*>>): ObjectConstructor? {
        val propertyMap = properties.associateBy { it.name }
        val immutableNames = properties.filter { it.isImmutable }.map { it.name }
        val constructors = mutableListOf<ObjectConstructor>()

        type.declaredConstructors.forEach { constructor ->
            val annot = constructor.annotation<RefractConstructor>() ?: return@forEach
            var names = annot.value.toList()
            if(names.isEmpty()) {
                val parameterNames = constructor.parameters.map { it.name }
                if(parameterNames.any { it == null }) {
                    throw InvalidRefractAnnotationException("$constructor was compiled without parameter names and " +
                        "had no names specified in its @RefractConstructor annotation")
                }
                names = parameterNames.requireNoNulls()
            }

            val unknownNames = names.filter { it !in propertyMap }
            if(unknownNames.isNotEmpty()) {
                throw InvalidRefractSignatureException("Some constructor parameter names have no corresponding " +
                    "property: [${unknownNames.joinToString(", ")}]")
            }

            val missingImmutableNames = immutableNames.filter { it !in names }
            if(missingImmutableNames.isNotEmpty()) {
                throw InvalidRefractSignatureException("Some immutable properties were not present in the " +
                    "constructor: [${missingImmutableNames.joinToString(", ")}]")
            }

            val mismatchedTypes = names.filterIndexed { i, name -> propertyMap.getValue(name).type != constructor.parameterTypes[i] }
            if(mismatchedTypes.isNotEmpty()) {
                throw InvalidRefractSignatureException("Some constructor parameters have types that aren't equal to " +
                    "their corresponding property: [${mismatchedTypes.joinToString(", ")}]")
            }

            constructors.add(ObjectConstructor(constructor, names))
        }

        if(constructors.size > 1)
            throw ObjectAnalysisException("${type.simpleName} has multiple @RefractConstructor annotated constructors")

        return constructors.firstOrNull()
    }
}

public class ObjectConstructor(public val mirror: ConstructorMirror, public val parameters: List<String>) {
    public fun createInstance(values: Array<Any?>): Any {
        return mirror(*values)
    }
}
