package nw.ExchangePlatform.commonData.DTO;

public class MessageDTO implements Transferable
{
    private final DTOType dtoType;
    private final String message;

    public MessageDTO(String message)
    {
        this.message = message;
        dtoType = DTOType.Message;
    }

    public byte[] Serialize()
    {
        byte[] messageDTO = message.getBytes();
        return messageDTO;
    }

    public static MessageDTO Deserialize(byte[] DTOByteArray) {
        String message = new String(DTOByteArray);
        MessageDTO DTO = new MessageDTO(message);
        return DTO;
    }

    @Override
    public DTOType getDtoType() {
        return dtoType;
    }

    public String getMessage() {
        return message;
    }
}
