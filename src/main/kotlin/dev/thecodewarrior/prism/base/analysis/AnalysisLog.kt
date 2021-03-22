package dev.thecodewarrior.prism.base.analysis

import java.lang.RuntimeException
import java.lang.StringBuilder

public class AnalysisLog {
    private val entries = mutableListOf<LogEntry>()

    private data class LogEntry(
        val severity: Severity,
        val source: String,
        val message: String,
        val cause: Throwable?
    ) {
        override fun toString(): String {
            return "[$severity] [$source] $message" + (if(cause == null) "" else "\n" + cause.stackTraceToString())
        }
    }

    private enum class Severity {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    override fun toString(): String {
        return entries.joinToString("\n")
    }

    public fun logger(name: String): AnalysisLogger {
        return AnalysisLoggerImpl(name)
    }

    public fun logger(clazz: Class<*>): AnalysisLogger {
        return logger(clazz.simpleName)
    }

    public inline fun <reified T> logger(): AnalysisLogger {
        return logger(T::class.java)
    }

    public interface AnalysisLogger {
        public fun debug(message: String)
        public fun debug(message: String, cause: Throwable?)
        public fun info(message: String)
        public fun info(message: String, cause: Throwable?)
        public fun warn(message: String)
        public fun warn(message: String, cause: Throwable?)
        public fun error(message: String)
        public fun error(message: String, cause: Throwable?)
        public fun fatal(message: String)
        public fun fatal(message: String, cause: Throwable?)
    }

    private inner class AnalysisLoggerImpl(val source: String): AnalysisLogger {
        override fun debug(message: String) {
            debug(message, null)
        }

        override fun debug(message: String, cause: Throwable?) {
            log(Severity.DEBUG, source, message, cause)
        }

        override fun info(message: String) {
            info(message, null)
        }

        override fun info(message: String, cause: Throwable?) {
            log(Severity.INFO, source, message, cause)
        }

        override fun warn(message: String) {
            warn(message, null)
        }

        override fun warn(message: String, cause: Throwable?) {
            log(Severity.WARN, source, message, cause)
        }

        override fun error(message: String) {
            error(message, null)
        }

        override fun error(message: String, cause: Throwable?) {
            log(Severity.ERROR, source, message, cause)
        }

        override fun fatal(message: String) {
            fatal(message, null)
        }

        override fun fatal(message: String, cause: Throwable?) {
            log(Severity.FATAL, source, message, cause)
        }
    }

    private fun log(severity: Severity, source: String, message: String, cause: Throwable?) {
        entries.add(LogEntry(severity, source, message, cause))
    }
}