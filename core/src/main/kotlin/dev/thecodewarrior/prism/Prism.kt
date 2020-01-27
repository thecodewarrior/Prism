package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.type.ConcreteTypeMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.WildcardMirror
import dev.thecodewarrior.prism.internal.unmodifiableView

class Prism<T: Serializer<*>> {
    private val _serializers = mutableListOf<T>()
    val serializers: List<T> = _serializers.unmodifiableView()

    private val _factories = mutableListOf<SerializerFactory<T>>()
    val factories: List<SerializerFactory<T>> = _factories.unmodifiableView()

    private val _cache = mutableMapOf<TypeMirror, Lazy<T>>()

    operator fun get(mirror: TypeMirror): Lazy<T> {
        @Suppress("NAME_SHADOWING")
        val mirror =
            if(mirror is WildcardMirror) {
                mirror.upperBound ?: throw InvalidTypeException("Wildcard $mirror can't be serialized since it has no upper bound")
            } else {
                mirror
            }

        _cache[mirror]?.also {
            return it
        }

        mirror as ConcreteTypeMirror

        val serializer = _serializers.fold<T, T?>(null) { acc, serializer ->
            if (serializer.type.isAssignableFrom(mirror) &&
                (acc == null || acc.type.specificity <= serializer.type.specificity)
            ) {
                serializer
            } else {
                acc
            }
        }

        val factory = _factories.fold<SerializerFactory<T>, SerializerFactory<T>?>(null) { acc, factory ->
            val applicable = factory.pattern.isAssignableFrom(mirror) && factory.predicate?.invoke(mirror) != false
            if (applicable) {
                val moreSpecific = acc == null || acc.pattern.specificity <= factory.pattern.specificity ||
                    (acc.predicate == null && factory.predicate != null && acc.pattern.specificity.compareTo(factory.pattern.specificity) == 0)
                if(moreSpecific)
                    factory
                else
                    acc
            } else {
                acc
            }
        }

        if(serializer != null) {
            if(factory == null || factory.pattern.specificity <= serializer.type.specificity)
                return lazyOf(serializer).also { _cache[mirror] = it }
        }
        if(factory == null)
            throw SerializerNotFoundException("Could not find a serializer or factory for $mirror")

        val lazy = lazy { factory.create(mirror) }
        _cache[mirror] = lazy
        return lazy
    }

    fun register(vararg factories: SerializerFactory<T>): Prism<T> {
        factories.forEach { factory ->
            _factories.removeIf { it === factory }
            _factories.add(factory)
        }
        return this
    }

    fun register(vararg serializers: T): Prism<T> {
        serializers.forEach { serializer ->
            _serializers.removeIf { it === serializer }
            _serializers.add(serializer)
        }
        return this
    }
}