package dev.thecodewarrior.prism.format.reference.builtin.auto;

import dev.thecodewarrior.prism.annotation.Refract;
import dev.thecodewarrior.prism.annotation.RefractClass;
import dev.thecodewarrior.prism.annotation.RefractGetter;
import dev.thecodewarrior.prism.annotation.RefractSetter;

import java.util.List;

@RefractClass
public class AutoSerializedType {
    // Prism can access private fields
    @Refract private List<String> plainField;

    private int accessorValue;
    private String accessorString;

    // Prism understands getters and setters
    @RefractGetter("accessorValue")
    public int getAccessorValue() {
        return this.accessorValue;
    }
    @RefractSetter("accessorValue")
    public void setAccessorValue(int value) {
        this.accessorValue = value;
        this.accessorString = "Value is: " + value;
    }

    // Prism will automatically detect when immutable properties' values have changed...
    @Refract private final int finalField;
    // ...and will detect any constructors it can use to create a new instance with the
    // changed values. Once it has created the new instance it will continue and set any
    // mutable properties that weren't set in the constructor.
    public AutoSerializedType(int finalField) {
        this.finalField = finalField;
    }
}
