package session;


import commonData.DataType.OrderStatusType;

public interface OrderStatusEventHandler {

    public void On_ReceiveOrderStatusChange(long requestID, OrderStatusType msgType, String msg) throws Exception;
}
