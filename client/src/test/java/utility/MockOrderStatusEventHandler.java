package utility;

import commonData.DataType.OrderStatusType;
import session.OrderStatusEventHandler;

public class MockOrderStatusEventHandler implements OrderStatusEventHandler {

    private long requestID;
    private OrderStatusType msgType;
    private String msg;

    public void On_ReceiveOrderStatusChange(long requestID, OrderStatusType msgType, String msg){
        this.requestID = requestID;
        this.msgType = msgType;
        this.msg = msg;
    }

    public long getRequestID() {
        return requestID;
    }

    public OrderStatusType getMsgType() {
        return msgType;
    }

    public String getMsg() {
        return msg;
    }
}
