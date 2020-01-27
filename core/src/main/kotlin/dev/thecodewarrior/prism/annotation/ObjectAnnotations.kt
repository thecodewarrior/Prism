package dev.thecodewarrior.prism.annotation

/**
 * Marks a class to be automatically (de)serialized
 *
 * @param value Override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.CLASS)
annotation class RefractClass

/**
 * Marks a field or Kotlin property to be automatically (de)serialized. This annotation is only effective in classes
 * with the [@RefractClass][RefractClass] annotation.
 *
 * @param value Override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class Refract(val value: String = "")

/**
 * Marks a method as a property getter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation.
 *
 * @param value Override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class RefractGetter(val value: String)

/**
 * Marks a method as a property setter to be automatically (de)serialized. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation.
 *
 * @param value Override the property name. If this is blank the name of the underlying field/property is used.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class RefractSetter(val value: String)

/**
 * When placed on a field, Kotlin property, or setter method, this annotation indicates what type of test should be done
 * to test whether a property has changed and needs to be set. This defaults to [Type.IDENTITY].
 *
 * @param value The test to use
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class UpdateTest(val value: Type) {
    enum class Type {
        /**
         * Don't perform any checks, always call the setter.
         */
        ALWAYS,
        /**
         * Get the current value and skip the setter if the new value is equal to the existing value by _identity._ That
         * is, they point to the same object. This is the default test.
         *
         * This is generally used because many serializers for mutable types will return the existing value back, so
         * this check will skip over them.
         */
        IDENTITY,
        /**
         * Get the current value and skip the setter if the new value is equal to the existing value by _value._ That
         * is, `newValue.equals(oldValue)`
         */
        VALUE
    }
}


/**
 * Marks a constructor to be used by Prism to create instances of this class. This annotation is only effective in
 * classes with the [@RefractClass][RefractClass] annotation.
 *
 * @param value Override the properties each parameter corresponds to. If this is blank the name of the underlying
 * parameters are used.
 * @param priority Set a priority to choose one constructor over another when possible. The default is zero, and higher
 * numbers will be chosen preferentially.
 */
@Target(AnnotationTarget.CONSTRUCTOR)
annotation class RefractConstructor(val value: Array<String> = [], val priority: Int = 0)

/**
 * Marks a Kotlin data class to be serialized
 */
@Target(AnnotationTarget.CLASS)
annotation class RefractData
