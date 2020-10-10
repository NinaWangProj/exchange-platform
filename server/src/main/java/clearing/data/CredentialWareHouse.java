package clearing.data;

import javafx.util.Pair;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CredentialWareHouse {
    //<userName,<passWord,userID>>
    private HashMap<String, Pair<String,Integer>> LoginCredentialMap;
    private AtomicInteger currentAvailableMaxUserID;

    public CredentialWareHouse(int currentAvailableMaxUserID) {
        this.currentAvailableMaxUserID = new AtomicInteger(currentAvailableMaxUserID);
        LoginCredentialMap = new HashMap<String, Pair<String,Integer>>();
    }

    public boolean CreateAccount(String userName, String password) {
        //need to check if userName exits already & password Strength;
        boolean success = ValidateNewAcctInfo(userName,password);

        if(success) {
            int userID = currentAvailableMaxUserID.getAndIncrement();
            LoginCredentialMap.put(userName, new Pair<String, Integer>(password, userID));
        }
        return success;
    }

    private boolean ValidateNewAcctInfo(String userName, String password) {
        boolean success = false;
        if(!LoginCredentialMap.containsKey(userName)) {
            success = true;
        }

        //implement later; check password strength
        return success;
    }

    public boolean ValidateLogin(String userName, String password) {
        boolean pass = false;
        if(LoginCredentialMap.containsKey(userName)) {
            if(password.equals(LoginCredentialMap.get(userName).getKey())) {
                pass = true;
            }
        }
        return pass;
    }

    public int GetUserID(String userName) {
        int userID = LoginCredentialMap.get(userName).getValue();
        return userID;
    }

    public HashMap<String, Pair<String, Integer>> getLoginCredentialMap() {
        return LoginCredentialMap;
    }

    public void WriteToCSVFile(OutputStream outputStream) {
        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
        try (ICsvListWriter listWriter = new CsvListWriter(outputWriter, CsvPreference.STANDARD_PREFERENCE)){
            int count = 0;
            // write the header
            final String[] header = new String[] { "UserName", "Password", "UserID", "currentAvailableMaxUserID"};
            listWriter.writeHeader(header);

            for (Map.Entry<String, Pair<String,Integer>> entry : LoginCredentialMap.entrySet()){
                if(count == 0) {
                    listWriter.write(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue(),currentAvailableMaxUserID);
                }
                listWriter.write(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue());
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
