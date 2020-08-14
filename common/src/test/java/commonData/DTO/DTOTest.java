package commonData.DTO;

import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


public class DTOTest {

    @Test
    public void orderDTOTest() {
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 101;
        Direction direction = Direction.BUY;
        OrderType type = OrderType.MARKETORDER;
        String tickerSymbol = "AAPL";
        int size = 1000;
        double price = -1;
        OrderDuration duration = OrderDuration.DAY;

        OrderDTO orderDTO = new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);

        byte[] orderDTOByteArray = orderDTO.Serialize();

        OrderDTO deserializedDTO = OrderDTO.Deserialize(orderDTOByteArray);

        assertThat(deserializedDTO).isEqualToComparingFieldByField(orderDTO);

        int a = 0;
    }
}