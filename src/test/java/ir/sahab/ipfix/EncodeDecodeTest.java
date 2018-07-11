package ir.sahab.ipfix;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * These tests check decode and encode process of different types of records.
 */
public class EncodeDecodeTest {

    @Test
    public void testGenericRecord() throws Exception {
        RecordFactory recordFactory = new RecordFactory();
        // create a message contains a template record encode and decodeFrom it ,then compare
        // decoded object to original object.
        IpfixMessage templateMessage = createTemplateMessage();
        Assert.assertEquals(templateMessage, IpfixMessage.decode(
                templateMessage.encode(), recordFactory));

        // create a message with a record of type example record and encode it, then compare it with
        // the same message encoded by hands.
        Assert.assertTrue(Arrays.equals(createDataMessage().encode(), createDataMessageByHands()));

        // create a message with a record of type example record, encode and decodeFrom it ,then
        // compare decoded object to original object.
        IpfixMessage dataMessage = createDataMessage();
        Assert.assertEquals(dataMessage, IpfixMessage.decode(dataMessage.encode(), recordFactory));
    }

    @Test
    public void testCustomRecord() throws IOException {
        RecordFactory recordFactory = new RecordFactory();
        recordFactory.registerCustomRecord(ExampleRecord.getTemplateId(), ExampleRecord.class);
        // create a data message object containing a example custom record and encode it, then
        // compare it with the same message encoded by hands.
        Assert.assertTrue(Arrays.equals(
                createDataMessageWithCustomRecord().encode(), createDataMessageByHands()));

        // create a data message object containing a example custom record, encode and decodeFrom
        // it, then compare the original message object with the decoded one.
        IpfixMessage Message = createDataMessageWithCustomRecord();
        Assert.assertEquals(Message, IpfixMessage.decode(Message.encode(), recordFactory));
    }

    private static IpfixMessage createTemplateMessage() throws IOException {
        long enterpriseNumber = 11112222L;
        IpfixMessage ipfixMessage = new IpfixMessage(1, 1234567890L, 1234, 87654321);
        // Id of templateRecord set should be 2
        RecordSet<TemplateRecord> templateRecordSet = new RecordSet<>(2);
        ipfixMessage.addSet(templateRecordSet);

        // ExampleRecord templateRecord Id = 1000
        TemplateRecord templateRecord = new TemplateRecord(1000);
        templateRecordSet.addRecord(templateRecord);

        // exampleInt
        FieldSpecifier fieldSpecifier = new FieldSpecifier(1, 4, enterpriseNumber);
        templateRecord.addField(fieldSpecifier);

        // exampleArray (variable length)
        fieldSpecifier = new FieldSpecifier(2, enterpriseNumber);
        templateRecord.addField(fieldSpecifier);

        return ipfixMessage;
    }

    private static IpfixMessage createDataMessage() throws IOException {
        IpfixMessage ipfixMessage = new IpfixMessage(1, 1234567890L, 1234, 87654321);
        // Id for a set of ExampleRecords should be 1000
        RecordSet<GenericRecord> dataRecordSet = new RecordSet<>(1000);
        ipfixMessage.addSet(dataRecordSet);

        // Data record has no header
        GenericRecord genericDataRecord = new GenericRecord(1000);
        dataRecordSet.addRecord(genericDataRecord);

        // exampleInt
        FieldValue fieldValue = new FieldValue(1, false);
        genericDataRecord.addFieldValue(fieldValue);

        // exampleArray
        fieldValue = new FieldValue(new byte[5], true);
        genericDataRecord.addFieldValue(fieldValue);

        return ipfixMessage;
    }

    private static byte[] createDataMessageByHands() throws IOException {
        // 30 is the size of whole message
        ByteBuffer messageAsBytes = ByteBuffer.allocate(30);

        // write version number in message header
        messageAsBytes.putShort((short) 1);

        // write length in message header
        int length = 16 + 14;   // 16 is size of the message headers in octets
                                // 14 is size of the set
        messageAsBytes.putShort((short) length);

        // write export time in message header
        messageAsBytes.putInt(1234567890);

        // write sequence number in message header
        messageAsBytes.putInt((int) 1234L);

        // write observation domain in message header
        messageAsBytes.putInt((int) 87654321L);

        // write set Id in set header
        messageAsBytes.putShort((short) 1000);

        // write length in set header
        length = 4 + 10;    // 4 is size of the set headers in octets
                            // 10 is size of the fields
        messageAsBytes.putShort((short) length);

        // Write exampleInt
        messageAsBytes.putInt(1);

        // Write exampleArray after writing it's length
        byte[] exampleArray = new byte[5];
        messageAsBytes.put((byte) exampleArray.length);
        messageAsBytes.put(exampleArray);

        return messageAsBytes.array();
    }

    private static IpfixMessage createDataMessageWithCustomRecord() throws IOException {
        // Create message
        IpfixMessage ipfixMessage = new IpfixMessage(1, 1234567890L, 1234, 87654321);
        // Create set
        // Id for a set of exampleRecord should be 1000
        RecordSet<ExampleRecord> dataRecordSet = new RecordSet<>(1000);
        // Put set in message
        ipfixMessage.addSet(dataRecordSet);

        // Create exampleRecord bean and put it in record set
        dataRecordSet.addRecord(new ExampleRecord(1, new byte[5]));

        return ipfixMessage;
    }
}