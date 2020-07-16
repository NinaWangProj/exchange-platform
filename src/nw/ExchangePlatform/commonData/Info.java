package nw.ExchangePlatform.commonData;

import nw.ExchangePlatform.trading.data.Direction;

import java.util.Date;

public interface Info {

    int getSessionID();
    int getUserID();
    String getName();
    int getOrderID();
    Date getTime();
    Direction getDirection();
    String getTickerSymbol();
    int getSize();
    double getPrice();
    String getReason();
}
