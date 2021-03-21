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
    val properties: List<ObjectProperty<S>>

    init {
        properties = emptyList()
    }
}