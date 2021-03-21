package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.PropertyAccessException
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.base.analysis.ObjectAnalyzer
import dev.thecodewarrior.prism.base.analysis.ObjectReader
import dev.thecodewarrior.prism.internal.unmodifiableView

internal class ObjectReaderImpl<T: Any, S: Serializer<*>>(val analyzer: ObjectAnalyzer<T, S>): ObjectReader<T, S> {
    override val properties: List<Property> = analyzer.properties.map { Property(it) }.unmodifiableView()
    private val propertyMap: Map<String, Property> = properties.associateBy { it.name }

    val immutableProperties: List<Property> = properties.filter { it.property.isImmutable }.unmodifiableView()
    val mutableProperties: List<Property> = properties.filter { !it.property.isImmutable }.unmodifiableView()

    val constructor: ObjectConstructor? = analyzer.constructor
    val constructorProperties: List<Property> = analyzer.constructor?.parameters?.map { propertyMap.getValue(it) }.orEmpty()
    val constructorParameters: Array<Any?> = analyzer.constructor?.let { Array<Any?>(it.parameters.size) { null } } ?: arrayOf()

    val remaining = mutableListOf<Property>() // reused to avoid allocating a new list for every object deserialized

    private var existing: T? = null

    override fun getProperty(name: String): ObjectReader.Property<S>? = propertyMap[name]

    override fun release() {
        properties.forEach { it.clear() }
        existing = null
        analyzer.release(this)
    }

    override fun build(): T {
        // if any properties are missing:
        //    throw
        // add all mutable properties to the remaining property list
        //
        // needsInstance = existing == null || any immutable properties changed
        // if needsInstance
        //     assemble constructor argument list
        //     remove properties from remaining properties
        //     result = call constructor
        // else
        //     result = existing
        //
        // for each remaining property
        //     set that property
        //
        // return the result


        if(properties.any { !it.valueExists }) {
            val missing = properties.filter { !it.valueExists }.map { it.name }
            throw DeserializationException("Missing values for properties (${missing.joinToString(", ")})")
        }

        remaining.clear()
        remaining.addAll(mutableProperties)

        val needsInstance = existing == null ||
            immutableProperties.any { it.didChange() }

        val result: Any
        if(needsInstance) {
            if(constructor == null) {
                if(existing == null) {
                    throw DeserializationException("Existing value is null but no @RefractConstructor exists to " +
                        "create a new instance")
                } else {
                    val changed = immutableProperties.filter { it.didChange() }
                    throw DeserializationException("Immutable properties changed " +
                        "(${changed.joinToString(", ") { it.name }}) and no @RefractConstructor exists to create a " +
                        "new instance")
                }
            }
            constructorProperties.forEachIndexed { i, property ->
                constructorParameters[i] = property.value_
            }
            remaining.removeAll(constructorProperties)
            result = constructor.createInstance(constructorParameters)
            constructorParameters.fill(null)
        } else {
            result = existing!!
        }

        remaining.forEach { property ->
            if(!property.property.isImmutable)
                property.property.setValue(result, property.value_)
        }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    inner class Property(val property: ObjectProperty<S>): ObjectReader.Property<S> {
        override val name: String = property.name
        override val serializer: S
            get() = property.serializer

        private var existingCached = false
        private var _existing: Any? = null

        var valueExists = false
        var value_: Any? = null
            set(value) {
                field = value
                valueExists = true
            }

        override val existing: Any?
            get() {
                if(!existingCached) {
                    _existing = getPropertyValue()
                    existingCached = true
                }
                return _existing
            }

        fun clear() {
            _existing = null
            existingCached = false
            value_ = null
            valueExists = false
        }

        private fun getPropertyValue(): Any? {
            try {
                return property.getValue(this@ObjectReaderImpl.existing ?: return null)
            } catch(e: Exception) {
                throw PropertyAccessException("Reading the value of ${property.name}", e)
            }
        }

        fun didChange(): Boolean {
            val existing = existing
            val value = value_
            @Suppress("UNCHECKED_CAST")
            return (existing == null) != (value == null)
        }

        override fun setValue(value: Any?) {
            TODO("Not yet implemented")
        }
    }
}
