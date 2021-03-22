package dev.thecodewarrior.prism.testsupport

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.SerializerFactory

class IdentitySerializer(type: TypeMirror): Serializer<Any>(type) {}

/**
 * A no-op prism
 */
val IdentityPrism = Prism<IdentitySerializer>().also { prism ->
    prism.register(object: SerializerFactory<IdentitySerializer>(prism, Mirror.types.any) {
        override fun create(mirror: TypeMirror): IdentitySerializer = IdentitySerializer(mirror)
    })
    prism.register(IdentitySerializer(Mirror.types.char))
    prism.register(IdentitySerializer(Mirror.types.boolean))
    prism.register(IdentitySerializer(Mirror.types.byte))
    prism.register(IdentitySerializer(Mirror.types.short))
    prism.register(IdentitySerializer(Mirror.types.int))
    prism.register(IdentitySerializer(Mirror.types.long))
    prism.register(IdentitySerializer(Mirror.types.float))
    prism.register(IdentitySerializer(Mirror.types.double))
}
