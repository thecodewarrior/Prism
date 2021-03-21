package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.IllegalTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalyzer
import dev.thecodewarrior.prism.TypeReader
import dev.thecodewarrior.prism.TypeWriter
import java.lang.IndexOutOfBoundsException
import java.util.Arrays

public class ArrayAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ArrayMirror)
    : TypeAnalyzer<Array<T>, ArrayReader<T, S>, ArrayWriter<T, S>, S>(prism, type) {

    public val elementType: TypeMirror = type.component

    override fun createReader(): ArrayReader<T, S> = Reader()
    override fun createWriter(): ArrayWriter<T, S> = Writer()

    private inner class Reader: ArrayReader<T, S> {
        override val serializer: S by prism[elementType]
        private var buffer = ArrayList<T?>()

        override fun reserve(length: Int) {
            buffer.ensureCapacity(length)
        }

        override fun padToLength(length: Int) {
            while(buffer.size < length)
                buffer.add(null)
        }

        override fun set(index: Int, value: T) {
            buffer[index] = value
        }

        override fun add(value: T) {
            buffer.add(value)
        }

        override fun release() {
            if(buffer.size > 500) {
                buffer = ArrayList()
            } else {
                buffer.clear()
            }
            release(this)
        }

        override fun build(): Array<T> {
            @Suppress("UNCHECKED_CAST")
            val value: Array<T> = type.asArrayMirror().newInstance(buffer.size) as Array<T>

            buffer.forEachIndexed { index, element ->
                @Suppress("UNCHECKED_CAST")
                value[index] = element as T
            }

            return value
        }
    }

    private inner class Writer: ArrayWriter<T, S> {
        override val serializer: S by prism[elementType]
        private var elements: Array<T>? = null

        override val size: Int
            get() = elements?.size ?: 0

        override fun get(index: Int): T {
            return elements!![index]
        }

        override fun load(value: Array<T>) {
            this.elements = value
        }

        override fun release() {
            elements = null
            release(this)
        }
    }
}