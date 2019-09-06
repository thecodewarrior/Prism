package dev.thecodewarrior.prism.annotation

/**
 * Marks a class to be automatically (de)serialized
 *
 * @param value Override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.CLASS)
annotation class RefractClass

/**
 * Marks a field or kotlin property to be automatically (de)serialized. This annotation is only effective in classes
 * with the [@RefractClass][RefractClass] annotation
 *
 * @param value Override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class Refract(val value: String = "")

/**
 * Marks a method as a property getter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation
 */
@Target(AnnotationTarget.FUNCTION)
annotation class RefractGetter(val value: String)

/**
 * Marks a method as a property setter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation
 */
@Target(AnnotationTarget.FUNCTION)
annotation class RefractSetter(val value: String)

/**
 * Marks a Kotlin data class to be serialized
 */
@Target(AnnotationTarget.CLASS)
annotation class RefractData
