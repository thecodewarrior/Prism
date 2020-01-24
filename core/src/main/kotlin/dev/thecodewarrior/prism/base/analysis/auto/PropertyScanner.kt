package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.InvalidTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.internal.identitySetOf
import dev.thecodewarrior.prism.utils.allDeclaredMemberProperties
import dev.thecodewarrior.prism.utils.annotation
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

class PropertyScanner<S: Serializer<*>>(val prism: Prism<S>, val type: ClassMirror) {
    val candidates = mutableMapOf<String, PropertyCandidate>()
    val properties: List<ObjectProperty<S>>

    inner class PropertyCandidate(val name: String) {
        val kotlin: MutableSet<KProperty<*>> = identitySetOf()
        val fields: MutableSet<FieldMirror> = identitySetOf()
        val getters: MutableSet<MethodMirror> = identitySetOf()
        val setters: MutableSet<MethodMirror> = identitySetOf()

        fun resolve(): ObjectProperty<S> {

            val count = kotlin.size + fields.size + getters.size * 0.5 + setters.size * 0.5

            if(count != 1.0)
                throw InvalidTypeException("multiple properties or fields or getters for $name")

            if(kotlin.isNotEmpty())
                throw error("kotlin properties not implemented yet")

            if(fields.isNotEmpty())
                return FieldProperty(prism, name, fields.first())

            if(getters.isEmpty() || setters.isNotEmpty())
                throw InvalidTypeException("setter with no getter")
            return AccessorProperty(prism, name, getters.first(), setters.firstOrNull())
        }
    }

    private fun getCandidate(name: String) = candidates.getOrPut(name) { PropertyCandidate(name) }

    private fun populateMembers() {
        type.allFields.forEach { field ->
            val annot = field.annotation<Refract>() ?: return@forEach
            val name = if (annot.value.isBlank()) field.name else annot.value
            getCandidate(name).fields.add(field)
        }

        type.allMethods.forEach { method ->
            method.annotation<RefractGetter>()?.also { annot ->
                getCandidate(annot.value).getters.add(method)
            }
            method.annotation<RefractSetter>()?.also { annot ->
                getCandidate(annot.value).setters.add(method)
            }
        }

        type.kClass!!.allDeclaredMemberProperties.forEach { property ->
            val annot = property.findAnnotation<Refract>() ?: return@forEach
            val name = if(annot.value.isBlank()) property.name else annot.value
            getCandidate(name).kotlin.add(property)
        }
    }

    init {
        populateMembers()
        properties = candidates.map { it.value.resolve() }
    }
}

abstract class ObjectProperty<S: Serializer<*>>(val name: String) {
    abstract val isImmutable: Boolean
    abstract val serializer: S
    abstract fun getValue(target: Any): Any?
    abstract fun setValue(target: Any, value: Any?)
}

class FieldProperty<S: Serializer<*>>(prism: Prism<S>, name: String, val mirror: FieldMirror): ObjectProperty<S>(name) {
    override val isImmutable: Boolean
        get() = mirror.isFinal
    override val serializer: S by prism[mirror.type]

    override fun getValue(target: Any): Any? {
        return mirror.get(target)
    }

    override fun setValue(target: Any, value: Any?) {
        mirror.set(target, value)
    }
}

class AccessorProperty<S: Serializer<*>>(prism: Prism<S>, name: String, val getter: MethodMirror, val setter: MethodMirror?): ObjectProperty<S>(name) {
    override val isImmutable: Boolean
        get() = setter != null
    override val serializer: S by prism[getter.returnType]

    override fun getValue(target: Any): Any? {
        return getter.call(target)
    }

    override fun setValue(target: Any, value: Any?) {
        setter?.call(target, value) as Any?
    }
}
