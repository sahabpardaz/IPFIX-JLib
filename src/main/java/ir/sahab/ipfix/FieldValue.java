package ir.sahab.ipfix;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents value of a field of a {@link GenericRecord}
 */
public class FieldValue {
    private byte[] value;
    private boolean isVariableLength;

    /**
     * Create a field with given byte array as value.
     *
     * @param isVariableLength determine whether field of value is variable length or not.This value
     * must be passed based on type of field in relevant template.
     */
    public FieldValue(byte[] value, boolean isVariableLength) {
        this.value = value;
        this.isVariableLength = isVariableLength;
    }

    /**
     * Create a field with a byte value.
     *
     * @param isVariableLength determine whether field of value is variable length or not.This value
     * must be passed based on type of field in relevant template.
     */
    public FieldValue(byte value, boolean isVariableLength) {
        this.value = new byte[]{value};
        this.isVariableLength = isVariableLength;
    }

    /**
     * Create a field with a short value.
     *
     * @param isVariableLength determine whether field of value is variable length or not.This value
     * must be passed based on type of field in relevant template.
     */
    public FieldValue(short value, boolean isVariableLength) {
        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort(value);
        this.value = b.array();
        this.isVariableLength = isVariableLength;
    }

    /**
     * Create a field with a int value.
     *
     * @param isVariableLength determine whether field of value is variable length or not.This value
     * must be passed based on type of field in relevant template.
     */
    public FieldValue(int value, boolean isVariableLength) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(value);
        this.value = b.array();
        this.isVariableLength = isVariableLength;
    }

    /**
     * Create a field with a byte value.
     *
     * @param isVariableLength determine whether field of value is variable length or not.This value
     * must be passed based on type of field in relevant template.
     */
    public FieldValue(long value, boolean isVariableLength) {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putLong(value);
        this.value = b.array();
        this.isVariableLength = isVariableLength;
    }

    /**
     * Create a field with a string value.
     *
     * @param isVariableLength determine whether field of value is variable length or not.This value
     * must be passed based on type of field in relevant template.
     */
    public FieldValue(String value, boolean isVariableLength) {
        this.value = value.getBytes();
        this.isVariableLength = isVariableLength;
    }

    public byte[] getValue() {
        return value;
    }

    protected int getLength() {
        if (isVariableLength) {
             return value.length + (value.length < 256 ? 1 : 3);
        }
        return value.length;
    }

    protected void putValue(ByteBuffer byteBuffer) {
        byteBuffer.put(value);
    }

    public void encode(ByteBuffer byteBuffer) {
        // Write length of field if its length is variable
        if (isVariableLength){
            if(value.length < 256) {
                byteBuffer.put((byte) value.length);
            } else {
                byteBuffer.put((byte) 255);
                byteBuffer.putShort((short) value.length);
            }
        }

        // Write value of field in record
        putValue(byteBuffer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FieldValue))
            return false;

        FieldValue otherField = (FieldValue) obj;

        return isVariableLength == otherField.isVariableLength &&
               Arrays.equals(value, otherField.value);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (isVariableLength ? 1 : 0);
        hashCode = 31 * hashCode + Arrays.hashCode(value);
        return hashCode;
    }
}