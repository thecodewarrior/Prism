package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.*
import dev.thecodewarrior.prism.internal.unmodifiableView

public class MapAnalyzer<K, V, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror)
    : TypeAnalyzer<Map<K, V>, MapReader<K, V, S>, MapWriter<K, V, S>, S>(prism, type) {

    public val mapType: ClassMirror = type.findSuperclass(Map::class.java)?.asClassMirror()
        ?: throw IllegalTypeException()
    public val keyType: TypeMirror = mapType.typeParameters[0]
    public val valueType: TypeMirror = mapType.typeParameters[1]

    private var constructor = type.declaredConstructors.find { it.parameters.isEmpty() }!! // TODO replace with helper

    override fun createReader(): MapReader<K, V, S> = Reader()
    override fun createWriter(): MapWriter<K, V, S> = Writer()

    private inner class Reader: MapReader<K, V, S> {
        override val keySerializer: S by prism[keyType]
        override val valueSerializer: S by prism[valueType]

        private var map: MutableMap<K, V>? = null

        override fun start() {
            map = constructor.call()
        }

        override fun put(key: K, value: V) {
            map!![key] = value
        }

        override fun release() {
            map = null
            release(this)
        }

        override fun build(): MutableMap<K, V> {
            return map!!
        }
    }

    private inner class Writer: MapWriter<K, V, S> {
        override val keySerializer: S by prism[keyType]
        override val valueSerializer: S by prism[valueType]
        private var entries: Map<K, V> = emptyMap()
        override val keys: Set<K>
            get() = entries.keys

        override fun get(key: K): V {
            val value = entries[key]
            if(value == null && key !in entries) {
                throw SerializationException("Attempted to access non-existent key '$key' for type $type")
            }
            @Suppress("UNCHECKED_CAST")
            return value as V
        }

        override fun load(value: Map<K, V>) {
            entries = value
        }

        override fun release() {
            entries = emptyMap()
            release(this)
        }
    }
}