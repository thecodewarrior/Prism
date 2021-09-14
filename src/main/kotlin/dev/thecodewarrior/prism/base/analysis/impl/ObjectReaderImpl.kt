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

    val constructor: ObjectConstructor = analyzer.constructor
    val constructorProperties: List<Property> = analyzer.constructor.parameters.map { propertyMap.getValue(it) }
    val constructorParameters: Array<Any?> = Array(analyzer.constructor.parameters.size) { null }
    val nonConstructorProperties: List<Property> = properties - constructorProperties

    override fun getProperty(name: String): ObjectReader.Property<S>? = propertyMap[name]

    override fun release() {
        properties.forEach { it.clear() }
        analyzer.release(this)
    }

    override fun build(): T {
        if(properties.any { !it.valueExists }) {
            val missing = properties.filter { !it.valueExists }.map { it.name }
            throw DeserializationException("Missing values for properties (${missing.joinToString(", ")})")
        }

        constructorProperties.forEachIndexed { i, property ->
            constructorParameters[i] = property.value_
        }
        val result = constructor.createInstance(constructorParameters)
        constructorParameters.fill(null) // so the values can be GC'd

        nonConstructorProperties.forEach { property ->
            property.property.setValue(result, property.value_)
        }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    inner class Property(val property: ObjectProperty<S>): ObjectReader.Property<S> {
        override val name: String = property.name
        override val serializer: S
            get() = property.serializer

        var valueExists = false
        var value_: Any? = null
            set(value) {
                field = value
                valueExists = true
            }

        fun clear() {
            value_ = null
            valueExists = false
        }

        override fun setValue(value: Any?) {
            value_ = value
            valueExists = true
        }
    }
}
