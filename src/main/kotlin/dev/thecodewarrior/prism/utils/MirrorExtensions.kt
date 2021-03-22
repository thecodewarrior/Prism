package dev.thecodewarrior.prism.utils

import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

internal inline fun <reified T> ClassMirror.annotation(): T? {
    annotations.forEach { if(it is T) return it }
    return null
}

internal inline fun <reified T> FieldMirror.annotation(): T? {
    annotations.forEach { if(it is T) return it }
    return null
}

internal inline fun <reified T> MethodMirror.annotation(): T? {
    annotations.forEach { if(it is T) return it }
    return null
}

internal inline fun <reified T> ConstructorMirror.annotation(): T? {
    annotations.forEach { if(it is T) return it }
    return null
}

internal val <T: Any> KClass<T>.allDeclaredMemberProperties: Collection<KProperty1<*, *>>
    get() {
        val kClasses = listOf(this) + this.allSupertypes.mapNotNull { it.classifier }.filterIsInstance<KClass<*>>()
        return kClasses.flatMap { it.declaredMemberProperties }
    }

internal val KProperty<*>.declaringClass: Class<*>?
    get() = this.javaField?.declaringClass ?: this.javaGetter?.declaringClass
