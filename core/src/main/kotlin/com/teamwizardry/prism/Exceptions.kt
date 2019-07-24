package com.teamwizardry.prism

open class PrismException: RuntimeException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when something is passed an inappropriate type (e.g.passing the `String` type to a `ListAnalyzer`).
 * This is distinct from [InvalidTypeException], which is thrown when a type is for some reason unserializable (e.g. it
 * isn't concrete, there's no valid constructor, etc.).
 */
class IllegalTypeException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when a type is for some reason unserializable (e.g. it isn't concrete, there's no valid constructor, etc.).
 * This is distinct from [IllegalTypeException], which is thrown when something is passed an inappropriate type (e.g.
 * passing the `String` type to a `ListAnalyzer`).
 */
class InvalidTypeException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when an object instantiation is needed but the serializer is unable to do so. This is not thrown to wrap
 * exceptions thrown in constructors, it is specifically for errors when determining _how_ to instantiate an object.
 */
class InstantiationException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

class SerializerNotFoundException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

class DeserializationException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

class SerializationException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}
