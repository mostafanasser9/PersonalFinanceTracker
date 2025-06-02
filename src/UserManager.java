import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static UserManager instance;
    private final String USERS_FILE = "users.txt";
    private final Map<String, String> users; // email -> name:password
    private final Map<String, String> userRoles; // email -> role
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
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println(USERS_FILE + " not found. A new one will be created upon registration.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 4); 
                if (parts.length >= 3) { 
                    String name = parts[0];
                    String email = parts[1];
                    String password = parts[2];
                    users.put(email, name + ":" + password);
                    if (parts.length == 4) {
                        userRoles.put(email, parts[3]);
                    } else {
                        userRoles.put(email, DEFAULT_ROLE);
                    }
                }
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                String email = entry.getKey();
                String nameAndPassword = entry.getValue(); // "name:password"
                
                String name;
                String password;
                int colonIndex = nameAndPassword.indexOf(':');
                if (colonIndex != -1) {
                    name = nameAndPassword.substring(0, colonIndex);
                    password = nameAndPassword.substring(colonIndex + 1);
                } else {
                    // This case implies only name was stored, or an error in data format
                    name = nameAndPassword; 
                    password = ""; // Default to empty password if format is unexpected
                }
                
                String role = userRoles.getOrDefault(email, DEFAULT_ROLE);
                writer.write(name + ":" + email + ":" + password + ":" + role);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    public synchronized boolean registerUser(String name, String email, String password) {
        if (users.containsKey(email)) {
            return false; 
        }
        users.put(email, name + ":" + password);
        userRoles.put(email, DEFAULT_ROLE); 
        saveUsers();
        return true;
    }

    public synchronized boolean loginUser(String email, String password) {
        String storedUserDetails = users.get(email);
        if (storedUserDetails != null) {
            // storedUserDetails is "name:password"
            int colonIndex = storedUserDetails.indexOf(':');
            if (colonIndex != -1) {
                String storedPassword = storedUserDetails.substring(colonIndex + 1);
                return storedPassword.equals(password);
            }
        }
        return false; 
    }

    public synchronized String getUserName(String email) {
        String storedUserDetails = users.get(email);
        if (storedUserDetails != null) {
            // storedUserDetails is "name:password"
            int colonIndex = storedUserDetails.indexOf(':');
            if (colonIndex != -1) {
                return storedUserDetails.substring(0, colonIndex);
            }
            // If no colon, implies only name was stored (unlikely with current registerUser)
            return storedUserDetails; 
        }
        return null; 
    }

    public synchronized String getUserRole(String email) { 
        return userRoles.getOrDefault(email, DEFAULT_ROLE);
    }

    public synchronized void setUserRole(String email, String role) { 
        if (users.containsKey(email)) {
            userRoles.put(email, role);
            saveUsers();
        }
    }

    public synchronized boolean isPremiumUser(String email) { 
        return PREMIUM_ROLE.equals(getUserRole(email));
    }

    // Changed 'username' parameter to 'email' for consistency
    public synchronized void storeUserInfo(String email, String key, String value) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(email + "_info.txt", true))) {
            writer.write(key + ":" + value);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Changed 'username' parameter to 'email' for consistency
    public synchronized String getUserInfo(String email, String key) {
        File file = new File(email + "_info.txt");
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2 && parts[0].equals(key)) {
                    return parts[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
