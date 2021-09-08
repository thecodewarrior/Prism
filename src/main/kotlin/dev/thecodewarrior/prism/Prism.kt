package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.type.ConcreteTypeMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.TypeSpecificityComparator
import dev.thecodewarrior.mirror.type.WildcardMirror
import dev.thecodewarrior.prism.internal.unmodifiableView
import java.lang.IllegalArgumentException

public class Prism<T: Serializer<*>> {
    private val _serializers = mutableMapOf<TypeMirror, Lazy<T>>()
    public val serializers: Map<TypeMirror, Lazy<T>> = _serializers.unmodifiableView()

    private val _factories = mutableListOf<SerializerFactory<T>>()
    public val factories: List<SerializerFactory<T>> = _factories.unmodifiableView()

    public operator fun get(type: TypeMirror): Lazy<T> {
        var mirror = type
        if(mirror is WildcardMirror) {
            mirror = mirror.upperBound
                ?: throw InvalidTypeException("Wildcard `$mirror` can't be serialized since it has no upper bound")

            if(mirror !is ConcreteTypeMirror) {
                throw InvalidTypeException(
                    "${mirror.javaClass.simpleName} `$mirror` (the extracted upper bound from `$type`) can't " +
                            "be serialized. Only concrete types (or type variables specialized to be concrete types) " +
                            "can be serialized"
                )
            }
        }
        if(mirror !is ConcreteTypeMirror) {
            throw InvalidTypeException(
                "${mirror.javaClass.simpleName} `$mirror` can't be serialized. Only concrete types " +
                        "(or type variables specialized to be concrete types) can be serialized"
            )
        }

        _serializers[mirror]?.also {
            return it
        }
        val unannotated = mirror.withTypeAnnotations(listOf())
        _serializers[unannotated]?.also {
            _serializers[mirror] = it
            return it
        }

        val factory = _factories.fold<SerializerFactory<T>, SerializerFactory<T>?>(null) { acc, factory ->
            val applicable = factory.pattern.isAssignableFrom(mirror) && factory.predicate?.invoke(mirror) != false
            if (applicable) {
                val moreSpecific = acc == null || TypeSpecificityComparator.compare(acc.pattern, factory.pattern) <= 0 ||
                    (acc.predicate == null && factory.predicate != null && TypeSpecificityComparator.compare(acc.pattern, factory.pattern) == 0)
                if (moreSpecific)
                    factory
                else
                    acc
            } else {
                acc
            }
        }
            ?: throw SerializerNotFoundException("Could not find a serializer or factory for $mirror")

        val lazy = lazy { factory.create(mirror) }
        _serializers[mirror] = lazy
        lazy.value
        return lazy
    }

    public fun register(vararg factories: SerializerFactory<T>): Prism<T> {
        factories.forEach { factory ->
            _factories.removeIf { it === factory }
            _factories.add(factory)
        }
        return this
    }

    public fun register(vararg serializers: T): Prism<T> {
        serializers.forEach { serializer ->
            if(serializer.type in _serializers)
                throw IllegalArgumentException("Duplicate serializer for ${serializer.type}")
            _serializers[serializer.type] = lazyOf(serializer)
        }
        return this
    }
}