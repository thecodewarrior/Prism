package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeReader

public interface MapReader<K, V, S: Serializer<*>>: TypeReader<Map<K, V>> {
    /**
     * The key serializer
     */
    public val keySerializer: S

    /**
     * The value serializer
     */
    public val valueSerializer: S

    /**
     * Adds an entry to this map
     */
    public fun put(key: K, value: V)
}