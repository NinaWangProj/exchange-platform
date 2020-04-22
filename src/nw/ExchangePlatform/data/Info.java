package nw.ExchangePlatform.data;

import java.util.Date;

public interface Info {

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
