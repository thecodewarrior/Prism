package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalysis
import dev.thecodewarrior.prism.TypeAnalyzer
import dev.thecodewarrior.prism.internal.unmodifiableView

class ObjectAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror): TypeAnalyzer<ObjectAnalyzer<T, S>.ObjectAnalysis, S>(prism, type)  {
    val properties: List<ObjectProperty<S>>
    val nameMap: Map<String, ObjectProperty<S>>
    val instantiators: List<ObjectInstantiator<S>>

    init {
        val propertyScanner = PropertyScanner(prism, type)
        properties = propertyScanner.properties.unmodifiableView()
        nameMap = properties.associateBy { it.name }.unmodifiableView()
        val instantiatorScanner = InstantiatorScanner(prism, type, properties)
        instantiators = instantiatorScanner.instantiators.unmodifiableView()
    }

    override fun createState(): ObjectAnalysis = ObjectAnalysis()

    fun applyValues(target: Any?, values: Map<ObjectProperty<S>, ValueContainer>): Any {
        val needsInstance = target == null || values.any { (property, value) ->
            value.isPresent && property.isImmutable
        }
        if(needsInstance) {
            val instantiator = instantiators.find { it.properties.all { values[it]?.isPresent == true } } ?: TODO() // todo: error type
            val instance = instantiator.createInstance(instantiator.properties.map { values[it]!!.value })
            values.forEach { (property, value) ->
                if(value.isPresent && property !in instantiator.propertySet) {
                    property.setValue(instance, value.value)
                }
            }
            return instance
        } else {
            target!!
            //todo: missing keys == error?
            values.forEach { (property, value) ->
                if(value.isPresent) {
                    property.setValue(target, value.value)
                }
            }
            return target
        }
    }

    inner class ObjectAnalysis: TypeAnalysis<Any>() {
        val values = properties.associateWith { ValueContainer() }

        override fun clear() {
            values.values.forEach {
                it.value = null
                it.isPresent = false
            }
        }

        fun setValue(property: ObjectProperty<S>, value: Any?) {
            values[property]?.also {
                it.isPresent = true
                it.value = value
            }
        }

        fun getValue(property: ObjectProperty<S>): Any? {
            return values[property]?.value
        }

        override fun populate(value: Any) {
            values.forEach { (property, container) ->
                container.isPresent = true
                container.value = property.getValue(value)
            }
        }

        override fun apply(target: Any?): Any {
            return applyValues(target, values)
        }

    }

    class ValueContainer {
        var isPresent: Boolean = false
        var value: Any? = null
    }

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