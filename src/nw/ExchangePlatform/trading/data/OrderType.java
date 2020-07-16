package nw.ExchangePlatform.trading.data;

import java.util.HashMap;

public enum OrderType {
    MARKETORDER(1),
    LIMITORDER(2);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer,OrderType> map = new HashMap<Integer,OrderType>();

    private OrderType(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (OrderType type : OrderType.values()) {
            map.put(type.value, type);
        }
    }

    public static OrderType valueOf(int typeInteger) {
        return map.get(typeInteger);
    }

    public int getValue() {
        return value;
    }

    public int getByteValue() {
        return byteValue;
    }
}
