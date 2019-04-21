package com.teamwizardry.prism

import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.ConcreteTypeMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.internal.identityMapOf
import com.teamwizardry.prism.internal.unmodifiableView

class Prism<T: Serializer<*>> {
    private val _serializers = mutableListOf<T>()
    val serializers: List<T> = _serializers.unmodifiableView()

    private val _factories = mutableListOf<SerializerFactory<T>>()
    val factories: List<SerializerFactory<T>> = _factories.unmodifiableView()

    private val _cache = mutableMapOf<TypeMirror, Lazy<T>>()

    fun get(mirror: TypeMirror): Lazy<T> {
        if(!isFullyConcrete(mirror))
            throw InvalidTypeException("$mirror isn't fully concrete")
        mirror as ConcreteTypeMirror

        _cache[mirror]?.also {
            return it
        }

        val serializer = _serializers.fold<T, T?>(null) { acc, serializer ->
            if (serializer.type.isAssignableFrom(mirror) &&
                (acc == null || acc.type.isAssignableFrom(serializer.type))
            ) {
                serializer
            } else {
                acc
            }
        }
        if(serializer != null) {
            return lazyOf(serializer).also { _cache[mirror] = it }
        }

        val factory = _factories.fold<SerializerFactory<T>, SerializerFactory<T>?>(null) { acc, factory ->
            if (factory.pattern.isAssignableFrom(mirror) && (acc == null || acc.pattern.isAssignableFrom(factory.pattern))
            ) {
                factory
            } else {
                acc
            }
        } ?: throw SerializerNotFoundException("Could not find a serializer for $mirror")

        val lazy = lazy { factory.create(this, mirror) }
        _cache[mirror] = lazy
        return lazy
    }

    fun register(factory: SerializerFactory<T>) {
        _factories.removeIf { it === factory }
        _factories.add(factory)
    }

    fun register(serializer: T) {
        _serializers.removeIf { it === serializer }
        _serializers.add(serializer)
    }

    companion object {
        private val concreteCache = identityMapOf<TypeMirror, Boolean>()

        internal fun isFullyConcrete(mirror: TypeMirror): Boolean {
            return concreteCache.getOrPut(mirror) { computeIsFullyConcrete(mirror) }
        }

        //TODO tests
        // this is incorrect if a generic method's type arguments aren't used in the enclosed type. It will think that
        // that type isn't concrete.
        private fun computeIsFullyConcrete(mirror: TypeMirror): Boolean {
            when(mirror) {
                is ArrayMirror -> {
                    val component = mirror.component as? ConcreteTypeMirror ?: return false
                    return isFullyConcrete(component)
                }
                is ClassMirror -> {
                    if (mirror.typeParameters.any { !isFullyConcrete(it) }) return false
                    if (mirror.enclosingClass?.let { isFullyConcrete(it) } == false) return false
                    mirror.enclosingExecutable?.also { enclosingExecutable ->
                        if (enclosingExecutable.typeParameters.any { !isFullyConcrete(it) }) return false
                        if (!isFullyConcrete(enclosingExecutable.enclosingClass)) return false
                    }
                    return true
                }
                else -> return false
            }
        }
    }
}