package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.type.ConcreteTypeMirror
import dev.thecodewarrior.prism.base.analysis.AnalysisLog
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Type analyzers are the backend for many serializers, providing a format-agnostic way to implement the complex
 * process of extracting serializable data from an object and later assembling that data back together into a new
 * object.
 *
 * Type analyzers MUST be thread safe, however the [TypeReader] and [TypeWriter] objects they return might not be.
 */
public abstract class TypeAnalyzer<T: Any, R: TypeReader<T>, W: TypeWriter<T>, S: Serializer<*>>(
    public val prism: Prism<S>,
    public val type: ConcreteTypeMirror
) {
    protected val log: AnalysisLog = AnalysisLog()

    protected abstract fun createReader(): R
    protected abstract fun createWriter(): W

    private val readerPool = ConcurrentLinkedQueue<R>()
    private val writerPool = ConcurrentLinkedQueue<W>()

    public fun getReader(): R {
        return readerPool.poll() ?: createReader()
    }

    public fun getWriter(value: T): W {
        val writer = writerPool.poll() ?: createWriter()
        writer.load(value)
        return writer
    }

    public fun release(reader: R) {
        readerPool.add(reader)
    }
    public fun release(writer: W) {
        writerPool.add(writer)
    }
}

/**
 * TypeReaders manage the process of assembling data into final objects. Type readers should have all the information
 * needed to deserialize the data available. e.g. the user shouldn't have to call out to the type analyzer itself to
 * get a list's element serializer, that should be present in the type reader.
 */
public interface TypeReader<T: Any>: AutoCloseable {
    /**
     * Build a new object from the read data. Calling this multiple times is considered a logic error and may throw an
     * error or return corrupt/invalid data.
     */
    public fun build(): T

    /**
     * Clears this reader's state and returns it to the [TypeAnalyzer]'s pool
     */
    public fun release()

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
public interface TypeWriter<T: Any>: AutoCloseable {
    /**
     * Load a value in preparation for serialization
     */
    public fun load(value: T) {}

    /**
     * Clears the writer's state and returns it to the [TypeAnalyzer]'s pool
     */
    public fun release()

    @JvmDefault
    override fun close() {
        release()
    }
}
