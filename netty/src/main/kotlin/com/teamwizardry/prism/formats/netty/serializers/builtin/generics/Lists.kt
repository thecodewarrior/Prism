package com.teamwizardry.prism.formats.netty.serializers.builtin.generics

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.InvalidTypeException
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.SerializerFactory
import com.teamwizardry.prism.formats.netty.NettySerializer
import com.teamwizardry.prism.formats.netty.readBooleanArray
import com.teamwizardry.prism.formats.netty.writeBooleanArray
import io.netty.buffer.ByteBuf
import java.util.Collections

/**
 * Created by TheCodeWarrior
 */
object ListSerializerFactory : SerializerFactory<NettySerializer<*>>(Mirror.reflect<List<Any?>>()) {
    override fun create(prism: Prism<NettySerializer<*>>, mirror: TypeMirror): NettySerializer<*> {
        return ListSerializer(prism, mirror as ClassMirror)
    }

    private class ListSerializer(prism: Prism<NettySerializer<*>>, type: ClassMirror) : NettySerializer<List<Any?>>(type) {
        val mirror = type

        val listType = mirror.findSuperclass(List::class.java) ?: throw InvalidTypeException()
        val component = listType.typeParameters[0]
        val componentSerializer by prism.get(component)

        override fun deserialize(buf: ByteBuf, existing: List<Any?>?, syncing: Boolean): List<Any?> {
            if(existing == null) {
                return createList(buf, existing, syncing)
            } else {
                return mutateList(buf, existing, syncing)
            }
        }

        override fun mutateList(buf: ByteBuf, existing: List<Any?>?, syncing: Boolean): List<Any?> {
            val nulls = buf.readBooleanArray()

            while (array.size > nullsig.size)
                array.removeAt(array.size - 1)

            for (i in 0..nullsig.size - 1) {
                val v = if (nullsig[i]) null else serGeneric.read(buf, array.getOrNull(i), syncing)
                if (i >= array.size) {
                    array.add(v)
                } else {
                    array[i] = v
                }
            }
        }

        override fun serialize(buf: ByteBuf, value: List<Any?>, syncing: Boolean) {
            val nullsig = BooleanArray(value.size) { value[it] == null }
            buf.writeBooleanArray(nullsig)

            (0 until value.size)
                    .filterNot { nullsig[it] }
                    .forEach { componentSerializer.write(buf, value[it]!!, syncing) }
        }

//        private fun createConstructorMH(): () -> MutableList<Any?> {
//            if (type.clazz == List::class.java) {
//                return { mutableListOf<Any?>() }
//            } else {
//                try {
//                    val mh = MethodHandleHelper.wrapperForConstructor<MutableList<Any?>>(type.clazz)
//                    return { mh(arrayOf()) }
//                } catch(e: ReflectionHelper.UnableToFindMethodException) {
//                    return { throw UnsupportedOperationException("Could not find zero-argument constructor for " +
//                            type.clazz.simpleName, e) }
//                }
//            }
//        }

        init {
            val collection = Mirror.reflectClass<Collection<*>>().specialize(component)
            val createWithCollection = mirror.declaredConstructors.find {
                it.parameters.size == 1 && collection.isAssignableFrom(it.parameters[0].type)
            }
            val createWithArray = mirror.declaredConstructors.find {
                it.parameters.size == 1 && it.parameters[0].type is ArrayMirror &&
                    it.parameters[0].type.isAssignableFrom(Mirror.createArrayType(it.parameters[0].type, 1))
            }
            val zeroArg = mirror.declaredConstructors.find { it.parameters.isEmpty() }
        }

        companion object {
            val unmodifiableTypes = Collections::class.java.declaredClasses.toSet()
        }

        private abstract class CreationStyle {
            abstract fun createList(buf: ByteBuf, existing: List<Any?>?, syncing: Boolean): List<Any?>
        }

        inner class CreateAddStyle(val constructor: ConstructorMirror): CreationStyle() {
            override fun createList(buf: ByteBuf, existing: List<Any?>?, syncing: Boolean): List<Any?> {
                val nulls = buf.readBooleanArray()
                val list = constructor.call<MutableList<Any?>>()

                for(isNull in nulls) {
                    if(isNull)
                        list.add(null)
                    else
                        list.add(componentSerializer.read(buf, null, syncing))
                }

                return list
            }
        }

        inner class CreateWithCollectionStyle(val constructor: ConstructorMirror): CreationStyle() {
            override fun createList(buf: ByteBuf, existing: List<Any?>?, syncing: Boolean): List<Any?> {
                val nulls = buf.readBooleanArray()
                val list = mutableListOf<Any?>()

                for(isNull in nulls) {
                    if(isNull)
                        list.add(null)
                    else
                        list.add(componentSerializer.read(buf, null, syncing))
                }
                return constructor.call(list)
            }
        }

        inner class CreateWithArrayStyle(val constructor: ConstructorMirror): CreationStyle() {
            override fun createList(buf: ByteBuf, existing: List<Any?>?, syncing: Boolean): List<Any?> {
                val nulls = buf.readBooleanArray()
                val list = mutableListOf<Any?>()

                for(isNull in nulls) {
                    if(isNull)
                        list.add(null)
                    else
                        list.add(componentSerializer.read(buf, null, syncing))
                }
                return constructor.call(list.toTypedArray())
            }
        }
    }
}
