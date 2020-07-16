package nw.ExchangePlatform.trading.data;

import java.util.HashMap;

public enum OrderDuration {
    DAY(1),
    GTC(2);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer,OrderDuration> map = new HashMap<Integer,OrderDuration>();

    private OrderDuration(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (OrderDuration duration : OrderDuration.values()) {
            map.put(duration.value, duration);
        }
    }

    public static OrderDuration valueOf(int durationInteger) {
        return map.get(durationInteger);
    }

    public int getValue() {
        return value;
    }

    public int getByteValue() {
        return byteValue;
    }
}
