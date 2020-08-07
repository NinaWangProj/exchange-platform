package nw.ExchangePlatform.commonData.Order;

import java.util.HashMap;

public enum Direction {
    BUY(1),
    SELL(2);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer,Direction> map = new HashMap<Integer,Direction>();

    private Direction(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (Direction direction : Direction.values()) {
            map.put(direction.value, direction);
        }
    }

    public static Direction lookupEnumType(int directionInteger) {
        return map.get(directionInteger);
    }

    public int getValue() {
        return value;
    }

    public int getByteValue() {
        return byteValue;
    }
}
