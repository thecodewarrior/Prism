package com.teamwizardry.prism.formats.netty.serializers.builtin.generics

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.InvalidTypeException
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.PrismException
import com.teamwizardry.prism.SerializerFactory
import com.teamwizardry.prism.SerializerNotFoundException
import com.teamwizardry.prism.formats.netty.NettySerializer
import com.teamwizardry.prism.formats.netty.readBooleanArray
import com.teamwizardry.prism.formats.netty.readVarInt
import com.teamwizardry.prism.formats.netty.writeVarInt
import io.netty.buffer.ByteBuf
import java.util.Collections

/**
 * Created by TheCodeWarrior
 */
object SetSerializerFactory : SerializerFactory<NettySerializer<*>>(Mirror.reflect<Set<Any?>>()) {
    override fun create(prism: Prism<NettySerializer<*>>, mirror: TypeMirror): NettySerializer<*> {
        return SetSerializer(prism, mirror as ClassMirror)
    }

    private class SetSerializer(prism: Prism<NettySerializer<*>>, type: ClassMirror) : NettySerializer<Set<Any?>>(type) {
        val mirror = type

        val setType = mirror.findSuperclass(Set::class.java) ?: throw InvalidTypeException()
        val component = setType.typeParameters[0]
        val componentSerializer by prism.get(component)

        override fun deserialize(buf: ByteBuf, existing: Set<Any?>?, syncing: Boolean): Set<Any?> {
            if(existing == null) {
                return creationStyle.createSet(buf, existing, syncing)
            } else {
                mutateSet(buf, existing as MutableSet<Any?>, syncing)
                return existing
            }
        }

        fun mutateSet(buf: ByteBuf, existing: MutableSet<Any?>, syncing: Boolean) {
            val hasNull = buf.readBoolean()
            val count = buf.readVarInt()

            val toRemove = mutableSetOf<Any?>()
            toRemove.addAll(existing)

            if(hasNull) {
                existing.add(null)
                toRemove.remove(null)
            }
            for (i in 0 until count) {
                val v = componentSerializer.read(buf, null, syncing)
                existing.add(v)
                toRemove.remove(v)
            }

            toRemove.forEach {
                existing.remove(it)
            }
        }

        override fun serialize(buf: ByteBuf, value: Set<Any?>, syncing: Boolean) {
            val hasNull = null in value
            buf.writeBoolean(hasNull)

            val count = value.size - if(hasNull) 1 else 0
            buf.writeVarInt(count)
            var writtenCount = 0
            value.forEach {
                if(it == null) return@forEach
                componentSerializer.write(buf, it, syncing)
                writtenCount++
            }
            if(writtenCount != count) {
                throw PrismException("Precomputed ($count) and written ($writtenCount) count mismatch ")
            }
        }

//        private fun createConstructorMH(): () -> MutableSet<Any?> {
//            if (type.clazz == Set::class.java) {
//                return { mutableSetOf<Any?>() }
//            } else {
//                try {
//                    val mh = MethodHandleHelper.wrapperForConstructor<MutableSet<Any?>>(type.clazz)
//                    return { mh(arrayOf()) }
//                } catch(e: ReflectionHelper.UnableToFindMethodException) {
//                    return { throw UnsupportedOperationException("Could not find zero-argument constructor for " +
//                            type.clazz.simpleName, e) }
//                }
//            }
//        }

        val creationStyle: CreationStyle

        init {
            val collection = Mirror.reflectClass<Collection<*>>().withTypeArguments(component)
            val createWithCollection = mirror.declaredConstructors.find {
                it.parameters.size == 1 && collection.isAssignableFrom(it.parameters[0].type)
            }
            val zeroArg = mirror.declaredConstructors.find { it.parameters.isEmpty() }
            creationStyle = when {
                zeroArg != null -> CreateAddStyle(zeroArg)
                createWithCollection != null -> CreateWithCollectionStyle(createWithCollection)
                else -> throw SerializerNotFoundException("Couldn't find constructor")
            }
        }

        companion object {
            val unmodifiableTypes = Collections::class.java.declaredClasses.toSet()
        }

        private abstract class CreationStyle {
            abstract fun createSet(buf: ByteBuf, existing: Set<Any?>?, syncing: Boolean): Set<Any?>
        }

        inner class CreateAddStyle(val constructor: ConstructorMirror): CreationStyle() {
            override fun createSet(buf: ByteBuf, existing: Set<Any?>?, syncing: Boolean): Set<Any?> {
                val nulls = buf.readBooleanArray()
                val set = constructor.call<MutableSet<Any?>>()

                for(isNull in nulls) {
                    if(isNull)
                        set.add(null)
                    else
                        set.add(componentSerializer.read(buf, null, syncing))
                }

                return set
            }
        }

        inner class CreateWithCollectionStyle(val constructor: ConstructorMirror): CreationStyle() {
            override fun createSet(buf: ByteBuf, existing: Set<Any?>?, syncing: Boolean): Set<Any?> {
                val nulls = buf.readBooleanArray()
                val set = mutableSetOf<Any?>()

                for(isNull in nulls) {
                    if(isNull)
                        set.add(null)
                    else
                        set.add(componentSerializer.read(buf, null, syncing))
                }
                return constructor.call(set)
            }
        }
    }
}
