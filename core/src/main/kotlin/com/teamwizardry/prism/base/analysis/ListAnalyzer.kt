package com.teamwizardry.prism.base.analysis

import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.IllegalTypeException
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.Serializer
import com.teamwizardry.prism.TypeAnalyzer

class ListAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror): TypeAnalyzer<MutableList<T>, S>(prism, type) {

    val listType: ClassMirror = type.findSuperclass(List::class.java)?.asClassMirror()
        ?: throw IllegalTypeException()
    val elementType: TypeMirror = listType.typeParameters[0]
    val elementSerializer: S by prism[elementType]

    var buffer = ArrayList<T>()
        private set

    private var constructor = type.declaredConstructors.find { it.parameters.isEmpty() }!! // TODO replace with helper

    /**
     * Ensures the buffer can contain at least [length] elements. This can be used in cases where the element count is
     * known before deserializing
     */
    fun reserve(length: Int) {
        buffer.ensureCapacity(length)
    }

    fun add(value: T) {
        buffer.add(value)
    }

    override fun clear() {
        buffer.clear()
    }

    override fun apply(target: MutableList<T>?): MutableList<T> {
        val value: MutableList<T>
        if(target != null) {
            value = target
            value.clear()
        } else {
            value = constructor.call()
        }

        value.addAll(buffer)

        if(buffer.size > 500) {
            buffer = ArrayList()
        } else {
            buffer.clear()
        }

        return value
    }

    override fun populate(value: MutableList<T>) {
        clear()
        buffer.addAll(value)
    }
}