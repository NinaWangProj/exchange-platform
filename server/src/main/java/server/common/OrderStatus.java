package server.common;

import commonData.DataType.OrderStatusType;

import java.util.ArrayList;

public class OrderStatus {
    private final ArrayList<String> statusMessages;
    private final OrderStatusType msgType;
    private final int orderID;

    public OrderStatus(int orderID, OrderStatusType msgType, ArrayList<String> statusMessages)
    {
        this.statusMessages = statusMessages;
        this.msgType = msgType;
        this.orderID = orderID;
    }

    public ArrayList<String> getStatusMessages() {
        return statusMessages;
    }

    public OrderStatusType getMsgType() {
        return msgType;
    }

    public int getOrderID() {
        return orderID;
    }
}
