package dev.thecodewarrior.prism.base.analysis.auto

import dev.thecodewarrior.prism.PrismException

/**
 * Errors that occur while analyzing an object class. Distinct [AutoSerializationException], which is for errors that
 * occur at runtime.
 */
open class ObjectAnalysisException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

class InvalidRefractAnnotationException: ObjectAnalysisException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

class InvalidRefractSignatureException: ObjectAnalysisException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

open class AutoSerializationException: PrismException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}
