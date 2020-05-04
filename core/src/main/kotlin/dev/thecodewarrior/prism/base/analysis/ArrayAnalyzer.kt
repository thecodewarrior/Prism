package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.IllegalTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalysis
import dev.thecodewarrior.prism.TypeAnalyzer
import java.lang.IndexOutOfBoundsException

class ArrayAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ArrayMirror): TypeAnalyzer<ArrayAnalyzer<T, S>.ArrayAnalysis, S>(prism, type) {
    val elementType: TypeMirror = type.component
    val elementSerializer: S by prism[elementType]

    override fun createState() = ArrayAnalysis()

    inner class ArrayAnalysis: TypeAnalysis<Array<T>>() {
        var buffer = ArrayList<T?>()
            private set

        /**
         * Ensures the buffer can contain at least [length] elements. This can be used in cases where the element count
         * is known before deserializing
         */
        fun reserve(length: Int) {
            buffer.ensureCapacity(length)
        }

        /**
         * Pads the buffer with nulls up until [length] elements, efficiently ensuring the capacity ahead of time. This
         * can be used in cases where the element count is known before deserializing, and *must* be used if elements
         * are going to be populated using [set].
         */
        fun padToLength(length: Int) {
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
        fun set(index: Int, value: T) {
            buffer[index] = value
        }

        fun add(value: T) {
            buffer.add(value)
        }

        override fun clear() {
            buffer.clear()
        }

        override fun apply(target: Array<T>?): Array<T> {
            val value: Array<T>
            if(buffer.size == target?.size) {
                value = target
            } else {
                @Suppress("UNCHECKED_CAST")
                value = type.asArrayMirror().newInstance(buffer.size) as Array<T>
            }

            buffer.forEachIndexed { index, element ->
                @Suppress("UNCHECKED_CAST")
                value[index] = element as T
            }

            if(buffer.size > 500) {
                buffer = ArrayList()
            } else {
                buffer.clear()
            }

            return value
        }

        override fun populate(value: Array<T>) {
            clear()
            buffer.addAll(value)
        }
    }
}