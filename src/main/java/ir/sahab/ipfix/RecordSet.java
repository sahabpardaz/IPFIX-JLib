package ir.sahab.ipfix;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of records in an IPFIX message.
 * It has two headers and zero or more records.
 * Headers:
 *          set Id    2 bytes
 *          length    2 bytes
 *
 * @param <T> type of {@link Record} that set contains.
 */
public class RecordSet<T extends Record> {
    private int setId;
    private ArrayList<T> records = new ArrayList<>();

    /**
     * @param setId must be less than 0xffff
     * @throws IllegalArgumentException
     */
    public RecordSet(int setId) throws IllegalArgumentException {
        if (setId > 0xffff) {
            throw new IllegalArgumentException("setId must be less than 0xffff");
        }
        this.setId = setId;
    }

    public boolean isTemplateSet() {
        return setId == 2;
    }

    public int getSetId() {
        return setId;
    }

    public <X extends T> RecordSet<T> addRecord(X record) {
        records.add(record);
        return this;
    }

    public int getLength() {
        // headers of 4 bytes length
        return 4 + records.stream().mapToInt(Record::length).sum();
    }

    public List<T> getRecords() {
        return records;
    }

    public void encode(ByteBuffer byteBuffer) {
        // Write headers.
        byteBuffer.putShort((short) setId);
        byteBuffer.putShort((short) getLength());

        // Encode records and write them in set
        records.forEach(record -> record.encode(byteBuffer));
    }

    public static RecordSet<Record> decode(ByteBuffer messageBuffer, RecordFactory recordFactory) {

        int startPosition = messageBuffer.position();

        // Read headers
        int setId = ByteBufferUtils.readUnsignedShort(messageBuffer);
        int length = ByteBufferUtils.readUnsignedShort(messageBuffer);

        // must avoid read this position
        int endOfSetPosition = startPosition + length;

        RecordSet<Record> recordSet = new RecordSet<>(setId);

        Record rawRecord = recordFactory.newRawRecord(setId);

        while (endOfSetPosition - messageBuffer.position() >= rawRecord.minimumLength()) {
            rawRecord.decodeFrom(messageBuffer);
            recordSet.addRecord(rawRecord);

            // Add templates to record factory
            if (setId == 2) {
                recordFactory.registerGenericRecordType((TemplateRecord) rawRecord);
            }
        }

        // Checks to avoid read bytes out of set's boundary
        if (messageBuffer.position() > endOfSetPosition) {
            throw new IllegalArgumentException("Invalid IPFIX message.");
        }
        // To pass padding.
        if (messageBuffer.position() != endOfSetPosition) {
            messageBuffer.position(endOfSetPosition);
        }
        return recordSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RecordSet))
            return false;

        RecordSet otherSet = (RecordSet) obj;

        if (setId == otherSet.setId &&
            records == null ? otherSet.records == null : records.equals(otherSet.records)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + setId;
        hashCode = 31 * hashCode + (records == null ? 0 : records.hashCode());
        return hashCode;
    }
}