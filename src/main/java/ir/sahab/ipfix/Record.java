package ir.sahab.ipfix;

import java.nio.ByteBuffer;

/**
 * This interface represents records that placed in {@link RecordSet}.
 * It can be a template record or just an ordinary record.
 * <ul>
 * <li>Template records will be recoded into a {@link TemplateRecord}.</li>
 * <li>Ordinary record can be decoded by a {@link TemplateRecord} into a {@link GenericRecord} or decoded
 * by an specific Implementation of {@link Record} that represents specific type of record.</li>
 * </ul>
 */
public interface Record {

    void encode(ByteBuffer byteBuffer);

    /**
     * Decodes data from byteBuffer as needed to fill this record.
     */
    void decodeFrom(ByteBuffer messageBuffer);

    /**
     * @return length of this record when it encoded as byte array.
     */
    int length();

    /**
     * @return minimum possible length of this type of record.
     */
    int minimumLength();
}