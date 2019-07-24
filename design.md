## Notes about the design of Prism

#### Automatic and annotation-driven
[Type analyzers](https://github.com/TeamWizardry/Prism/blob/master/core/src/main/kotlin/com/teamwizardry/prism/TypeAnalyzer.kt)
should work with as little configuration as possible. This often requires searching for multiple possible solutions to 
a problem and picking the best one. For example, a `List` analyzer could implement multiple strategies for 
instantiating new objects:
- use a zero-argument constructor and call `addAll`
- use a constructor that accepts another list
- or use a constructor that accepts an array

#### Compatibility
Type analyzers should have built-in support for as many libraries as possible, using the (as yet unimplemented)
NoClassDefFoundError-tolerant class loading functionality of Mirror to only load these classes if they are present. For
example, optimized list/set serializers for the primitive collection types found in
[trove](http://trove4j.sourceforge.net/html/overview.html) and [fastutil](http://fastutil.di.unimi.it/).

#### Useful failures
(this behavior is up in the air, I'm not sure how best to handle it. This is just the idea so far)
Serializers should fail at whatever point would be most useful for the user, failing immediately when essential
requirements aren't met, but only failing when optional functionality is actually used. For example, A `List` analyzer
should fail immediately if a serializer couldn't be found for its component type, but if it couldn't find a constructor
it should only fail when an instance actually needs to be created. 

#### Descriptive errors
Exception messages should be very descriptive and ideally provide a short recommendation for how to fix the error.
Serialization logic is often very complicated and daunting for the average user, so if possible they should end with a
recommendation for how to fix the error or what to check to investigate the error. For example:
```txt
com.teamwizardry.prism.DeserializationException: Error deserializing `com.pack.age.CoolContainer<com.pack.age.CoolObject>`. 
  An exception was thrown deserializing the field `com.pack.age.AbstractContainer<com.pack.age.CoolObject>.contents`.
    at ...
    at ...
Caused by: com.teamwizardry.prism.InstantiationException: Could not create a new instance of `com.pack.age.CoolObject`.
  Final fields (id, size) had to be modified but no suitable constructor was be found. See ObjectAnalyzer for information on final fields.
    at ...
    at ...
```

