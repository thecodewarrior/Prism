package com.teamwizardry.prism.format.reference.testsupport

import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.builtin.ByteSerializer
import com.teamwizardry.prism.format.reference.builtin.CharSerializer
import com.teamwizardry.prism.format.reference.builtin.DoubleSerializer
import com.teamwizardry.prism.format.reference.builtin.FloatSerializer
import com.teamwizardry.prism.format.reference.builtin.IntSerializer
import com.teamwizardry.prism.format.reference.builtin.LongSerializer
import com.teamwizardry.prism.format.reference.builtin.ShortSerializer
import com.teamwizardry.prism.format.reference.builtin.StringSerializer
import org.junit.jupiter.api.BeforeEach

abstract class PrismTest {
    private var _prism: ReferencePrism<*>? = null
    val prism: ReferencePrism<*>
        get() = _prism ?: createPrism().also { _prism = it }

    protected fun registerPrimitives(prism: ReferencePrism<*>) {
        prism.register(StringSerializer)
        prism.register(LongSerializer)
        prism.register(IntSerializer)
        prism.register(ShortSerializer)
        prism.register(ByteSerializer)
        prism.register(CharSerializer)
        prism.register(DoubleSerializer)
        prism.register(FloatSerializer)
    }

    abstract fun createPrism(): ReferencePrism<*>

    @BeforeEach
    private fun preparePrism() {
        _prism = prism
    }
}