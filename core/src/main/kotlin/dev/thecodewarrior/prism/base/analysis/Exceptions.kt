package dev.thecodewarrior.prism.base.analysis

import dev.thecodewarrior.prism.PrismException

/**
 * Errors that occur while analyzing an object class. Distinct from [AutoSerializationException], which is for errors that
 * occur at runtime.
 */
public open class ObjectAnalysisException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

public class InvalidRefractAnnotationException: ObjectAnalysisException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

public class InvalidRefractSignatureException: ObjectAnalysisException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}

public open class AutoSerializationException: PrismException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}
