package nw.ExchangePlatform.data;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CredentialWareHouse {
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
        if(LoginCredentialMap.containsKey(userName)) {
            success = true;
        }

        //implement later; check password strength
        return success;
    }

    public boolean ValidateLogin(String userName, String password) {
        boolean pass = false;
        if(LoginCredentialMap.containsKey(userName)) {
            if(password == LoginCredentialMap.get(userName).getKey()) {
                pass = true;
            }
        }
        return pass;
    }

    public int GetUserID(String userName) {
        int userID = LoginCredentialMap.get(userName).getValue();
        return userID;
    }
}
