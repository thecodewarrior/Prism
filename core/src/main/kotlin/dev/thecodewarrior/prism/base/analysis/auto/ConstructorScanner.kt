package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.utils.annotation

class InstantiatorScanner<S: Serializer<*>>(val prism: Prism<S>, val type: ClassMirror, val properties: List<ObjectProperty<S>>) {
    val instantiators: List<ObjectInstantiator<S>>

    private fun populateMembers(): List<ObjectInstantiator<S>> {
        val propertyMap = properties.associateBy { it.name }
        val instantiators = mutableListOf<ObjectInstantiator<S>>()

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
            val missingNames = names.filter { it !in propertyMap }
            if(missingNames.isNotEmpty()) {
                throw InvalidRefractSignatureException("Some constructor parameter names have no corresponding " +
                    "property: [${missingNames.joinToString(", ")}]")
            }
            instantiators.add(ConstructorInstantiator(prism, constructor, names.map { propertyMap.getValue(it) }, annot.priority))
        }

        return instantiators
    }

    init {
        instantiators = populateMembers().sortedWith(Comparator { a, b ->
            -compareValues(a.priority, b.priority).let {
                if(it == 0)
                    compareValues(a.properties.size, b.properties.size)
                else
                    it
            }
        })
    }
}


abstract class ObjectInstantiator<S: Serializer<*>>(val properties: List<ObjectProperty<S>>, val priority: Int) {
    val propertySet: Set<ObjectProperty<S>> = properties.toSet()

    /**
     * Creates an instance with the passed positional values (each value corresponds to the same element in the
     * [properties] list)
     */
    abstract fun createInstance(values: List<Any?>): Any
}

class ConstructorInstantiator<S: Serializer<*>>(prism: Prism<S>, val mirror: ConstructorMirror,
    val parameters: List<ObjectProperty<S>>, priority: Int): ObjectInstantiator<S>(parameters, priority) {
    override fun createInstance(values: List<Any?>): Any {
        return mirror(*values.toTypedArray())
    }
}
