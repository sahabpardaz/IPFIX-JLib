package ir.sahab.ipfix;

import java.nio.ByteBuffer;

/**
 * <pre>
 * Represents a field specifier in IPFIX protocol
 * It contains three part:
 *                          element Id
 *                          length
 *                          enterprise number
 *
 * first bit of element Id is enterprise bit
 * </pre>
 */
public class FieldSpecifier implements Cloneable {
    private final static int MAX_UNSIGNED_SHORT = 0xffff;
    private final static long MAX_UNSIGNED_INT = 0xffffffffL;
    private final static int ELEMENT_ID_THRESHOLD = 0x7fff;
    private final static int ENTERPRISE_SIGN = 0x8000;

    private int elementId;
    private int length;
    private long enterpriseNum;

    /**
     * @param elementId must be less than 0xffff
     * @param length must be less than 0xffff
     * @param enterpriseNum must be less than 0xffffffffL
     * @throws IllegalArgumentException when parameters are out of range.
     */
    public FieldSpecifier(int elementId, int length, long enterpriseNum)
        throws IllegalArgumentException {
        if (elementId > MAX_UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Element Id is out of range.");
        }
        if (length > MAX_UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Length is out of range.");
        }
        if (enterpriseNum > MAX_UNSIGNED_INT) {
            throw new IllegalArgumentException("Enterprise num is out of range.");
        }
        if (elementId < ENTERPRISE_SIGN) {
            elementId += ENTERPRISE_SIGN;
        }
        this.elementId = elementId;
        this.length = length;
        this.enterpriseNum = enterpriseNum;
    }

    /**
     * Creates a non-enterprise field specifier.
     *
     * @param elementId must be less than 0xffff
     * @param length must be less than 0xffff
     * @throws IllegalArgumentException
     */
    public FieldSpecifier(int elementId, int length) throws IllegalArgumentException {
        if (elementId > MAX_UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Element Id is out of range.");
        }
        if (length > MAX_UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Length is out of range.");
        }
        this.elementId = elementId;
        this.length = length;
    }

    /**
     * Creates a variable length field specifier.
     *
     * @param elementId must be less than 0xffff
     * @param enterpriseNum must be less than 0xffffffffL
     * @throws IllegalArgumentException
     */
    public FieldSpecifier(int elementId, long enterpriseNum) throws IllegalArgumentException {
       this(elementId, MAX_UNSIGNED_SHORT, enterpriseNum);
    }

    /**
     * Creates a non-enterprise variable length field specifier.
     *
     * @param elementId must be less than 0xffff
     * @throws IllegalArgumentException
     */
    public FieldSpecifier(int elementId) throws IllegalArgumentException {
       this(elementId, MAX_UNSIGNED_SHORT);
    }

    public int getLength() {
        return elementId > ELEMENT_ID_THRESHOLD ? 8 : 4;
    }

    public int getMinimumLengthOfDefinedField() {
        return length == MAX_UNSIGNED_SHORT ? 1 : length;
    }

    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.putShort((short) elementId);
        byteBuffer.putShort((short) length);

        // Write enterprise number if enterprise bit is true
        if (elementId > ELEMENT_ID_THRESHOLD) {
            byteBuffer.putInt((int) enterpriseNum);
        }
    }

    public static FieldSpecifier decode(ByteBuffer messageBuffer) {
        int elementId = ByteBufferUtils.readUnsignedShort(messageBuffer);
        int length = ByteBufferUtils.readUnsignedShort(messageBuffer);
        if (elementId > ELEMENT_ID_THRESHOLD){
            long enterpriseNum = ByteBufferUtils.readUnsignedInt(messageBuffer);
            return new FieldSpecifier(elementId, length, enterpriseNum);
        } else {
            return new FieldSpecifier(elementId, length);
        }
    }

    /**
     * Decode a {@link FieldValue} of type this {@link FieldSpecifier} defines.
     *
     * @param messageBuffer buffer to read bytes from
     * @return decoded {@link FieldValue}
     */
    public FieldValue decodeValue(ByteBuffer messageBuffer) {
        // Checks if the field is variable length
        if (length == 65535) {
            int len = ByteBufferUtils.readUnsignedByte(messageBuffer);
            // Checks if length is more than 254 bytes
            if (len == 255) {
                len = ByteBufferUtils.readUnsignedShort(messageBuffer);
            }
            byte[] value = new byte[len];
            messageBuffer.get(value);
            return new FieldValue(value, true);
        } else {
            byte[] value = new byte[length];
            messageBuffer.get(value);
            return new FieldValue(value, false);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FieldSpecifier))
            return false;

        FieldSpecifier otherField = (FieldSpecifier) obj;

        return (elementId == otherField.elementId &&
                length == otherField.length &&
                enterpriseNum == otherField.enterpriseNum);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + elementId;
        hashCode = 31 * hashCode + length;
        hashCode = (int) (31 * hashCode + enterpriseNum);
        return hashCode;
    }

    public FieldSpecifier clone() {
        return new FieldSpecifier(elementId, length, enterpriseNum);
    }
}