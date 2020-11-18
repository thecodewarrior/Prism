# TODO
- optional properties?
- `@Upgrade`
- `@RefractData`
- `RefractConstructor.Pad`
- property name rules and validation
- wrap errors for informative reporting (e.g. "Error analyzing property... caused by:
- autoDetectSetter

# Tests
## Object Analyzer
### Property Scanner
- **Detection:**
    - `@Refract`, `@Refract(name)`, `@Refract + @field:Refract(name)` = err, duplicate properties
    - getters/setters, no getter, multiple getters, multiple setters, mismatched types, wrong signature (i.e. get takes param, set returns value)
    - Kotlin property, `@JvmField var`,
    - var w/ custom getter & backing field, var w/ custom setter & backing field, var w/ custom getter/setter and no backing field
    - val w/ custom getter & backing field, val w/ custom getter & no backing field
- **annotations:**
    - field, getter/setter, property
- **isImmutable:**
    - `@RefractMutable`
        - final field, non-final field
        - val w/ field, var w/ field
        - val w/o field, var w/o field
    - `@RefractImmutable`
        - final field, non-final field
        - val w/ field, var w/ field
        - val w/o field, var w/o field
    - `@RefractMutable` + `@RefractImmutable`
        - final field, non-final field
        - val w/ field, var w/ field
        - val w/o field, var w/o field
- **getValue:**
    - public field, private field
    - public getter, private getter
    - public val, private val
    - public val w/ getter, private val w/ getter
    - public var, private var
    - public var w/ getter, private var w/ getter
    - public @JvmField, private @JvmField
- **setValue:**
    - field, @RefractImmutable field, @RefractImmutable final field
    - final field, @RefractMutable final field, @RefractMutable field
    - var, @RefractImmutable var
    - val, @RefractMutable val
    - @JvmField var, @JvmField @RefractImmutable var
    - @JvmField val, @JvmField @RefractMutable val
- **type:**
    - field, getter/setter, property
- **future:**
    - @RefractData
    - optional properties
    - property name validation (e.g. any valid Java identifier: `/[a-zA-Z$_][a-zA-Z$_0-9]*/`)

### Constructor Scanner
- no constructor, multiple constructors
- no param names, override param names
- all mutable properties & constructor w/ no params
- immutable + mutable & constructor w/ no params 
- immutable + mutable & constructor w/ all properties
- immutable + mutable & constructor w/ only immutable
- immutable + mutable & constructor missing some immutable
- params with no corresponding property
- params with mismatched type
- **future:**
    - RefractConstructor.Pad

### Reading Objects
- missing properties
- null existing w/ no constructor, null existing w/ constructor
- immutables changed w/ no constructor, immutables changed w/ constructor
- custom didChange function
- change immutables then mutables
- constructor w/ immutables + mutables
- **exception wrapping:**
    - exception calling constructor
    - exception setting property
    - exception getting property
- **future:**
    - @Upgrade w/o respective data (does nothing)
    - @Upgrade w/ data
    - optional
    - `RefractConstructor.Pad`


### Edge cases
- `data class Foo @RefractConstructor constructor(val x: Int = 0)` generates multiple annotated constructors (Because 
  of the default value)