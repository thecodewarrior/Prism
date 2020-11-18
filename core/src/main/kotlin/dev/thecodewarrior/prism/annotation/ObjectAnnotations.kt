package dev.thecodewarrior.prism.annotation

// TODO - annotation or parameter that specifies whether to pass the existing object or not // property
// TODO - support autoDetectSetter

/**
 * Marks a class to be automatically (de)serialized
 */
@Target(AnnotationTarget.CLASS)
public annotation class RefractClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
public annotation class RefractTargets(vararg val targets: String)

/**
 * Marks a field or Kotlin property to be automatically (de)serialized. This annotation is only effective in classes
 * with the [@RefractClass][RefractClass] annotation.
 *
 * @param value override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public annotation class Refract(val value: String = "")

/**
 * Marks a method as a property getter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation.
 *
 * @param value the property name. Used for serialization and to match it with a [RefractSetter] method.
 * @param autoDetectSetter Automatically detect a setter by replacing `get` or `is` from the start of the annotated
 * method's name with `set`, and attempting to find setter method with the correct type and name. This will throw an
 * error if such a method could not be found.
 */
@Target(AnnotationTarget.FUNCTION)
public annotation class RefractGetter(val value: String, val autoDetectSetter: Boolean = false)

/**
 * Marks a method as a property setter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation. A setter with no getter is considered an error.
 *
 * @param value the property name. Used for deserialization and to match it with a [RefractGetter] method.
 */
@Target(AnnotationTarget.FUNCTION)
public annotation class RefractSetter(val value: String)

/**
 * Marks a constructor to be used by Prism to create instances of this class. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation. Each class can only have one annotated constructor.
 *
 * @param value Override the properties each parameter corresponds to. If this is blank the name of the underlying
 * parameters are used.
 */
@Target(AnnotationTarget.CONSTRUCTOR)
public annotation class RefractConstructor(val value: Array<String> = [])

/**
 * Marks a Kotlin data class to be serialized
 */
@Target(AnnotationTarget.CLASS)
public annotation class RefractData

/**
 * Marks a property as mutable, allowing Prism to modify it even if the underlying field is `final`. Adding this
 * annotation to an already mutable property or at the same time as [RefractImmutable] is considered an error.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public annotation class RefractMutable

/**
 * Marks a property as immutable, disallowing Prism from modify it even if it is normally mutable. Adding this
 * annotation to an already immutable property or at the same time as [RefractMutable] is considered an error.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public annotation class RefractImmutable
