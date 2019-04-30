package com.teamwizardry.prism.format.reference.testsupport

import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import org.junit.jupiter.api.BeforeEach

abstract class PrismTest {
    private var _prism: Prism<ReferenceSerializer<*>>? = null
    val prism: Prism<ReferenceSerializer<*>>
        get() = _prism ?: createPrism().also { _prism = it }

    abstract fun createPrism(): Prism<ReferenceSerializer<*>>

    @BeforeEach
    private fun preparePrism() {
        _prism = prism
    }
}