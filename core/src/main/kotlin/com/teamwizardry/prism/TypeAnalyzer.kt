package com.teamwizardry.prism

import com.teamwizardry.mirror.type.ConcreteTypeMirror
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Type analyzers are the backend for many serializers, providing a format-agnostic way to implement the complex
 * process of extracting serializable data from an object and later integrating that data back in, either by modifying
 * an existing object or creating a new one.
 *
 * A type analyzer analyzes types and objects to determine the best way to get and set data. This often includes
 * decisions such as whether a new instance of the object is needed, and then if so what constructor to use. Some
 * serializers will change how they set data based on that data, such as not instantiating new objects if only a
 *
 * Type analyzers can have multiple state instances, each of which has internal buffers to store data before
 * serializing or deserializing. Among the simplest buffer would be for a List serializer, which would have an internal
 * list where elements that need to be serialized and elements that have just been deserialized are stored temporarily.
 * Doing it this way means the analyzer has access to all the data when it comes time to deserialize, allowing for much
 * more complex deserialization strategies.
 *
 * Well-built analyzers should be thread safe, however the [TypeAnalysis] objects they return may not be.
 */
abstract class TypeAnalyzer<T: TypeAnalysis<*>, S: Serializer<*>>(val prism: Prism<S>, val type: ConcreteTypeMirror) {
    protected abstract fun createState(): T

    private val statePool = ConcurrentLinkedQueue<T>()

    /**
     * Adds a [TypeAnalysis] state object back to the pool to be returned by a later call to [getState].
     */
    fun releaseState(state: T) {
        state.clear()
        statePool.add(state)
    }

    /**
     * Gets a pooled [TypeAnalysis] state object. The returned object should be passed to [releaseState] when the
     * (de)serialization operation has been completed.
     */
    fun getState(): T {
        return (statePool.poll() ?: createState()).also { it.clear() }
    }
}

/**
 * TypeAnalysis objects are used to allow multi-threaded and recursive use of [type analyzers][TypeAnalyzer]. Since the
 * type analyzer might be used recursively, the analyzer itself can't store the intermediate state for a
 * (de)serialization operation. Because of this, type analyzers provide pooled type analysis objects, each of which
 * stores the intermediate state required for their operation.
 */
abstract class TypeAnalysis<T: Any> {
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