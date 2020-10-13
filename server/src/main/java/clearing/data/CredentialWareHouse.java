package clearing.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import javafx.util.Pair;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    public void WriteToCSVFile2(OutputStream outputStream) {
        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
        List<CredentialRow> credentials = LoginCredentialMap.entrySet().stream().map(e ->
                new CredentialRow(e.getKey(), e.getValue().getKey(), e.getValue().getValue())).collect(Collectors.toList());

        StatefulBeanToCsv credentialsToCsv = new StatefulBeanToCsvBuilder(outputWriter).build();

        try {
            credentialsToCsv.write(credentials);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void WriteToJSONString(OutputStream outputStream) {
        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        try {
            String jsonStr = JSONObjectMapper.writeValueAsString(this);
            outputWriter.write(jsonStr);
            //JSONObjectMapper.writeValue(outputStream,this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CredentialWareHouse BuildFromJSON(InputStream inputStream) {
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        CredentialWareHouse credentialWareHouse = null;
        try {
            credentialWareHouse = JSONObjectMapper.readValue(inputStream,CredentialWareHouse.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return credentialWareHouse;
    }

}
