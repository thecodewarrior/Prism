Notes: 
- Don't annotate overridden getters or setters. ~~They will register twice (maybe make them not?)~~  
  Only in kotlin where we don't have mirror's equivalent of `allMethods`
- Interface members can be marked with `@Refract`
- Should subclasses need an `@RefractClass` annotation?

## Errors

### Property resolution

- Multiple names conflict
- Multiple getters conflict
- Multiple setters conflict

- A getter or setter conflicts with a field
- A setter with no getter
- A getter method has an incorrect signature  
  (it returns void, accepts parameters, is abstract, is static)
- A setter method has an incorrect signature  
  (it returns a value, does not have exactly one parameter, is abstract, is static)
- A getter/setter pair have incompatible types  
  (the getter's return type is not the same as the setter's parameter type)
- `@PrismFinal` property has setter
- Kotlin getter/setter annotated with `@RefractGetter/Setter`  
  - if the property is mutable and only the getter was annotated, maybe use `@PrismFinal`
- Property with `@PrismFinal` has an annotated setter  
  (the setter shouldn't be annotated, kotlin properties are fine with this)
