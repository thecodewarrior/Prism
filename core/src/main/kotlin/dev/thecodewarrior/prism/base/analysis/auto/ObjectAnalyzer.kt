package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.TypeAnalysis
import dev.thecodewarrior.prism.TypeAnalyzer
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.internal.identitySetOf
import dev.thecodewarrior.prism.internal.unmodifiableView
import dev.thecodewarrior.prism.utils.annotation

class ObjectAnalyzer<T, S: Serializer<*>>(prism: Prism<S>, type: ClassMirror): TypeAnalyzer<ObjectAnalyzer<T, S>.ObjectAnalysis, S>(prism, type)  {
    val properties: List<ObjectProperty>
    val nameMap: Map<String, ObjectProperty>

    init {
        val _properties: List<ObjectProperty>
        val _nameMap: Map<String, ObjectProperty>

        run { // generate properties list
            // store multiple values so we can detect duplicates
            val properties = mutableMapOf<String, MutableSet<ObjectProperty>>()

            type.allFields.forEach { field ->
                val annot = field.annotation<Refract>() ?: return@forEach
                val name = if (annot.value.isBlank()) field.name else annot.value
                val property = FieldProperty(field)
                properties.getOrPut(name) { identitySetOf() }.add(property)
            }

            // store multiple values so we can detect duplicates
            val getters = mutableMapOf<String, MutableSet<MethodMirror>>()
            val setters = mutableMapOf<String, MutableSet<MethodMirror>>()
            type.allMethods.forEach { method ->
                method.annotation<RefractGetter>()?.also { annot ->
                    getters.getOrPut(annot.value) { identitySetOf() }.add(method)
                }
                method.annotation<RefractSetter>()?.also { annot ->
                    setters.getOrPut(annot.value) { identitySetOf() }.add(method)
                }
            }

            _properties = properties.values.map { it.first() }.unmodifiableView()
            _nameMap = properties.entries.associate { (k, v) -> k to v.first() }.unmodifiableView()
        }

        this.properties = _properties
        this.nameMap = _nameMap
    }

    override fun createState(): ObjectAnalysis = ObjectAnalysis()

    fun applyValues(target: Any?, values: Map<ObjectProperty, ValueContainer>): Any {
        val needsInstance = target == null || values.any { (property, value) ->
            value.isPresent && property.isImmutable
        }
        if(needsInstance) {
            TODO()
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

        fun setValue(property: ObjectProperty, value: Any?) {
            values[property]?.also {
                it.isPresent = true
                it.value = value
            }
        }

        fun getValue(property: ObjectProperty): Any? {
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

    abstract inner class ObjectProperty(val name: String) {
        abstract val isImmutable: Boolean
        abstract val serializer: S
        abstract fun getValue(target: Any): Any?
        abstract fun setValue(target: Any, value: Any?)
    }

    inner class FieldProperty(val mirror: FieldMirror): ObjectProperty(mirror.name) {
        override val isImmutable: Boolean
            get() = mirror.isFinal
        override val serializer: S by prism[mirror.type]

        override fun getValue(target: Any): Any? {
            return mirror.get(target)
        }

        override fun setValue(target: Any, value: Any?) {
            mirror.set(target, value)
        }
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