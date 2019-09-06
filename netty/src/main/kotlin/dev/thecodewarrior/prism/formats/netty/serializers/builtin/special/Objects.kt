package dev.thecodewarrior.prism.formats.netty.serializers.builtin.special

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.InvalidTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.SerializerFactory
import dev.thecodewarrior.prism.formats.netty.NettySerializer
import dev.thecodewarrior.prism.formats.netty.readBooleanArray
import dev.thecodewarrior.prism.formats.netty.writeBooleanArray
import io.netty.buffer.ByteBuf
import java.lang.reflect.Constructor

/**
 * Created by TheCodeWarrior
 */
object ObjectSerializerFactory : SerializerFactory<NettySerializer<*>>(Mirror.reflect<Set<Any?>>()) {
    override fun create(prism: Prism<NettySerializer<*>>, mirror: TypeMirror): NettySerializer<*> {
        return ObjectSerializer(prism, mirror as ClassMirror, SerializerAnalysis(mirror))
    }

    private class ObjectSerializer(prism: Prism<NettySerializer<*>>, type: ClassMirror, val analysis: SerializerAnalysis) : NettySerializer<Set<Any?>>(type) {
        override fun deserialize(buf: ByteBuf, existing: Set<Any?>?, syncing: Boolean): Set<Any?> {
            val nullsig = buf.readBooleanArray()
            val nulliter = nullsig.iterator()
            if (analysis.mutable && (existing != null || analysis.constructor.parameters.isEmpty())) {
                val instance = existing ?: analysis.constructorMH(arrayOf())
                readFields(analysis.alwaysFields, buf, instance, nulliter, syncing)
                if (!syncing) {
                    readFields(analysis.noSyncFields, buf, instance, nulliter, syncing)
                } else {
                    readFields(analysis.nonPersistentFields, buf, instance, nulliter, syncing)
                }
                return instance
            } else {
                val map = mutableMapOf<String, Any?>()

                analysis.alwaysFields.forEach {
                    try {
                        if (!nulliter.next()) {
                            map[it.key] = analysis.serializers[it.key]!!.value.read(buf, null, syncing)
                        }
                    } catch (e: Throwable) {
                        throw SerializerException("Error reading field ${it.value.name} from bytes", e)
                    }
                }
                if (!syncing) {
                    analysis.noSyncFields.forEach {
                        try {
                            if (!nulliter.next()) {
                                map[it.key] = analysis.serializers[it.key]!!.value.read(buf, null, syncing)
                            }
                        } catch (e: Throwable) {
                            throw SerializerException("Error reading field ${it.value.name} from bytes", e)
                        }
                    }
                } else {
                    analysis.nonPersistentFields.forEach {
                        try {
                            if (!nulliter.next()) {
                                map[it.key] = analysis.serializers[it.key]!!.value.read(buf, null, syncing)
                            }
                        } catch (e: Throwable) {
                            throw SerializerException("Error reading field ${it.value.name} from bytes", e)
                        }
                    }
                }
                try {
                    return analysis.constructorMH(analysis.constructorArgOrder.map {
                        map[it]
                    }.toTypedArray())
                } catch (e: Throwable) {
                    throw SerializerException("Error creating instance of type $type", e)
                }
            }
        }

        private fun readFields(map: Map<String, FieldMirror>, buf: ByteBuf, instance: Any, nullsig: BooleanIterator, sync: Boolean) {
            map.forEach {
                try {
                    val oldValue = it.value.getter(instance)
                    val value = if (nullsig.next()) {
                        null
                    } else {
                        analysis.serializers[it.key]!!.value.read(buf, oldValue, sync)
                    }
                    if (it.value.meta.hasFlag(SavingFieldFlag.FINAL)) {
                        if (oldValue !== value) {
                            throw SerializerException("Cannot set final field to new value. Either make the field " +
                                    "mutable or modify the serializer to change the existing object instead of " +
                                    "creating a new one.")
                        }
                    } else {
                        it.value.setter(instance, value)
                    }
                } catch (e: Throwable) {
                    throw SerializerException("Error reading field ${it.value.name} from bytes", e)
                }
            }

        }

        override fun serialize(buf: ByteBuf, value: Set<Any?>, syncing: Boolean) {
            var nullsig = mutableListOf<Boolean>()
            analysis.alwaysFields.forEach {
                try {
                    nullsig.add(it.value.getter(value) == null)
                } catch (e: Throwable) {
                    throw SerializerException("Error getting field ${it.value.name} for nullsig", e)
                }
            }
            if (!syncing) {
                analysis.noSyncFields.forEach {
                    try {
                        nullsig.add(it.value.getter(value) == null)
                    } catch (e: Throwable) {
                        throw SerializerException("Error getting field ${it.value.name} for nullsig", e)
                    }
                }
            } else {
                analysis.nonPersistentFields.forEach {
                    try {
                        nullsig.add(it.value.getter(value) == null)
                    } catch (e: Throwable) {
                        throw SerializerException("Error getting field ${it.value.name} for nullsig", e)
                    }
                }
            }

            buf.writeBooleanArray(nullsig.toTypedArray().toBooleanArray())

            analysis.alwaysFields.forEach {
                try {
                    val fieldValue = it.value.getter(value)
                    if (fieldValue != null)
                        analysis.serializers[it.key]!!.value.write(buf, fieldValue, syncing)
                } catch (e: Throwable) {
                    throw SerializerException("Error writing field ${it.value.name} to bytes", e)
                }
            }

            if (!syncing) {
                analysis.noSyncFields.forEach {
                    try {
                        val fieldValue = it.value.getter(value)
                        if (fieldValue != null)
                            analysis.serializers[it.key]!!.value.write(buf, fieldValue, syncing)
                    } catch (e: Throwable) {
                        throw SerializerException("Error writing field ${it.value.name} to bytes", e)
                    }
                }
            } else {
                analysis.nonPersistentFields.forEach {
                    try {
                        val fieldValue = it.value.getter(value)
                        if (fieldValue != null)
                            analysis.serializers[it.key]!!.value.write(buf, fieldValue, syncing)
                    } catch (e: Throwable) {
                        throw SerializerException("Error writing field ${it.value.name} to bytes", e)
                    }
                }
            }
        }
    }
}


class SerializerAnalysis(prism: Prism<NettySerializer<*>>, val type: ClassMirror) {
    val fields: Map<String, FieldMirror>

    val mutable: Boolean

    val constructor: ConstructorMirror
    val constructorArgOrder: List<String>
    val serializers: Map<String, Lazy<NettySerializer<*>>>

    init {
        val allFields = mutableMapOf<String, FieldMirror>()
        this.fields =
            allFields.filter { (_, field) -> !field.isTransient }
        this.mutable = fields.none { it.value.isFinal }

        constructor =
            type.declaredConstructors.find {
                val paramsToFind = HashMap(fields)
                if(it.parameters.any { it.name == null }) return@find false
                var i = 0
                it.parameters.all {
                    val ret =
                        paramsToFind.remove(it.name)?.type == it.type
                    i++
                    ret
                }
            } ?:
                type.declaredConstructor() ?:
                throw InvalidTypeException("Couldn't find zero-argument constructor or constructor with parameters (${fields.map { it.value.meta.type.toString() + " " + it.key }.joinToString(", ")}) for immutable type ${type.clazz.canonicalName}")
        constructorArgOrder = constructor.parameters.map { it.name!! }

        serializers = fields.mapValues { prism.get(it.value.type) }
    }
}
