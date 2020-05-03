package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.InvalidTypeException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.annotation.RefractUpdateTest
import dev.thecodewarrior.prism.internal.identitySetOf
import dev.thecodewarrior.prism.utils.allDeclaredMemberProperties
import dev.thecodewarrior.prism.utils.annotation
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

class PropertyScanner<S: Serializer<*>>(val prism: Prism<S>, val type: ClassMirror) {
    val candidates = mutableMapOf<String, PropertyCandidate>()
    val properties: List<ObjectProperty<S>>

    inner class PropertyCandidate(val name: String) {
        val kotlin: MutableSet<KProperty<*>> = identitySetOf()
        val fields: MutableSet<FieldMirror> = identitySetOf()
        val getters: MutableSet<MethodMirror> = identitySetOf()
        val setters: MutableSet<MethodMirror> = identitySetOf()

        fun resolve(): ObjectProperty<S> {

            val count = kotlin.size + fields.size + getters.size * 0.5 + setters.size * 0.5

            if(count != 1.0)
                throw InvalidTypeException("Multiple properties or fields or getters for $name")

            if(kotlin.isNotEmpty())
                return KotlinProperty.create(prism, name, type, kotlin.first())

            if(fields.isNotEmpty())
                return FieldProperty(prism, name, fields.first())

            if(getters.isEmpty() || setters.isNotEmpty())
                throw InvalidTypeException("Setter with no getter")
            return AccessorProperty(prism, name, getters.first(), setters.firstOrNull())
        }
    }

    private fun getCandidate(name: String) = candidates.getOrPut(name) { PropertyCandidate(name) }

    private fun populateMembers() {
        type.fields.forEach { field ->
            val annot = field.annotation<Refract>() ?: return@forEach
            val name = if (annot.value.isBlank()) field.name else annot.value
            getCandidate(name).fields.add(field)
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

        type.kClass.allDeclaredMemberProperties.forEach { property ->
            val annot = property.findAnnotation<Refract>() ?: return@forEach
            val name = if(annot.value.isBlank()) property.name else annot.value
            getCandidate(name).kotlin.add(property)
        }
    }

    init {
        populateMembers()
        properties = candidates.map { it.value.resolve() }
    }
}

abstract class ObjectProperty<S: Serializer<*>>(val name: String) {
    abstract val type: TypeMirror
    abstract val isImmutable: Boolean
    abstract val serializer: S
    abstract fun getValue(target: Any): Any?
    abstract fun setValue(target: Any, value: Any?)
    /**
     * Returns true if setting the property to the passed value requires an actual update. Typically this performs an
     * identity-equals operation with the current value.
     */
    abstract fun needsUpdate(target: Any, value: Any?): Boolean
}

class FieldProperty<S: Serializer<*>>(prism: Prism<S>, name: String, val mirror: FieldMirror): ObjectProperty<S>(name) {
    override val type: TypeMirror get() = mirror.type
    private val updateTest: RefractUpdateTest.Type = mirror.annotation<RefractUpdateTest>()?.value ?: RefractUpdateTest.Type.IDENTITY_CHANGED

    override val isImmutable: Boolean
        get() = mirror.isFinal
    override val serializer: S by prism[mirror.type]

    override fun getValue(target: Any): Any? {
        return mirror.get(target)
    }

    override fun setValue(target: Any, value: Any?) {
        if(isImmutable)
            throw AutoSerializationException("Property $name is read-only")
        mirror.set(target, value)
    }

    override fun needsUpdate(target: Any, value: Any?): Boolean {
        return when(updateTest) {
            RefractUpdateTest.Type.ALWAYS -> true
            RefractUpdateTest.Type.IDENTITY_CHANGED -> value !== getValue(target)
            RefractUpdateTest.Type.VALUE_CHANGED -> value != getValue(target)
        }
    }
}

class AccessorProperty<S: Serializer<*>>(prism: Prism<S>, name: String, val getter: MethodMirror, val setter: MethodMirror?): ObjectProperty<S>(name) {
    override val type: TypeMirror get() = getter.returnType
    private val updateTest: RefractUpdateTest.Type = setter?.annotation<RefractUpdateTest>()?.value ?: RefractUpdateTest.Type.IDENTITY_CHANGED

    override val isImmutable: Boolean
        get() = setter != null
    override val serializer: S by prism[getter.returnType]

    override fun getValue(target: Any): Any? {
        return getter.call(target)
    }

    override fun setValue(target: Any, value: Any?) {
        if(isImmutable)
            throw AutoSerializationException("Property $name is read-only")
        setter!!.call(target, value) as Any?
    }

    override fun needsUpdate(target: Any, value: Any?): Boolean {
        return when(updateTest) {
            RefractUpdateTest.Type.ALWAYS -> true
            RefractUpdateTest.Type.IDENTITY_CHANGED -> value !== getValue(target)
            RefractUpdateTest.Type.VALUE_CHANGED -> value != getValue(target)
        }
    }
}

abstract class KotlinProperty<S: Serializer<*>>(prism: Prism<S>, name: String, val property: KProperty<*>): ObjectProperty<S>(name) {
    class KotlinFieldProperty<S: Serializer<*>>(
        prism: Prism<S>, name: String, property: KProperty<*>, val mirror: FieldMirror
    ): KotlinProperty<S>(prism, name, property) {
        override val type: TypeMirror get() = mirror.type
        private val updateTest: RefractUpdateTest.Type = property.findAnnotation<RefractUpdateTest>()?.value ?: RefractUpdateTest.Type.IDENTITY_CHANGED

        override val isImmutable: Boolean
            get() = property !is KMutableProperty<*>
        override val serializer: S by prism[mirror.type]

        override fun getValue(target: Any): Any? {
            return mirror.get(target)
        }

        override fun setValue(target: Any, value: Any?) {
            if(isImmutable)
                throw AutoSerializationException("Property $name is read-only")
            mirror.set(target, value)
        }

        override fun needsUpdate(target: Any, value: Any?): Boolean {
            return when(updateTest) {
                RefractUpdateTest.Type.ALWAYS -> true
                RefractUpdateTest.Type.IDENTITY_CHANGED -> value !== getValue(target)
                RefractUpdateTest.Type.VALUE_CHANGED -> value != getValue(target)
            }
        }
    }

    class KotlinMethodProperty<S: Serializer<*>>(
        prism: Prism<S>, name: String, property: KProperty<*>, val getter: MethodMirror, val setter: MethodMirror?
    ): KotlinProperty<S>(prism, name, property) {
        override val type: TypeMirror get() = getter.returnType
        private val updateTest: RefractUpdateTest.Type = property.findAnnotation<RefractUpdateTest>()?.value ?: RefractUpdateTest.Type.IDENTITY_CHANGED

        override val isImmutable: Boolean
            get() = property !is KMutableProperty<*>
        override val serializer: S by prism[getter.returnType]

        override fun getValue(target: Any): Any? {
            return getter.call(target)
        }

        override fun setValue(target: Any, value: Any?) {
            if(isImmutable)
                throw AutoSerializationException("Property $name is read-only")
            setter!!.call(target, value) as Any?
        }

        override fun needsUpdate(target: Any, value: Any?): Boolean {
            return when(updateTest) {
                RefractUpdateTest.Type.ALWAYS -> true
                RefractUpdateTest.Type.IDENTITY_CHANGED -> value !== getValue(target)
                RefractUpdateTest.Type.VALUE_CHANGED -> value != getValue(target)
            }
        }
    }

    companion object {
        fun <S: Serializer<*>> create(prism: Prism<S>, name: String, parentType: ClassMirror, property: KProperty<*>): KotlinProperty<S> {
            val field = property.javaField
            val getterMethod = property.getter.javaMethod
            val setterMethod = (property as? KMutableProperty<*>)?.setter?.javaMethod
            if(getterMethod != null)
                return KotlinMethodProperty(prism, name, property, parentType.getMethod(getterMethod)!!,
                    setterMethod?.let { parentType.getMethod(it)!! })
            if(field != null)
                return KotlinFieldProperty(prism, name, property, parentType.getField(field)!!)

            throw ObjectAnalysisException("Kotlin property $property has no getter method and no field")
        }
    }
}
