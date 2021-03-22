package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeReader
import dev.thecodewarrior.prism.TypeWriter

public interface ArrayWriter<T, S: Serializer<*>>: TypeWriter<Array<T>> {
    /**
     * The array element serializer
     */
    public val serializer: S

    /**
     * The length of the array
     */
    public val size: Int

    /**
     * Get the value at the specified index in the array
     */
    public fun get(index: Int): T
}