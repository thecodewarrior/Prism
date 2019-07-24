package com.teamwizardry.prism

import com.teamwizardry.mirror.type.ConcreteTypeMirror

/**
 * Type analyzers are the backend for many serializers, providing a format-agnostic way to implement the complex
 * process of extracting serializable data from an object and later integrating that data back in, either by modifying
 * an existing object or creating a new one.
 *
 * A type analyzer takes analyzes types and objects to determine the best way to get and set data. This often includes
 * decisions such as whether a new instance of the object is needed, and then if so what constructor to use. Some
 * serializers will change how they set data based on that data, such as not instantiating new objects if only a
 *
 * Type analyzers each have internal buffers to store data before serializing or deserializing. Among the simplest
 * buffer would be for a List serializer, which would have an internal list where elements that need to be serialized
 * and elements that have just been deserialized are stored temporarily. Doing it this way means the analyzer has
 * access to all the data when it comes time to deserialize, allowing for much more complex deserialization strategies.
 *
 * Because of their internal buffers, type analyzers are not thread safe.
 */
abstract class TypeAnalyzer<T: Any, S: Serializer<*>>(val prism: Prism<S>, val type: ConcreteTypeMirror) {
    /**
     * Reset this analyzer's internal buffer
     */
    abstract fun clear()

    /**
     * Apply the current buffer to the passed object or create and initialize a new object if null is passed or
     * instantiation is otherwise necessary.
     *
     * @throws InstantiationException if an object instantiation was required but could not be performed
     * @throws PrismException if there was an error applying the buffer
     *
     * @return The passed object, or the newly created object if instantiation was necessary.
     */
    abstract fun apply(target: T?): T

    /**
     * Clear the buffer and populate it with the data from the passed object.
     *
     * @throws PrismException if there was an error populating the buffer
     */
    abstract fun populate(value: T)
}