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

public class ListAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror)
    : TypeAnalyzer<MutableList<T>, ListAnalyzer<T, S>.Reader, ListAnalyzer<T, S>.Writer, S>(prism, type) {
    public val listType: ClassMirror = type.findSuperclass(List::class.java)?.asClassMirror()
        ?: throw IllegalTypeException()
    public val elementType: TypeMirror = listType.typeParameters[0]

    private var constructor = type.declaredConstructors.find { it.parameters.isEmpty() }!! // TODO replace with helper

    override fun createReader(): Reader = Reader()
    override fun createWriter(): Writer = Writer()

    public inner class Reader: TypeReader<MutableList<T>> {
        public val serializer: S by prism[elementType]
        private var buffer = ArrayList<T?>()
        private var existing: MutableList<T>? = null

        /**
         * Ensures the buffer can contain at least [length] elements, potentially increasing efficiency. This can be
         * used in cases where the element count is known before deserializing.
         */
        public fun reserve(length: Int) {
            buffer.ensureCapacity(length)
        }

        /**
         * Pads the buffer with nulls up until [length] elements, efficiently ensuring the capacity ahead of time. This
         * can be used in cases where the element count is known before deserializing, and *must* be used if elements
         * are going to be populated using [set].
         */
        public fun padToLength(length: Int) {
            while(buffer.size < length)
                buffer.add(null)
        }

        /**
         * Sets the value in the buffer, padding with nulls if necessary in order to reach the given index. This *must*
         * be used in combination with [padToLength] in order to avoid accidentally allocating enormous lists due to
         * corrupt indices.
         *
         * @throws IndexOutOfBoundsException if the index is negative or beyond the capacity set using [padToLength]
         */
        public fun set(index: Int, value: T) {
            buffer[index] = value
        }

        public fun add(value: T) {
            buffer.add(value)
        }

        override fun load(existing: MutableList<T>?) {
            this.existing = existing
        }

        override fun release() {
            existing = null
            if(buffer.size > 500) {
                buffer = ArrayList()
            } else {
                buffer.clear()
            }
            release(this)
        }

        override fun apply(): MutableList<T> {
            val existing = existing
            val value: MutableList<T>
            if(existing != null) {
                value = existing
                value.clear()
            } else {
                value = constructor.call()
            }

            @Suppress("UNCHECKED_CAST")
            value.addAll(buffer as List<T>)

            return value
        }
    }

    public inner class Writer: TypeWriter<MutableList<T>> {
        public val serializer: S by prism[elementType]
        public var elements: List<T> = emptyList()
            private set

        override fun load(value: MutableList<T>) {
            elements = value
        }

        override fun release() {
            elements = emptyList()
            release(this)
        }
    }
}