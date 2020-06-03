package nw.ExchangePlatform.data;

import java.util.HashMap;

public enum DTOType {
    ORDER(1),
    CONFIG(2);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer,DTOType> map = new HashMap<Integer,DTOType>();

    private DTOType(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (DTOType type : DTOType.values()) {
            map.put(type.value, type);
        }
    }

    public static DTOType valueOf(int typeInteger) {
        return map.get(typeInteger);
    }

    public int getValue() {
        return value;
    }

    public int getByteValue() {
        return byteValue;
    }
}
