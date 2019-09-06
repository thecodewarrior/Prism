package dev.thecodewarrior.prism.utils

import org.slf4j.event.Level

class ProblemTracker {
    val problems: MutableList<Problem> = mutableListOf()

    fun error(message: String) {
        problems.add(Problem(Level.ERROR, message))
    }

    fun warn(message: String) {
        problems.add(Problem(Level.WARN, message))
    }

    fun info(message: String) {
        problems.add(Problem(Level.INFO, message))
    }

    fun debug(message: String) {
        problems.add(Problem(Level.DEBUG, message))
    }

    class Problem(val level: Level, val message: String)
}

