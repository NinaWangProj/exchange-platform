package nw.ExchangePlatform.commonData.DTO;

import nw.ExchangePlatform.commonData.DataType.OrderStatusType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MessageDTO implements Transferable
{
    private final DTOType dtoType;
    private final String message;
    private final OrderStatusType msgType;
    private byte[] msgDTOByteArray;
    private final long clientRequestID;

    public MessageDTO(long requestID, OrderStatusType msgType, String message)
    {
        this.message = message;
        dtoType = DTOType.Message;
        this.msgType = msgType;
        clientRequestID = requestID;
    }

    public byte[] Serialize()
    {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] msgByteArray = message.getBytes();
            byte msgByteSize = (byte)msgByteArray.length;

            byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
            outputStream.write(requestIDByteArray);

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

        OrderStatusType msgTypeT = OrderStatusType.valueOf(inputStream.read());
        int msgLength = inputStream.read();
        byte[] msgBuffer = new byte[msgLength];
        inputStream.read(msgBuffer, 0, msgLength);
        String msgT = new String(msgBuffer);

        MessageDTO DTO = new MessageDTO(requestIDT,msgTypeT,msgT);
        return DTO;
    }

    @Override
    public DTOType getDtoType() {
        return dtoType;
    }

    public String getMessage() {
        return message;
    }

    public OrderStatusType getMsgType() {
        return msgType;
    }

    public long getClientRequestID() {
        return clientRequestID;
    }
}
