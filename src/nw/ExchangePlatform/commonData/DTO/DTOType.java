package nw.ExchangePlatform.commonData.DTO;

import java.util.HashMap;

public enum DTOType {
    Order(1),
    Config(2),
    Message(3),
    OpenAcctRequest(4),
    LoginRequest(5),
    MareketDataRequest(6),
    MarketData(7),
    BookChangesDTO(8),
    MarketDataItem(9),
    DepositRequest(10),
    PortfolioRequest(11),
    Portfolio(12);

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
