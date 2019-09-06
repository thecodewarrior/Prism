package dev.thecodewarrior.prism.formats.netty.serializers.builtin.generics

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.InvalidTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.SerializerFactory
import dev.thecodewarrior.prism.SerializerNotFoundException
import dev.thecodewarrior.prism.formats.netty.NettySerializer
import dev.thecodewarrior.prism.formats.netty.readBooleanArray
import dev.thecodewarrior.prism.formats.netty.writeBooleanArray
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
                return creationStyle.createList(buf, existing, syncing)
            } else {
                mutateList(buf, existing as MutableList<Any?>, syncing)
                return existing
            }
        }

        fun mutateList(buf: ByteBuf, existing: MutableList<Any?>, syncing: Boolean) {
            val nulls = buf.readBooleanArray()

            while (existing.size > nulls.size)
                existing.removeAt(existing.lastIndex)

            for (i in 0 until nulls.size) {
                val v = if (nulls[i]) null else componentSerializer.read(buf, null, syncing)
                if (i >= existing.size) {
                    existing.add(v)
                } else {
                    existing[i] = v
                }
            }
        }

        override fun serialize(buf: ByteBuf, value: List<Any?>, syncing: Boolean) {
            val nulls = BooleanArray(value.size) { value[it] == null }
            buf.writeBooleanArray(nulls)

            (0 until value.size).asSequence()
                    .filterNot { nulls[it] } // don't check == null again to avoid race conditions
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

        val creationStyle: CreationStyle

        init {
            val collection = Mirror.reflectClass<Collection<*>>().withTypeArguments(component)
            val createWithCollection = mirror.declaredConstructors.find {
                it.parameters.size == 1 && collection.isAssignableFrom(it.parameters[0].type)
            }
            val createWithArray = mirror.declaredConstructors.find {
                it.parameters.size == 1 && it.parameters[0].type is ArrayMirror &&
                    it.parameters[0].type.isAssignableFrom(Mirror.createArrayType(it.parameters[0].type))
            }
            val zeroArg = mirror.declaredConstructors.find { it.parameters.isEmpty() }
            creationStyle = when {
                zeroArg != null -> CreateAddStyle(zeroArg)
                createWithCollection != null -> CreateWithCollectionStyle(createWithCollection)
                createWithArray != null -> CreateWithArrayStyle(createWithArray)
                else -> throw InvalidTypeException("Couldn't find constructor")
            }
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
