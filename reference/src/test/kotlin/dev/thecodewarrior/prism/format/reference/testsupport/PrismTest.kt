package dev.thecodewarrior.prism.format.reference.testsupport

import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.builtin.BooleanSerializer
import dev.thecodewarrior.prism.format.reference.builtin.ByteSerializer
import dev.thecodewarrior.prism.format.reference.builtin.CharSerializer
import dev.thecodewarrior.prism.format.reference.builtin.DoubleSerializer
import dev.thecodewarrior.prism.format.reference.builtin.FloatSerializer
import dev.thecodewarrior.prism.format.reference.builtin.IntSerializer
import dev.thecodewarrior.prism.format.reference.builtin.LongSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveBooleanSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveByteSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveCharSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveDoubleSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveFloatSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveIntSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveLongSerializer
import dev.thecodewarrior.prism.format.reference.builtin.PrimitiveShortSerializer
import dev.thecodewarrior.prism.format.reference.builtin.ShortSerializer
import dev.thecodewarrior.prism.format.reference.builtin.StringSerializer
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
        prism.register(BooleanSerializer)
        prism.register(PrimitiveLongSerializer)
        prism.register(PrimitiveIntSerializer)
        prism.register(PrimitiveShortSerializer)
        prism.register(PrimitiveByteSerializer)
        prism.register(PrimitiveCharSerializer)
        prism.register(PrimitiveDoubleSerializer)
        prism.register(PrimitiveFloatSerializer)
        prism.register(PrimitiveBooleanSerializer)
    }

    abstract fun createPrism(): ReferencePrism<*>

    @BeforeEach
    private fun preparePrism() {
        _prism = createPrism()
    }
}