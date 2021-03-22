package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.IllegalTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalyzer

public class ListAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror)
    : TypeAnalyzer<List<T>, ListReader<T, S>, ListWriter<T, S>, S>(prism, type) {

    public val listType: ClassMirror = type.findSuperclass(List::class.java)?.asClassMirror()
        ?: throw IllegalTypeException()
    public val elementType: TypeMirror = listType.typeParameters[0]

    private var constructor = type.declaredConstructors.find { it.parameters.isEmpty() }!! // TODO replace with helper

    override fun createReader(): ListReader<T, S> = Reader()
    override fun createWriter(): ListWriter<T, S> = Writer()

    private inner class Reader: ListReader<T, S> {
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

        override fun build(): MutableList<T> {
            val value: MutableList<T> = constructor.call()

            @Suppress("UNCHECKED_CAST")
            value.addAll(buffer as List<T>)

            return value
        }
    }

    private inner class Writer: ListWriter<T, S> {
        override val serializer: S by prism[elementType]
        private var elements: List<T> = emptyList()

        override val size: Int
            get() = elements.size

        override fun get(index: Int): T {
            return elements[index]
        }

        override fun load(value: List<T>) {
            elements = value
        }

        override fun release() {
            elements = emptyList()
            release(this)
        }
    }
}