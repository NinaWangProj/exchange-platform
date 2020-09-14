package commonData.DataType;

import java.util.HashMap;

public enum MessageType {
    ErrorMessage(1),
    SuccessMessage(2);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer, MessageType> map = new HashMap<Integer, MessageType>();

    private MessageType(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (MessageType type : MessageType.values()) {
            map.put(type.value, type);
        }
    }

    public static MessageType valueOf(int typeInteger) {
        return map.get(typeInteger);
    }

    public int getValue() {
        return value;
    }

    public byte getByteValue() {
        return byteValue;
    }
}
