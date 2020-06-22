package nw.ExchangePlatform.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessageDTO
{
    private final String message;

    public MessageDTO(String message)
    {
        this.message = message;
    }

    public byte[] Serialize()
    {
        byte[] messageDTO = message.getBytes();
        return messageDTO;
    }

    public static String Deserialize(byte[] DTOByteArray) {
        String message = new String(DTOByteArray);
        return message;
    }
}
