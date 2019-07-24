package com.teamwizardry.prism.annotation

/**
 * Marks a class or field, indicating that existing instances should never be modified and instead new instances should
 * always be created.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class AlwaysInstantiate {
}
