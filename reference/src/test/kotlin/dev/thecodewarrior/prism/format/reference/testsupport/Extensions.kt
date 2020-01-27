package dev.thecodewarrior.prism.format.reference.testsupport

import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError

/**
 * Asserts that this exception has a cause of the specified type, then returns that cause.
 */
inline fun <reified T : Throwable> Throwable.assertCause(message: String? = null): T = this.assertCause { message }

/**
 * Asserts that this exception has a cause of the specified type, then returns that cause.
 */
inline fun <reified T : Throwable> Throwable.assertCause(message: () -> String?): T {
    return when(val cause = this.cause) {
        null -> fail(AssertionUtils.format(message(), T::class.java, null, "Exception has no cause"))
        is T -> cause
        else -> throw AssertionFailedError(
            AssertionUtils.format(message(), T::class.java, cause.javaClass, "Unexpected cause type"),
            cause
        )
    }
}

/**
 * Asserts that this exception has the passed message, then returns this exception.
 */
inline fun <reified T : Throwable> T.assertMessage(expected: String?, assertionMessage: String? = null): T =
    this.assertMessage(expected) { assertionMessage }

/**
 * Asserts that this exception has the passed message, then returns this exception.
 */
inline fun <reified T : Throwable> T.assertMessage(expected: String?, assertionMessage: () -> String?): T {
    if(this.message == expected) {
        return this
    }
    throw AssertionFailedError(
        AssertionUtils.format(assertionMessage(), expected, message, "Unexpected message"),
        this
    )
}
