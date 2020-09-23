package commonData.DTO;

import commonData.limitOrderBook.BookOperation;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DepositDTOTest {

    @Test
    public void serializationDeserializationTest() throws Exception {
        long clientRequestID = 109;
        String tickerSymbol = "FB";
        double cashAmt = 262.3;

        DepositDTO dto = new DepositDTO(clientRequestID,cashAmt);

        byte[] dTOByteArray = dto.Serialize();

        DepositDTO deserializedDTO = DepositDTO.Deserialize(dTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(dto);
    }
}