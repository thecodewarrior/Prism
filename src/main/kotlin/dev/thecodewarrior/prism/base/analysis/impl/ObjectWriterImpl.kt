package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.prism.PropertyAccessException
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.base.analysis.ObjectAnalyzer
import dev.thecodewarrior.prism.base.analysis.ObjectWriter
import dev.thecodewarrior.prism.internal.unmodifiableView
import java.lang.IllegalStateException

internal class ObjectWriterImpl<T: Any, S: Serializer<*>>(val analyzer: ObjectAnalyzer<T, S>): ObjectWriter<T, S> {
    override val properties: List<Property> = analyzer.properties.map { Property(it) }.unmodifiableView()
    private val propertyMap: Map<String, Property> = properties.associateBy { it.name }

    private var _value: T? = null
    private val value: Any
        get() = _value ?: throw IllegalStateException("No value has been loaded")

    override fun getProperty(name: String): ObjectWriter.Property<S>? = propertyMap[name]

    override fun load(value: T) {
        _value = value
    }

    override fun release() {
        properties.forEach {
            it.clear()
        }
        _value = null
        analyzer.release(this)
    }

    inner class Property(val property: ObjectProperty<S>): ObjectWriter.Property<S> {
        override val name: String = property.name
        override val serializer: S
            get() = property.serializer

        private var cached = false
        private var cachedValue: Any? = null

        override val value: Any?
            get() {
                if(!cached) {
                    cached = true
                    cachedValue = getPropertyValue()
                }
                return cachedValue
            }

        private fun getPropertyValue(): Any? {
            try {
                return property.getValue(this@ObjectWriterImpl.value)
            } catch(e: Exception) {
                throw PropertyAccessException("Reading the value of ${property.name}", e)
            }
        }

        internal fun clear() {
            cached = false
            cachedValue = null
        }
    }
}
