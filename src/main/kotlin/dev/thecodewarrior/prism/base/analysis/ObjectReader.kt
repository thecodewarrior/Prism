package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeReader

/**
 * ObjectReader encapsulates the state necessary for reading an object from an abstract data source.
 *
 * Deserializers using an ObjectReader *must* call [setValue] exactly once for every property, unless that property is
 * marked as .
 */
public interface ObjectReader<T: Any, S: Serializer<*>>: TypeReader<T> {
    /**
     * The list of properties on this object in a stable order.
     */
    public val properties: List<Property<S>>

    /**
     * Gets the property with the given name, or null if no such property exists.
     */
    public fun getProperty(name: String): Property<S>?

    /**
     * An encapsulation of the state necessary for reading a property
     */
    public interface Property<S: Serializer<*>> {
        /**
         * The name of the property
         */
        public val name: String

        /**
         * The serializer for the property
         */
        public val serializer: S

        /**
         * Sets the deserialized value.
         */
        public fun setValue(value: Any?)
    }
}