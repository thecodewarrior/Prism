package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeReader
import dev.thecodewarrior.prism.TypeWriter

public interface MapWriter<K, V, S: Serializer<*>>: TypeWriter<Map<K, V>> {
    /**
     * The key serializer
     */
    public val keySerializer: S

    /**
     * The value serializer
     */
    public val valueSerializer: S

    /**
     * The keys of the array
     */
    public val keys: Set<K>

    /**
     * Get the value for the specified key. Throws if the key doesn't exist.
     */
    public fun get(key: K): V
}