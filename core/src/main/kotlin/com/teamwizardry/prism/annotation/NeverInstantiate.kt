package com.teamwizardry.prism.annotation

/**
 * Marks a class or field, indicating that existing instances should be modified and new instances should never be
 * created by the serializer.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class NeverInstantiate {
}
