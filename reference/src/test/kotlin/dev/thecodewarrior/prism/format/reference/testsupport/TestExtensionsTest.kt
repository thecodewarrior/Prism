package dev.thecodewarrior.prism.format.reference.testsupport

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class TestExtensionsTest {

    class ExceptionType1: RuntimeException {
        constructor(): super()
        constructor(message: String?): super(message)
        constructor(message: String?, cause: Throwable?): super(message, cause)
        constructor(cause: Throwable?): super(cause)
    }

    class ExceptionType2: RuntimeException {
        constructor(): super()
        constructor(message: String?): super(message)
        constructor(message: String?, cause: Throwable?): super(message, cause)
        constructor(cause: Throwable?): super(cause)
    }

    @Test
    fun assertCause_withCorrectCause_shouldReturnCause() {
        val cause = ExceptionType1()
        val assertResult = RuntimeException(cause).assertCause<ExceptionType1>()
        assertSame(cause, assertResult)
    }

    @Test
    fun assertCause_withIncorrectCause_shouldFail() {
        val e = assertThrows<AssertionFailedError> {
            val cause = ExceptionType1()
            RuntimeException(cause).assertCause<ExceptionType2>("Hi!")
        }
        assertEquals("Hi! ==> Unexpected cause type ==> " +
            "expected: <dev.thecodewarrior.prism.format.reference.testsupport.TestExtensionsTest.ExceptionType2> " +
            "but was: <dev.thecodewarrior.prism.format.reference.testsupport.TestExtensionsTest.ExceptionType1>",
            e.message
        )
    }

    @Test
    fun assertCause_withNoCause_shouldFail() {
        val e = assertThrows<AssertionFailedError> {
            RuntimeException().assertCause<ExceptionType1>("Hi!")
        }
        assertEquals("Hi! ==> Exception has no cause ==> " +
            "expected: <dev.thecodewarrior.prism.format.reference.testsupport.TestExtensionsTest.ExceptionType1> " +
            "but was: <null>",
            e.message
        )
    }

    @Test
    fun assertMessage_withCorrectNullMessage_shouldReturnSameException() {
        val exception = RuntimeException()
        val assertResult = exception.assertMessage(null)
        assertSame(exception, assertResult)
    }

    @Test
    fun assertMessage_withIncorrectNullMessage_shouldReturnSameException() {
        val e = assertThrows<AssertionFailedError> {
            RuntimeException("Hi, I'm a message!").assertMessage(null)
        }
        assertEquals("Unexpected message ==> expected: <null> but was: <Hi, I'm a message!>", e.message)
    }

    @Test
    fun assertMessage_withIncorrectMessage_shouldFail() {
        val e = assertThrows<AssertionFailedError> {
            RuntimeException("Hi, I'm a message!").assertMessage("Hi, I'm wrong!", "**C U S T O M**")
        }
        assertEquals(
            "**C U S T O M** ==> Unexpected message ==> expected: <Hi, I'm wrong!> but was: <Hi, I'm a message!>",
            e.message
        )
    }
}