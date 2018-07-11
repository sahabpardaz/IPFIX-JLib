package ir.sahab.ipfix;

import java.nio.ByteBuffer;

/**
 * A class that provides utility methods for ByteBuffer
 */
public class ByteBufferUtils {

    private ByteBufferUtils() {}

    public static short readUnsignedByte(ByteBuffer byteBuffer) {
        return (short) (0xFF & (byteBuffer.get()));
    }

    public static int readUnsignedShort(ByteBuffer byteBuffer) {
        return 0xFFFF &  (byteBuffer.getShort());
    }

    public static long readUnsignedInt(ByteBuffer byteBuffer) {
        return 0xFFFFFFFFL & (byteBuffer.getInt());
    }
}
