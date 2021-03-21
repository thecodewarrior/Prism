package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.reflectcase.ReflectTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class PropertyScannerTest: ReflectTest(
    "dev.thecodewarrior.prism.annotation.*"
) {
    // # Basics
    // a class with no @Refract annotated fields or methods should have no properties
    // fields should be scanned to correctly-configured FieldProperty properties
    // getters and setters should be scanned to correctly-configured AccessorProperty properties
    // kotlin properties with backing fields should be scanned to correctly-configured AccessorProperty properties
    // kotlin properties without backing fields should be scanned to correctly-configured AccessorProperty properties
    // @JvmField kotlin properties should be scanned to correctly-configured FieldProperty properties
    // directly annotated backing fields property should be scanned to correctly-configured FieldProperty properties
    // all three property types should be able to coexist without conflicts

    // # @RefractImmutable
    // adding '@RefractImmutable' to a non-final field should mark the resulting property as immutable
    // adding '@RefractImmutable' to a final field should throw an error
    // adding '@RefractImmutable' to a kotlin 'var' should mark the resulting property as immutable
    // adding '@RefractImmutable' to a kotlin 'val' should throw an error

    // # @RefractMutable
    // adding '@RefractMutable' to a final field should mark the resulting property as mutable
    // adding '@RefractMutable' to a non-final field should throw an error
    // adding '@RefractMutable' to a kotlin 'val' with a backing field should return a HybridMutableProperty
    // adding '@RefractMutable' to a kotlin 'val' without a backing field should throw an error
    // adding '@RefractMutable' to a kotlin 'var' should throw an error

    // # Empty names - should these be allowed?
    // a field with an empty name
    // a kotlin property with an empty name
    // a getter/setter with an empty name

    // # Error cases
    // a setter with no getter
    // duplicate fields // specific error?
    // duplicate setters
    // duplicate getters
    // duplicate properties
    // duplicate field+getter/setter, field+property, getter/setter+property

    // # Error reporting
    // a single error
    // multiple errors
    // errors only during the initial scan
    // errors only when resolving candidates
    // errors during the initial scan *and* when resolving candidates

    @Test
    fun scan() {
    }
}