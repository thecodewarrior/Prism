package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.type.ConcreteTypeMirror
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Type analyzers are the backend for many serializers, providing a format-agnostic way to implement the complex
 * process of extracting serializable data from an object and later integrating that data back in, either by modifying
 * an existing object or creating a new one.
 *
 * A type analyzer analyzes types and objects to determine the best way to get and set data. This often includes
 * decisions such as whether a new instance of the object is needed, and then if so what constructor to use. Some
 * serializers will change how they set data based on that data, such as not instantiating new objects if only mutable
 * fields were modified.
 *
 * Well-built analyzers should be thread safe, however the [TypeReader] and [TypeWriter] objects they return may not be.
 */
abstract class TypeAnalyzer<T: Any, R: TypeReader<T>, W: TypeWriter<T>, S: Serializer<*>>(val prism: Prism<S>, val type: ConcreteTypeMirror) {
    protected abstract fun createReader(): R
    protected abstract fun createWriter(): W

    private val readerPool = ConcurrentLinkedQueue<R>()
    private val writerPool = ConcurrentLinkedQueue<W>()

    fun getReader(existing: T?): R {
        val reader = readerPool.poll() ?: createReader()
        reader.load(existing)
        return reader
    }

    fun getWriter(value: T): W {
        val writer = writerPool.poll() ?: createWriter()
        writer.load(value)
        return writer
    }

    fun release(reader: R) {
        readerPool.add(reader)
    }
    fun release(writer: W) {
        writerPool.add(writer)
    }
}

/**
 * TypeReaders manage the process of assembling data into final objects, potentially integrating the data into existing
 * objects as opposed to creating new ones. Type readers should ideally have all the information needed to deserialize
 * the data available. e.g. the user shouldn't have to call out to the type analyzer itself to get a list's element
 * serializer, that should be present in the type reader.
 */
interface TypeReader<T: Any>: AutoCloseable {
    /**
     * Load an existing value in preparation for deserialization
     */
    fun load(existing: T?)

    /**
     * Apply the read data, producing a new object or mutating and returning the existing object
     */
    fun apply(): T

    /**
     * Clears this reader's state and returns it to the [TypeAnalyzer]'s pool
     */
    fun release()

    @JvmDefault
    override fun close() {
        release()
    }
}

/**
 * TypeWriters manage the process of extracting meaningful data from objects of their respective type. Type writers
 * should ideally have all the information needed to serialize the data available. e.g. the user shouldn't have to call
 * out to the type analyzer itself to get a list's element serializer, that should be present in the type writer.
 */
interface TypeWriter<T: Any>: AutoCloseable {
    /**
     * Load a value in preparation for serialization
     */
    fun load(value: T) {}

    /**
     * Clears the writer's state and returns it to the [TypeAnalyzer]'s pool
     */
    fun release()

    @JvmDefault
    override fun close() {
        release()
    }
}
