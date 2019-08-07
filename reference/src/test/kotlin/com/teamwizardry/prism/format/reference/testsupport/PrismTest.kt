package com.teamwizardry.prism.format.reference.testsupport

import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.builtin.ByteSerializer
import com.teamwizardry.prism.format.reference.builtin.CharSerializer
import com.teamwizardry.prism.format.reference.builtin.DoubleSerializer
import com.teamwizardry.prism.format.reference.builtin.FloatSerializer
import com.teamwizardry.prism.format.reference.builtin.IntSerializer
import com.teamwizardry.prism.format.reference.builtin.LongSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveByteSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveCharSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveDoubleSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveFloatSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveIntSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveLongSerializer
import com.teamwizardry.prism.format.reference.builtin.PrimitiveShortSerializer
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
        prism.register(PrimitiveLongSerializer)
        prism.register(PrimitiveIntSerializer)
        prism.register(PrimitiveShortSerializer)
        prism.register(PrimitiveByteSerializer)
        prism.register(PrimitiveCharSerializer)
        prism.register(PrimitiveDoubleSerializer)
        prism.register(PrimitiveFloatSerializer)
    }

    abstract fun createPrism(): ReferencePrism<*>

    @BeforeEach
    private fun preparePrism() {
        _prism = createPrism()
    }
}