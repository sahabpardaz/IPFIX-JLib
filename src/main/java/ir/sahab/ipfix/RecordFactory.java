package ir.sahab.ipfix;

import java.util.HashMap;
import java.util.Map;

/**
 * This class used to register templates and custom records, then instantiate raw records based on
 * template Id to fill in decode procedure.
 */
public class RecordFactory {
    private Map<Integer, Class<? extends Record>> customRecordMap = new HashMap<>();
    private Map<Integer, TemplateRecord> templateMap = new HashMap<>();

    public RecordFactory() {
        registerCustomRecord(2, TemplateRecord.class);
    }

    public Record newRawRecord(int templateId) {

        if (customRecordMap.containsKey(templateId)) {
            try {
                return customRecordMap.get(templateId).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (templateMap.containsKey(templateId)) {
            return new GenericRecord(templateMap.get(templateId));
        }

        throw new TemplateNotDefinedException("No template or custom record defined for " +
                "requested Id: " + templateId + '.');
    }

    public void registerCustomRecord(int templateId, Class<? extends Record> customClass) {
        try {
            customClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Custom record Implementation should have default " +
                    "constructor.", e);
        }
        customRecordMap.put(templateId, customClass);
    }

    public void registerGenericRecordType(TemplateRecord templateRecord) {
        templateMap.put(templateRecord.getTemplateId(), templateRecord.clone());
    }
}