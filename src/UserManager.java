import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static UserManager instance;
    private final String USERS_FILE = "users.txt";
    private Map<String, String> users; // username, password

    private UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (FileNotFoundException e) {
            // File might not exist yet, which is fine for the first run
            System.out.println("users.txt not found. A new one will be created upon registration.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        users.put(username, password);
        saveUsers();
        return true;
    }

    public synchronized boolean loginUser(String username, String password) {
        loadUsers(); // Ensure we have the latest user data
        return users.containsKey(username) && users.get(username).equals(password);
    }

    // Optional: Method to store additional user info if needed later
    public synchronized void storeUserInfo(String username, String key, String value) {
        // This could be expanded to store more complex user objects or write to a different file/format
        // For now, let's assume we might want to add more details to a user-specific file or a more structured storage.
        // Example: "username_info.txt" could store key-value pairs.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_info.txt", true))) {
            writer.write(key + ":" + value);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Optional: Method to retrieve additional user info
    public synchronized String getUserInfo(String username, String key) {
        try (BufferedReader reader = new BufferedReader(new FileReader(username + "_info.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2 && parts[0].equals(key)) {
                    return parts[1];
                }
            }
        } catch (FileNotFoundException e) {
            // File might not exist
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
