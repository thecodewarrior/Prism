package com.teamwizardry.prism.base.analysis

import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.Serializer
import com.teamwizardry.prism.TypeAnalysis
import com.teamwizardry.prism.TypeAnalyzer
import com.teamwizardry.prism.annotation.Refract

class ObjectAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror): TypeAnalyzer<ObjectAnalyzer<T, S>.ObjectAnalysis, S>(prism, type)  {
    val properties: List<ObjectProperty>
    val nameMap: Map<String, ObjectProperty>

    init {
        properties = type.allFields
            .filter { field -> field.annotations.any { it is Refract } }
            .map { FieldProperty(it) }

        nameMap = properties.associateBy { it.name }
    }

    override fun createState(): ObjectAnalysis = ObjectAnalysis()

    fun applyValues(target: Any?, values: Map<ObjectProperty, ValueContainer>): Any {
        val needsInstance = target == null || values.any { (property, value) ->
            value.present && property.isFinal
        }
        if(needsInstance) {
            TODO()
        } else {
            target!!
            //todo: missing keys == error?
            values.forEach { (property, value) ->
                if(value.present) {
                    property.setValue(target, value.value)
                }
            }
            return target
        }
    }

    inner class ObjectAnalysis: TypeAnalysis<Any>() {
        val values = properties.associateWith { ValueContainer() }

        override fun clear() {
            values.values.forEach {
                it.value = null
                it.present = false
            }
        }

        fun setValue(property: ObjectProperty, value: Any?) {
            values[property]?.also {
                it.present = true
                it.value = value
            }
        }

        fun getValue(property: ObjectProperty): Any? {
            return values[property]?.value
        }

        override fun populate(value: Any) {
            values.forEach { (property, container) ->
                container.present = true
                container.value = property.getValue(value)
            }
        }

        override fun apply(target: Any?): Any {
            return applyValues(target, values)
        }

    }

    class ValueContainer {
        var present: Boolean = false
        var value: Any? = null
    }

    abstract inner class ObjectProperty(val name: String) {
        abstract val isFinal: Boolean
        abstract val serializer: S
        abstract fun getValue(target: Any): Any?
        abstract fun setValue(target: Any, value: Any?)
    }

    inner class FieldProperty(val mirror: FieldMirror): ObjectProperty(mirror.name) {
        override val isFinal: Boolean
            get() = mirror.isFinal
        override val serializer: S by prism[mirror.type]

        override fun getValue(target: Any): Any? {
            return mirror.get(target)
        }

        override fun setValue(target: Any, value: Any?) {
            mirror.set(target, value)
        }
    }
}