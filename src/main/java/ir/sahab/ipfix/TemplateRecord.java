package ir.sahab.ipfix;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a template record in IPFIX protocol.
 * Templates are extracted from massages that contains record sets of template records (with set Id 2)
 * Templates used to decode other type of records into a {@link GenericRecord} see
 * {@link TemplateRecord#decodeDataRecord(ByteBuffer, GenericRecord)}, they can decode records of a
 * record set with set Id equal to their template Id.
 */
public class TemplateRecord implements Record, Cloneable {

    private int templateId;
    private ArrayList<FieldSpecifier> fieldSpecifiers = new ArrayList<>();

    public TemplateRecord() {}

    public TemplateRecord(int templateId) throws IllegalArgumentException {
        if (templateId < 256 || templateId > 0xffff)
            throw new IllegalArgumentException("TemplateRecord identifier must be between 256 " +
                    "and 65535.");
        this.templateId = templateId;
    }

    public TemplateRecord addField(FieldSpecifier field) {
        fieldSpecifiers.add(field);
        return this;
    }

    public int getTemplateId() {
        return templateId;
    }

    @Override
    public int length() {
        // TemplateRecord has headers of length 4 bytes
        return 4 + fieldSpecifiers.stream().mapToInt(FieldSpecifier::getLength).sum();
    }

    @Override
    public int minimumLength() {
        // 8 bytes for headers.
        // Can have no field specifier.
        return 8;
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        // Write template Id in message header
        byteBuffer.putShort((short) templateId);

        // Write field count in message header
        byteBuffer.putShort((short) fieldSpecifiers.size());

        // Encode field specifiers and write them in template record
        for (FieldSpecifier fieldSpecifier: fieldSpecifiers)
            fieldSpecifier.encode(byteBuffer);
    }

    @Override
    public void decodeFrom(ByteBuffer messageBuffer) {
        // Read headers
        templateId = ByteBufferUtils.readUnsignedShort(messageBuffer);
        int fieldCount = ByteBufferUtils.readUnsignedShort(messageBuffer);

        // Read field specifiers
        for (int i = 0; i < fieldCount; i++) {
            addField(FieldSpecifier.decode(messageBuffer));
        }
    }

    public void decodeDataRecord(ByteBuffer messageBuffer, GenericRecord dataRecord) {
        for (FieldSpecifier fieldSpecifier : fieldSpecifiers)
            dataRecord.addFieldValue(fieldSpecifier.decodeValue(messageBuffer));
    }

    public int getMinimumLengthOfDefinedRecord() {
        return fieldSpecifiers.stream().mapToInt(FieldSpecifier::getMinimumLengthOfDefinedField)
                .sum();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TemplateRecord))
            return false;

        TemplateRecord otherTemp = (TemplateRecord) obj;

        return (templateId == otherTemp.templateId &&
                Objects.equals(fieldSpecifiers, otherTemp.fieldSpecifiers));
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + templateId;
        hashCode = 31 * hashCode + Objects.hashCode(fieldSpecifiers);
        return hashCode;
    }

    @Override
    public TemplateRecord clone() {
        TemplateRecord newRecord = new TemplateRecord(templateId);
        for (FieldSpecifier fieldSpecifier : fieldSpecifiers){
            newRecord.addField(fieldSpecifier.clone());
        }
        return newRecord;
    }
}