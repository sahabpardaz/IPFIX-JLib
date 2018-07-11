package ir.sahab.ipfix;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * <p>
 *     This class is an example of custom record.
 * </p>
 * <p>
 *     Custom records let us decode record's fields to predefined types instead of byte arrays in
 *     generic decode mode performed by {@link GenericRecord}.
 * </p>
 */
public class ExampleRecord implements Record {

    private int exampleInt;
    private byte[] exampleArray;
    private final static int templateId = 1000;

    public static int getTemplateId() {
        return templateId;
    }

    public ExampleRecord() {}

    public ExampleRecord(int exampleInt, byte[] exampleArray) {
        this.exampleInt = exampleInt;
        this.exampleArray = exampleArray;
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.putInt(exampleInt);

        if (exampleArray.length < 255) {
            byteBuffer.put((byte) exampleArray.length);
        } else {
            byteBuffer.put((byte) 255);
            byteBuffer.putShort((short) exampleArray.length);
        }
        byteBuffer.put(exampleArray);
    }

    @Override
    public int length() {
        int length = 4; // Length of exampleInt

        if (exampleArray.length < 255) {
            length += 1;
        } else {
            length += 3;
        }
        length += exampleArray.length;

        return length;
    }

    @Override
    public int minimumLength() {
        // 4 bytes for exampleInt
        // 1 byte for length of variable length field 'exampleArray'
        return 4 + 1;
    }

    @Override
    public void decodeFrom(ByteBuffer byteBuffer) {
        exampleInt = byteBuffer.getInt();

        int someArrayLength = byteBuffer.get();
        if (someArrayLength == ((byte) 255)) {
            someArrayLength = byteBuffer.getShort();
        }
        exampleArray = new byte[someArrayLength];
        byteBuffer.get(exampleArray);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ExampleRecord))
            return false;

        ExampleRecord otherRecord = (ExampleRecord) obj;

        return exampleInt == otherRecord.exampleInt &&
               Arrays.equals(exampleArray, otherRecord.exampleArray);
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 31 * hashCode + exampleInt;
        hashCode = 31 * hashCode + Arrays.hashCode(exampleArray);
        return hashCode;
    }
}