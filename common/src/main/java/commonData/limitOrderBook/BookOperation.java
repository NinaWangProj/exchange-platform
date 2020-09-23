package commonData.limitOrderBook;

import java.util.HashMap;

public enum BookOperation {
    REMOVE(1),
    INSERT(2),
    MODIFY(3);

    private final int value;
    private final byte byteValue;
    private static final HashMap<Integer,BookOperation> map = new HashMap<Integer,BookOperation>();

    private BookOperation(int value) {
        this.value = value;
        this.byteValue = (byte)value;
    }

    //static initialisation block; only run this method once when the class is first loaded;
    static {
        for (BookOperation operation : BookOperation.values()) {
            map.put(operation.value, operation);
        }
    }

    public static BookOperation valueOf(int opeartaionInteger) {
        return map.get(opeartaionInteger);
    }

    public int getValue() {
        return value;
    }

    public int getByteValue() {
        return byteValue;
    }
}
