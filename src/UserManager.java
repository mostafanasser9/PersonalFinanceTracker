import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static UserManager instance;
    private final String USERS_FILE = "users.txt";
    private Map<String, String> users; // username, password
    private Map<String, String> userRoles; // username, role (e.g., "standard", "premium")
    private static final String DEFAULT_ROLE = "standard";
    private static final String PREMIUM_ROLE = "premium";


    private UserManager() {
        users = new HashMap<>();
        userRoles = new HashMap<>();
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
                String[] parts = line.split(":", 3); // Split into 3 parts for username, password, and role
                if (parts.length >= 2) { // Existing users might not have a role yet
                    users.put(parts[0], parts[1]);
                    if (parts.length == 3) {
                        userRoles.put(parts[0], parts[2]);
                    } else {
                        userRoles.put(parts[0], DEFAULT_ROLE); // Default role for users without one
                    }
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
                String role = userRoles.getOrDefault(entry.getKey(), DEFAULT_ROLE);
                writer.write(entry.getKey() + ":" + entry.getValue() + ":" + role);
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
        userRoles.put(username, DEFAULT_ROLE); // New users start with a default role
        saveUsers();
        return true;
    }

    public synchronized boolean loginUser(String username, String password) {
        loadUsers(); // Ensure we have the latest user data
        return users.containsKey(username) && users.get(username).equals(password);
    }

    public synchronized String getUserRole(String username) {
        return userRoles.getOrDefault(username, DEFAULT_ROLE);
    }

    public synchronized void setUserRole(String username, String role) {
        if (users.containsKey(username)) {
            userRoles.put(username, role);
            saveUsers();
        }
    }

    public synchronized boolean isPremiumUser(String username) {
        return PREMIUM_ROLE.equals(getUserRole(username));
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
