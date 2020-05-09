package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.InstantiationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.PropertyAccessException
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalyzer
import dev.thecodewarrior.prism.TypeReader
import dev.thecodewarrior.prism.TypeWriter
import dev.thecodewarrior.prism.internal.unmodifiableView

class ObjectAnalyzer<T: Any, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror)
    : TypeAnalyzer<T, ObjectReader<T, S>, ObjectWriter<T, S>, S>(prism, type)  {
    val properties: List<ObjectProperty<S>>
    val constructor: ObjectConstructor?

    init {
        // TODO: enforce that the iteration order here is stable
        val propertyScanner = PropertyScanner(prism, type)
        properties = propertyScanner.properties.unmodifiableView()
        constructor = ConstructorScanner.findConstructor(type, properties)
    }

    override fun createReader(): ObjectReader<T, S> = ObjectReaderImpl(this)
    override fun createWriter(): ObjectWriter<T, S> = ObjectWriterImpl(this)

    companion object {
        private val _nullableAnnotations: MutableList<String> = mutableListOf(
            "org.jetbrains.annotations.Nullable",
            "javax.annotation.Nullable",
            "javax.annotation.CheckForNull",
            "edu.umd.cs.findbugs.annotations.Nullable",
            "android.support.annotation.Nullable",
            "androidx.annotation.Nullable",
            "androidx.annotation.RecentlyNullable",
            "org.checkerframework.checker.nullness.qual.Nullable",
            "org.checkerframework.checker.nullness.compatqual.NullableDecl",
            "org.checkerframework.checker.nullness.compatqual.NullableType",
            "com.android.annotations.Nullable"
        )

        private val _notNullAnnotations: MutableList<String> = mutableListOf(
            "org.jetbrains.annotations.NotNull",
            "javax.annotation.Nonnull",
            "edu.umd.cs.findbugs.annotations.NonNull",
            "android.support.annotation.NonNull",
            "androidx.annotation.NonNull",
            "androidx.annotation.RecentlyNonNull",
            "org.checkerframework.checker.nullness.qual.NonNull",
            "org.checkerframework.checker.nullness.compatqual.NonNullDecl",
            "org.checkerframework.checker.nullness.compatqual.NonNullType",
            "com.android.annotations.NonNull"
        )

        val nullableAnnotations: List<String> = _nullableAnnotations.unmodifiableView()
        val notNullAnnotations: List<String> = _notNullAnnotations.unmodifiableView()

        fun registerNullableAnnotation(qualifiedName: String) {
            _nullableAnnotations.add(qualifiedName)
            try {
                _nullableAnnotationClasses.add(Class.forName(qualifiedName))
            } catch(e: ClassNotFoundException) {
                // nop
            }
        }

        fun registerNotNullAnnotation(qualifiedName: String) {
            _notNullAnnotations.add(qualifiedName)
            try {
                _notNullAnnotationClasses.add(Class.forName(qualifiedName))
            } catch(e: ClassNotFoundException) {
                // nop
            }
        }

        private val _nullableAnnotationClasses: MutableList<Class<*>> = mutableListOf()
        private val _notNullAnnotationClasses: MutableList<Class<*>> = mutableListOf()
        val nullableAnnotationClasses: List<Class<*>> = _nullableAnnotationClasses.unmodifiableView()
        val notNullAnnotationClasses: List<Class<*>> = _notNullAnnotationClasses.unmodifiableView()

        init {
            _nullableAnnotations.forEach { name ->
               try {
                   _nullableAnnotationClasses.add(Class.forName(name))
               } catch(e: ClassNotFoundException) {
                   // nop
               }
            }
            _notNullAnnotations.forEach { name ->
                try {
                    _notNullAnnotationClasses.add(Class.forName(name))
                } catch(e: ClassNotFoundException) {
                    // nop
                }
            }
        }
    }
}
