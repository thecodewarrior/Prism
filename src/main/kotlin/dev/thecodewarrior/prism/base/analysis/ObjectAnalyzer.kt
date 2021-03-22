package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalyzer
import dev.thecodewarrior.prism.base.analysis.impl.ObjectProperty
import dev.thecodewarrior.prism.base.analysis.impl.PropertyScanner
import dev.thecodewarrior.prism.base.analysis.impl.ConstructorScanner
import dev.thecodewarrior.prism.base.analysis.impl.ObjectConstructor
import dev.thecodewarrior.prism.base.analysis.impl.ObjectReaderImpl
import dev.thecodewarrior.prism.base.analysis.impl.ObjectWriterImpl

public class ObjectAnalyzer<T: Any, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror)
    : TypeAnalyzer<T, ObjectReader<T, S>, ObjectWriter<T, S>, S>(prism, type)  {
    internal val properties: List<ObjectProperty<S>> = PropertyScanner.scan(prism, type)
        @JvmSynthetic get
    internal val constructor: ObjectConstructor? = ConstructorScanner.findConstructor(type, properties)
        @JvmSynthetic get

    override fun createReader(): ObjectReader<T, S> = ObjectReaderImpl(this)
    override fun createWriter(): ObjectWriter<T, S> = ObjectWriterImpl(this)
}
