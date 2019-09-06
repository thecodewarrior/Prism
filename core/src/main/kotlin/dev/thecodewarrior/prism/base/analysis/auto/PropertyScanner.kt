package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractSetter
import dev.thecodewarrior.prism.internal.identitySetOf
import dev.thecodewarrior.prism.utils.ProblemTracker
import dev.thecodewarrior.prism.utils.allDeclaredMemberProperties
import dev.thecodewarrior.prism.utils.annotation
import dev.thecodewarrior.prism.utils.declaringClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

class PropertyScanner(val problems: ProblemTracker, val type: ClassMirror) {
    // store multiple values so we can detect duplicates
    val kotlin = mutableMapOf<String, MutableSet<KProperty<*>>>()
    val fields = mutableMapOf<String, MutableSet<FieldMirror>>()

    val getters = mutableMapOf<String, MutableSet<MethodMirror>>()
    val setters = mutableMapOf<String, MutableSet<MethodMirror>>()

    val candidates = mutableMapOf<String, PropertyCandidates>()

    class PropertyCandidates(val name: String) {
        val kotlin: MutableSet<KProperty<*>> = identitySetOf()
        val fields: MutableSet<FieldMirror> = identitySetOf()
        val getters: MutableSet<MethodMirror> = identitySetOf()
        val setters: MutableSet<MethodMirror> = identitySetOf()

        fun detectErrors(problems: ProblemTracker) {
            var i = 1
            val kotlin = this.kotlin.associateWith { i++ }
            val fields = this.fields.associateWith { i++ }
            val getters = this.getters.associateWith { i++ }
            val setters = this.setters.associateWith { i++ }

            val kotlinOutline = kotlin.map { (property, index) ->
                "\n$index: $property in class ${property.declaringClass?.let { Mirror.reflect(it).toString() } ?: "?"}"
            }.joinToString("")
            val fieldsOutline = fields.map { (field, index) ->
                "\n$index: $field in class ${field.declaringClass}"
            }.joinToString("")
            val gettersOutline = getters.map { (getter, index) ->
                "\n$index: $getter in class ${getter.declaringClass}"
            }.joinToString("")
            val settersOutline = setters.map { (setter, index) ->
                "\n$index: $setter in class ${setter.declaringClass}"
            }.joinToString("")

            val outline = """
                |[Property] Errors occurred analyzing property `$name`
                |    Discovered ${kotlin.size} Kotlin properties:${kotlinOutline.prependIndent("|        ")}
                |    Discovered ${fields.size} fields:${fieldsOutline.prependIndent("|        ")}
                |    Discovered ${getters.size} getters:${gettersOutline.prependIndent("|        ")}
                |    Discovered ${setters.size} setters:${settersOutline.prependIndent("|        ")}
                |    [Errors]:
            """.trimMargin()

            val errors = mutableListOf<String>()
            if(kotlin.size > 1) {
                errors.add("[Error] Multiple kotlin properties were found with the same name.")
                if(kotlin.keys.any { it.findAnnotation<Refract>()?.value?.isBlank() == false })
                    errors.add("[Tip] @Refract annotations with custom names may be causing the conflict")
                if(kotlin.keys.mapNotNullTo(mutableSetOf()) { it.declaringClass }.size > 1)
                    errors.add("[Tip] A property with the same name from a superclass may be causing the conflict")
            }
            if(fields.size > 1) {
                errors.add("Multiple fields with were found with the same name.")
            }
            if(getters.size > 1) {
                errors.add("Multiple getters with the same name")
            }
            if(setters.size > 1) {
                errors.add("Multiple setters with the same name")
            }

            if(kotlin.isNotEmpty() && fields.isNotEmpty()) {
                errors.add("")
            }
            if((kotlin.isNotEmpty() || fields.isNotEmpty()) && getters.isNotEmpty()) {
                errors.add("Conflicting fields and properties")
            }
            if((kotlin.isNotEmpty() || fields.isNotEmpty()) && getters.isNotEmpty()) {
                errors.add("Conflicting fields and properties")
            }



//            getters.forEach { (name, values) ->
//                if(name in fields) {
//                    problems.error("Field property conflicts with getters")
//                }
//            }
//            setters.forEach { (name, values) ->
//                if(name in fields) {
//                    problems.error("Field property conflicts with setters")
//                }
//            }
//
//            (setters.keys - getters.keys).forEach { name ->
//                problems.error("Setter with no getter. Property ignored")
//            }
//            (getters.keys + fields.keys).forEach { name ->
//                problems.error("")
//            }
        }
    }

    private fun getCandidate(name: String) = candidates.getOrPut(name) { PropertyCandidates(name) }

    private fun populateMembers() {
        type.allFields.forEach { field ->
            val annot = field.annotation<Refract>() ?: return@forEach
            val name = if (annot.value.isBlank()) field.name else annot.value
            getCandidate(name).fields.add(field)
        }

        type.allMethods.forEach { method ->
            method.annotation<RefractGetter>()?.also { annot ->
                getCandidate(annot.value).getters.add(method)
            }
            method.annotation<RefractSetter>()?.also { annot ->
                getCandidate(annot.value).setters.add(method)
            }
        }

        type.kClass!!.allDeclaredMemberProperties.forEach { property ->
            val annot = property.findAnnotation<Refract>() ?: return@forEach
            val name = if(annot.value.isBlank()) property.name else annot.value
            getCandidate(name).kotlin.add(property)
        }
    }

    init {
        populateMembers()
        candidates.forEach { it.value.detectErrors(problems) }
    }
}

abstract class ObjectProperty(val name: String) {
    abstract val isImmutable: Boolean
    abstract fun getValue(target: Any): Any?
    abstract fun setValue(target: Any, value: Any?)
}

class FieldProperty(val mirror: FieldMirror): ObjectProperty(mirror.name) {
    override val isImmutable: Boolean
        get() = mirror.isFinal

    override fun getValue(target: Any): Any? {
        return mirror.get(target)
    }

    override fun setValue(target: Any, value: Any?) {
        mirror.set(target, value)
    }
}

class AccessorProperty(val mirror: FieldMirror): ObjectProperty(mirror.name) {
    override val isImmutable: Boolean
        get() = mirror.isFinal

    override fun getValue(target: Any): Any? {
        return mirror.get(target)
    }

    override fun setValue(target: Any, value: Any?) {
        mirror.set(target, value)
    }
}
