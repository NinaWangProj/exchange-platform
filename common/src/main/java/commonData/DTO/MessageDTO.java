package commonData.DTO;

import commonData.DataType.MessageType;
import commonData.DataType.OrderStatusType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MessageDTO implements Transferable{
    private final DTOType dtoType;
    private final String message;
    private final MessageType msgType;
    private final long clientRequestID;

    public MessageDTO(long requestID, MessageType msgType, String message)
    {
        this.message = message;
        dtoType = DTOType.Message;
        this.msgType = msgType;
        clientRequestID = requestID;
    }

    public byte[] Serialize()
    {
        byte[] msgDTOByteArray = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] msgByteArray = message.getBytes();
            byte msgByteSize = (byte)msgByteArray.length;

            byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();

            outputStream.write(requestIDByteArray);
            outputStream.write(msgType.getByteValue());
            outputStream.write(msgByteSize);
            outputStream.write(msgByteArray);
            msgDTOByteArray = outputStream.toByteArray();

        } catch (Exception e) {

        }
        return msgDTOByteArray;
    }

    public static MessageDTO Deserialize(byte[] DTOByteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        MessageType msgTypeT = MessageType.valueOf(inputStream.read());
        int msgLength = inputStream.read();
        byte[] msgBuffer = new byte[msgLength];
        inputStream.read(msgBuffer, 0, msgLength);
        String msgT = new String(msgBuffer);

        MessageDTO DTO = new MessageDTO(requestIDT,msgTypeT,msgT);
        return DTO;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public long getClientRequestID() {
        return clientRequestID;
    }
}
