package ir.sahab.ipfix;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a general data record in IPFIX protocol.
 * It contains several {@link FieldValue}.
 */
public class GenericRecord implements Record {
    private final int templateId;
    private TemplateRecord templateRecord;
    private ArrayList<FieldValue> fieldValues = new ArrayList<>();

    GenericRecord(int templateId) {
        this.templateId = templateId;
    }

    GenericRecord(TemplateRecord templateRecord) {
        this.templateRecord = templateRecord;
        templateId = templateRecord.getTemplateId();
    }

    void addFieldValue(FieldValue fieldValue) {
        fieldValues.add(fieldValue);
    }

    @Override
    public int length() {
        return fieldValues.stream().mapToInt(FieldValue::getLength).sum();
    }

    @Override
    public int minimumLength() {
        return templateRecord.getMinimumLengthOfDefinedRecord();
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        for(FieldValue fieldValue: fieldValues) {
            fieldValue.encode(byteBuffer);
        }
    }

    @Override
    public void decodeFrom(ByteBuffer messageBuffer) {
        templateRecord.decodeDataRecord(messageBuffer, this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof GenericRecord))
            return false;

        GenericRecord otherRec = (GenericRecord) obj;

        return templateId == otherRec.templateId &&
               Objects.equals(fieldValues, otherRec.fieldValues);
    }

    @Override
    public int hashCode() {
        int hashCode = templateId;
        hashCode = 31 * hashCode + Objects.hashCode(fieldValues);
        return hashCode;
    }
}