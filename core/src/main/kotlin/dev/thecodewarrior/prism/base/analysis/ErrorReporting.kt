package dev.thecodewarrior.prism.base.analysis

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class AnalysisError(public val message: String, public val cause: Throwable?)

public sealed class AnalysisResult<T> {
    public class Success<T>(public val value: T): AnalysisResult<T>()
    public class Error<T>(public val errors: List<AnalysisError>): AnalysisResult<T>()

    public class Builder<T> {
        private val errors = mutableListOf<AnalysisError>()
        public val hasErrors: Boolean
            get() = errors.isNotEmpty()
        private var value: T? = null

        public fun add(error: AnalysisError): Builder<T> {
            errors.add(error)
            return this
        }

        public fun add(message: String, cause: Throwable?): Builder<T> {
            errors.add(AnalysisError(message, cause))
            return this
        }

        /**
         * Returns an error result if there are any errors, otherwise returns null.
         */
        public fun errorOrNull(): Error<T>? {
            return if(errors.isEmpty()) null else Error(errors)
        }

        /**
         * Calls the given block if there are any errors. This can be useful for early returns.
         */
        public inline fun ifError(block: (Error<T>) -> Unit) {
            contract {
                callsInPlace(block, InvocationKind.AT_MOST_ONCE)
            }
            errorOrNull()?.also(block)
        }
    }
}
