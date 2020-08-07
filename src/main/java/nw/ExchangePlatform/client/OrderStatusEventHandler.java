package nw.ExchangePlatform.client;


import nw.ExchangePlatform.commonData.DataType.OrderStatusType;

public interface OrderStatusEventHandler {

    public void On_ReceiveOrderStatusChange(long requestID, OrderStatusType msgType, String msg);
}
