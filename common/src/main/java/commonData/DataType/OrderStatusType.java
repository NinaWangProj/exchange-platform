package commonData.DataType;

import java.util.HashMap;

public enum OrderStatusType {
    Pending(1),
    PartiallyFilled(2),
    Unfilled(3),
    Deposit(4),
    MarketOrderComplete(5);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer, OrderStatusType> map = new HashMap<Integer, OrderStatusType>();

    private OrderStatusType(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (OrderStatusType type : OrderStatusType.values()) {
            map.put(type.value, type);
        }
    }

    public static OrderStatusType valueOf(int typeInteger) {
        return map.get(typeInteger);
    }

    public int getValue() {
        return value;
    }

    public byte getByteValue() {
        return byteValue;
    }
}
