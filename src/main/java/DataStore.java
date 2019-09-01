import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static Map<UUID, Account> accounts = new ConcurrentHashMap<>();

    private static Map<UUID, Transfer> transfers = new ConcurrentHashMap<>();

    private DataStore(){

    }

    public static Map<UUID, Account> getAccounts() {
        return accounts;
    }

    public static Map<UUID, Transfer> getTransfers() {
        return transfers;
    }
}
