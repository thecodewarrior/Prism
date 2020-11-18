package dev.thecodewarrior.prism

public open class PrismException: RuntimeException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when something is passed an inappropriate type (e.g.passing the `String` type to a `ListAnalyzer`).
 * This is distinct from [InvalidTypeException], which is thrown when a type is for some reason unserializable (e.g. it
 * isn't concrete, there's no valid constructor, etc.).
 */
public class IllegalTypeException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when a type is for some reason unserializable (e.g. it isn't concrete, there's no valid constructor, etc.).
 * This is distinct from [IllegalTypeException], which is thrown when something is passed an inappropriate type (e.g.
 * passing the `String` type to a `ListAnalyzer`).
 */
public class InvalidTypeException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when an object instantiation is needed but the serializer is unable to do so. This is not thrown to wrap
 * exceptions thrown in constructors, it is specifically for errors when determining _how_ to instantiate an object.
 */
public class InstantiationException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

/**
 * Thrown when the object analyzer encounters an error occurs getting or setting a property's value
 */
public class PropertyAccessException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

public class SerializerNotFoundException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

public class DeserializationException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

public class SerializationException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}
