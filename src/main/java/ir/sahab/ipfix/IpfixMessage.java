package ir.sahab.ipfix;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Represents a message in IPFIX protocol.
 * It contains 5 headers and zero or more {@link RecordSet}
 * <pre>
 * Headers:
 *          version number
 *          length
 *          export time
 *          sequence number
 *          observation domain Id
 * </pre>
 */
public class IpfixMessage {
    private final static int MAX_UNSIGNED_SHORT = 0xffff;
    private final static long MAX_UNSIGNED_INT = 0xffffffffL;

    private final int versionNum;
    private final long exportTime;
    private final long sequenceNum;
    private final long observationDomainId;
    private ArrayList<RecordSet> recordSets = new ArrayList<>();

    /**
     * @param versionNum must be less than 0xffff
     * @param exportTime must be less than 0xffffffffL
     * @param sequenceNum must be less than 0xffffffffL
     * @param observationDomainId must be less than 0xffffffffL
     * @throws IllegalArgumentException When parameters are out of range.
     */
    public IpfixMessage(int versionNum, long exportTime, long sequenceNum,
                        long observationDomainId) throws IllegalArgumentException{
        if (versionNum > MAX_UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Version number is out of range.");
        }
        if (exportTime > MAX_UNSIGNED_INT) {
            throw new IllegalArgumentException("Export time is out of range.");
        }
        if (sequenceNum > MAX_UNSIGNED_INT) {
            throw new IllegalArgumentException("Sequence number is out of range.");
        }
        if (observationDomainId > MAX_UNSIGNED_INT) {
            throw new IllegalArgumentException("Observation domain Id is out of range.");
        }

        this.versionNum = versionNum;
        this.exportTime = exportTime;
        this.sequenceNum = sequenceNum;
        this.observationDomainId = observationDomainId;
    }

    public IpfixMessage addSet(RecordSet recordSet) {
        recordSets.add(recordSet);
        return this;
    }

    public int getLength() {
        // Message has 16 bytes headers.
        return 16 + recordSets.stream().mapToInt(RecordSet::getLength).sum();
    }

    public ArrayList<RecordSet> getRecordSets() {
        return recordSets;
    }

    public void encode(ByteBuffer byteBuffer) {
        // Write version number in message header
        byteBuffer.putShort((short) versionNum);

        // Write length in message header
        byteBuffer.putShort((short) getLength());

        // Write export time in message header
        byteBuffer.putInt((int) exportTime);

        // write sequence number in message header
        byteBuffer.putInt((int) sequenceNum);

        // Write observation domain in message header
        byteBuffer.putInt((int) observationDomainId);

        // Encode and write recordSets in message
        for (RecordSet recordSet : recordSets) {
            recordSet.encode(byteBuffer);
        }
    }

    public byte[] encode() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getLength());
        encode(byteBuffer);
        return byteBuffer.array();

    }

    public static IpfixMessage decode(byte[] message, RecordFactory recordFactory) {

        ByteBuffer messageBuffer = ByteBuffer.wrap(message);
        return decode(messageBuffer, recordFactory);
    }

    public static IpfixMessage decode(ByteBuffer messageBuffer, RecordFactory recordFactory) {
        int startOfMessagePosition = messageBuffer.position();
        int endOfMessagePosition = startOfMessagePosition;
        try {
            startOfMessagePosition = messageBuffer.position();
            // Read headers
            int versionNum = ByteBufferUtils.readUnsignedShort(messageBuffer);
            int length = ByteBufferUtils.readUnsignedShort(messageBuffer);
            long exportTime = ByteBufferUtils.readUnsignedInt(messageBuffer);
            long sequenceNum = ByteBufferUtils.readUnsignedInt(messageBuffer);
            long observationDomainId = ByteBufferUtils.readUnsignedInt(messageBuffer);

            // must stop reading at this position
            endOfMessagePosition = startOfMessagePosition + length;

            IpfixMessage ipfixMessage = new IpfixMessage(versionNum, exportTime, sequenceNum,
                                                         observationDomainId);
            // Read sets and add them to IPFIX message
            while (messageBuffer.position() < endOfMessagePosition && messageBuffer.hasRemaining()) {
                ipfixMessage.addSet(RecordSet.decode(messageBuffer, recordFactory));
            }

            if (ipfixMessage.getLength() != length) {
                throw new IllegalArgumentException(
                    "Length in header doesn't match length of message.");
            }
            if (messageBuffer.position() != endOfMessagePosition) {
                throw new IllegalArgumentException("Length of the message does not match the " +
                                                   "payload.");
            }

            return ipfixMessage;
        } catch (BufferUnderflowException e) {
            throw new IllegalArgumentException("Invalid IPFIX message.", e);
        } finally {
            messageBuffer.position(endOfMessagePosition);
        }
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof IpfixMessage))
            return false;

        IpfixMessage otherMessage = (IpfixMessage) obj;

        return versionNum == otherMessage.versionNum &&
               exportTime == otherMessage.exportTime &&
               sequenceNum == otherMessage.sequenceNum &&
               observationDomainId == otherMessage.observationDomainId &&
               recordSets == null ?
               otherMessage.recordSets == null : recordSets.equals(otherMessage.recordSets);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + versionNum;
        hashCode = (int) (31 * hashCode + exportTime);
        hashCode = (int) (31 * hashCode + sequenceNum);
        hashCode = (int) (31 * hashCode + observationDomainId);
        hashCode = 31 * hashCode + (recordSets == null ? 0 : recordSets.hashCode());
        return hashCode;
    }
}