package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeWriter

public interface ObjectWriter<T: Any, S: Serializer<*>>: TypeWriter<T> {
    public val properties: List<Property<S>>
    public fun getProperty(name: String): Property<S>?

    public interface Property<S: Serializer<*>> {
        public val name: String
        public val serializer: S
        public val value: Any?
    }
}