package nw.ExchangePlatform.commonData;

import java.util.HashMap;

public enum MarketDataType {
    Level1(1),
    Level3(2),
    ContinuousLevel3(3);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer,MarketDataType> map = new HashMap<Integer,MarketDataType>();

    private MarketDataType(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (MarketDataType type : MarketDataType.values()) {
            map.put(type.value, type);
        }
    }

    public static MarketDataType valueOf(int typeInteger) {
        return map.get(typeInteger);
    }

    public int getValue() {
        return value;
    }

    public byte getByteValue() {
        return byteValue;
    }
}
