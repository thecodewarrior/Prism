package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.type.ConcreteTypeMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.WildcardMirror
import dev.thecodewarrior.prism.internal.unmodifiableView
import java.lang.IllegalArgumentException

public class Prism<T: Serializer<*>> {
    private val _serializers = mutableMapOf<TypeMirror, Lazy<T>>()
    public val serializers: Map<TypeMirror, Lazy<T>> = _serializers.unmodifiableView()

    private val _factories = mutableListOf<SerializerFactory<T>>()
    public val factories: List<SerializerFactory<T>> = _factories.unmodifiableView()

    public operator fun get(mirror: TypeMirror): Lazy<T> {
        @Suppress("NAME_SHADOWING")
        val mirror =
            if(mirror is WildcardMirror) { // todo: VariableMirror isn't valid either
                mirror.upperBound ?: throw InvalidTypeException("Wildcard $mirror can't be serialized since it has no upper bound")
            } else {
                mirror
            }

        _serializers[mirror]?.also {
            return it
        }
        val unannotated = mirror.withTypeAnnotations(listOf())
        _serializers[unannotated]?.also {
            _serializers[mirror] = it
            return it
        }

        mirror as ConcreteTypeMirror

        val factory = _factories.fold<SerializerFactory<T>, SerializerFactory<T>?>(null) { acc, factory ->
            val applicable = factory.pattern.isAssignableFrom(mirror) && factory.predicate?.invoke(mirror) != false
            if (applicable) {
                val moreSpecific = acc == null || acc.pattern.specificity <= factory.pattern.specificity ||
                    (acc.predicate == null && factory.predicate != null && acc.pattern.specificity.compareTo(factory.pattern.specificity) == 0)
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