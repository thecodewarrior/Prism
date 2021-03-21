package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.InvalidTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractImmutable
import dev.thecodewarrior.prism.annotation.RefractMutable
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.base.analysis.AnalysisLog
import dev.thecodewarrior.prism.base.analysis.AutoSerializationException
import dev.thecodewarrior.prism.base.analysis.ObjectAnalysisException
import dev.thecodewarrior.prism.internal.identitySetOf
import dev.thecodewarrior.prism.internal.unmodifiableView
import dev.thecodewarrior.prism.utils.allDeclaredMemberProperties
import dev.thecodewarrior.prism.utils.annotation
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

internal class PropertyScannerOld<S: Serializer<*>>(log: AnalysisLog, val prism: Prism<S>, val type: ClassMirror) {
    val logger = log.logger<PropertyScannerOld<*>>()
    val candidates = mutableMapOf<String, PropertyCandidate>()
    val properties: List<ObjectProperty<S>>

    inner class PropertyCandidate(val name: String) {
        val properties: MutableSet<KProperty<*>> = identitySetOf()
        val getters: MutableSet<MethodMirror> = identitySetOf()
        val setters: MutableSet<MethodMirror> = identitySetOf()

        fun resolve(): ObjectProperty<S> {
            if (properties.size > 1)
                throw InvalidTypeException(
                    "Property $name has multiple @Refract annotated Java fields and/or Kotlin " +
                            "properties: [${properties.joinToString(", ")}]"
                )

            val count = properties.size + getters.size * 0.5 + setters.size * 0.5

//            if (count != 1.0)
//                throw InvalidTypeException("Property $name has ${properties.size} properties/fields, ${}")

            if (properties.isNotEmpty())
                return KotlinProperty.create(prism, name, type, properties.first())

//            if (fields.isNotEmpty())
//                return FieldProperty(prism, name, fields.first())

            if (getters.isEmpty() || setters.isNotEmpty())
                throw InvalidTypeException("Setter with no getter")
            return AccessorProperty(prism, name, getters.first(), setters.firstOrNull())
        }
    }

    private fun getCandidate(name: String) = candidates.getOrPut(name) { PropertyCandidate(name) }

    private fun populateMembers() {
        val properties = mutableMapOf<String, ObjectProperty<S>>()

        for(property in type.kClass.allDeclaredMemberProperties) {
            val annotation = property.findAnnotation<Refract>() ?: continue
            logger.debug("Scanning @Refract annotated member property $property")
            val name = if (annotation.value.isBlank()) {
                logger.debug("  Using property name `${property.name}`")
                property.name
            } else {
                logger.debug("  Overriding property name with `${annotation.value}` from the annotation")
                annotation.value
            }
            if (name in properties) {
                logger.error("  Property named `$name` already exists")
                continue
            }

            val field = property.javaField?.let { type.getField(it) }
            val getter = property.getter.javaMethod?.let { type.getMethod(it) }
            val setter = (property as? KMutableProperty<*>)?.setter?.javaMethod?.let { type.getMethod(it) }



            val type = field?.type
                ?: getter?.returnType
                ?: throw ObjectAnalysisException("Property $property has no getter or backing field")
            getCandidate(name).properties.add(property)
        }

        // get fields/properties, create ObjectProperty immediately
        //   if already exists, throw
        // find getters
        //   if a property already exists, throw
        //   find its setter
        //   autodetect + explicit = error
        // find setters with no properties, throw

        type.kClass.allDeclaredMemberProperties.forEach { property ->
            val annot = property.findAnnotation<Refract>() ?: return@forEach
            val name = if (annot.value.isBlank()) property.name else annot.value
            getCandidate(name).properties.add(property)
        }

        generateSequence(type) { it.superclass }
            .flatMap { it.declaredMethods.asSequence() }
            .forEach { method ->
                method.annotation<RefractGetter>()?.also { annot ->
                    getCandidate(annot.value).getters.add(method)
                }
                method.annotation<RefractSetter>()?.also { annot ->
                    getCandidate(annot.value).setters.add(method)
                }
            }
    }

    init {
        populateMembers()
        properties = candidates.map { it.value.resolve() }
    }

    private class FieldProperty<S: Serializer<*>>(prism: Prism<S>, name: String, val mirror: FieldMirror):
        ObjectProperty<S>(name, mirror.type, prism) {
        override val annotations: List<Annotation> = mirror.annotations

        override val isImmutable: Boolean

        init {
            val refractMutable = annotations.any { it is RefractMutable }
            val refractImmutable = annotations.any { it is RefractImmutable }
            if (refractMutable && refractImmutable)
                throw ObjectAnalysisException("@RefractMutable and @RefractImmutable are mutually exclusive")
            if (refractMutable && !mirror.isFinal)
                throw ObjectAnalysisException("@RefractMutable is only applicable to final fields")
            if (refractImmutable && mirror.isFinal)
                throw ObjectAnalysisException("@RefractImmutable is only applicable to non-final fields")

            isImmutable = refractImmutable || (mirror.isFinal && !refractMutable)
        }

        override fun getValue(target: Any): Any? {
            return mirror.get(target)
        }

        override fun setValue(target: Any, value: Any?) {
            if (isImmutable)
                throw AutoSerializationException("Property $name is read-only")
            mirror.set(target, value)
        }
    }

    private class AccessorProperty<S: Serializer<*>>(
        prism: Prism<S>,
        name: String,
        val getter: MethodMirror,
        val setter: MethodMirror?
    ): ObjectProperty<S>(name, getter.returnType, prism) {
        override val annotations: List<Annotation> = getter.annotations + setter?.annotations.orEmpty()

        override val isImmutable: Boolean
            get() = setter != null

        override fun getValue(target: Any): Any? {
            return getter.call(target)
        }

        override fun setValue(target: Any, value: Any?) {
            if (isImmutable)
                throw AutoSerializationException("Property $name is read-only")
            setter!!.call(target, value) as Any?
        }
    }

    private class KotlinProperty<S: Serializer<*>>(
        name: String,
        type: TypeMirror,
        prism: Prism<S>,
        val property: KProperty<*>,
        val field: FieldMirror?,
        val getter: MethodMirror?,
        val setter: MethodMirror?
    ): ObjectProperty<S>(name, type, prism) {
        override val annotations: List<Annotation> = property.annotations.unmodifiableView()

        override val isImmutable: Boolean

        init {
            var isMutable = setter != null || field?.isFinal == false

            val refractMutable = annotations.any { it is RefractMutable }
            val refractImmutable = annotations.any { it is RefractImmutable }
            if (refractMutable && refractImmutable)
                throw ObjectAnalysisException("Property $property is annotated both @RefractMutable and @RefractImmutable")

            if (refractMutable) {
                if (isMutable || field == null)
                    throw ObjectAnalysisException("Property $property is annotated @RefractMutable but has no backing field")
                isMutable = true
            }
            if (refractImmutable && !isMutable) {
                throw ObjectAnalysisException("Property $property is annotated @RefractImmutable but is already immutable")
            }

            isImmutable = refractImmutable || !isMutable
        }

        override fun getValue(target: Any): Any? {
            if (getter != null)
                return getter.call(target)
            if (field != null)
                return field.get(target)
            throw AutoSerializationException("Property $property has no getter or backing field") // should be impossible
        }

        override fun setValue(target: Any, value: Any?) {
            if (isImmutable)
                throw AutoSerializationException("Property $name is read-only")
            if (setter != null)
                return setter.call(target, value)
            if (field != null)
                return field.set(target, value)
        }

        companion object {
            fun <S: Serializer<*>> create(
                prism: Prism<S>,
                name: String,
                parentType: ClassMirror,
                property: KProperty<*>
            ): KotlinProperty<S> {
                val field = property.javaField?.let { parentType.getField(it) }
                val getter = property.getter.javaMethod?.let { parentType.getMethod(it) }
                val setter = (property as? KMutableProperty<*>)?.setter?.javaMethod?.let { parentType.getMethod(it) }
                val type = field?.type
                    ?: getter?.returnType
                    ?: throw ObjectAnalysisException("Property $property has no getter or backing field")

                return KotlinProperty(name, type, prism, property, field, getter, setter)
            }
        }
    }
}