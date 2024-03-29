package dev.thecodewarrior.prism.annotation

/**
 * Marks a class to be automatically (de)serialized
 */
@Target(AnnotationTarget.CLASS)
public annotation class RefractClass

/**
 * Marks a field or Kotlin property to be automatically (de)serialized. This annotation is only effective in classes
 * with the [@RefractClass][RefractClass] annotation.
 *
 * @param value the property name
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public annotation class Refract(val value: String)

/**
 * Marks a method as a property getter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation.
 *
 * @param value the property name. Used for serialization and to match it with a [RefractSetter] method.
 */
@Target(AnnotationTarget.FUNCTION)
public annotation class RefractGetter(val value: String)

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
