package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeReader

public interface ListReader<T, S: Serializer<*>>: TypeReader<List<T>> {
    /**
     * The array element serializer
     */
    public val serializer: S

    /**
     * Ensures the buffer can contain at least [length] elements, potentially increasing efficiency. This can be
     * used in cases where the element count is known before deserializing.
     */
    public fun reserve(length: Int)

    /**
     * Pads the buffer with nulls up until [length] elements, efficiently ensuring the length ahead of time. This
     * can be used in cases where the element count is known before deserializing, and *must* be used if elements
     * are going to be populated using [set].
     */
    public fun padToLength(length: Int)

    /**
     * Directly sets the value in the buffer. This *must* be used in combination with [padToLength] to ensure the
     * list's length is sufficient.
     *
     * @throws IndexOutOfBoundsException if the index is negative or beyond the capacity set using [padToLength]
     */
    public fun set(index: Int, value: T)

    /**
     * Add a value to the buffer
     */
    public fun add(value: T)
}