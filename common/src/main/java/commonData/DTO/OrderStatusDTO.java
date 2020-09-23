package commonData.DTO;

import commonData.DataType.OrderStatusType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class OrderStatusDTO implements Transferable
{
    private final DTOType dtoType;
    private final String message;
    private final OrderStatusType statusType;
    private final long clientRequestID;

    public OrderStatusDTO(long requestID, OrderStatusType statusType, String message)
    {
        this.message = message;
        dtoType = DTOType.OrderStatus;
        this.statusType = statusType;
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
            outputStream.write(statusType.getByteValue());
            outputStream.write(msgByteSize);
            outputStream.write(msgByteArray);
            msgDTOByteArray = outputStream.toByteArray();

        } catch (Exception e) {

        }
        return msgDTOByteArray;
    }

    public static OrderStatusDTO Deserialize(byte[] DTOByteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        OrderStatusType msgTypeT = OrderStatusType.valueOf(inputStream.read());
        int msgLength = inputStream.read();
        byte[] msgBuffer = new byte[msgLength];
        inputStream.read(msgBuffer, 0, msgLength);
        String msgT = new String(msgBuffer);

        OrderStatusDTO DTO = new OrderStatusDTO(requestIDT,msgTypeT,msgT);
        return DTO;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public String getMessage() {
        return message;
    }

    public OrderStatusType getStatusType() {
        return statusType;
    }

    public long getClientRequestID() {
        return clientRequestID;
    }
}
