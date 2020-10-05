package common;

import commonData.DataType.OrderStatusType;

public class OrderStatus {
    private final String statusMessage;
    private final OrderStatusType msgType;
    private final Integer orderID;

    public OrderStatus(Integer orderID, OrderStatusType msgType, String statusMessage)
    {
        this.statusMessage = statusMessage;
        this.msgType = msgType;
        this.orderID = orderID;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public OrderStatusType getMsgType() {
        return msgType;
    }

    public Integer getOrderID() {
        return orderID;
    }
}
