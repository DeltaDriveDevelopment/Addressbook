package com.magneticconfoundry.databaseUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.sortedset.ZAddParams;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * Created by Tyler on 5/25/2017.
 *
 * Handles all communication between the application and the database
 */


public class Redis {
    private static final String authKey = "SometimesImakethingsweirdonpurpose";
    private static Jedis jedis = new Jedis("localhost");

    /*
    Since apparently exposing a redis server directly to the internet is incredibly insecure,
    I put a tiny amount of protection on it in the form of an insecure password that is (poorly)
    stored as plain text within the code. This isn't how one should secure a redis server, but
    it's easy enough and the machine running the server has little to nothing to lose.
     */
    private static void auth() {
            String ans = jedis.auth(authKey);
    }

    /**
     * Attempts to log in with username/password
     * @param username
     * @param password
     * @return true if successful, false otherwise
     */
    public static boolean login(String username, String password) {
        auth();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        //Change the byte[] from MessageDigest into a string
        String hashPass = Base64.getEncoder().encodeToString(digest.digest(password.getBytes()));
        if(jedis.hexists("user:" + username, "password")) {
            return (jedis.hget("user:" + username, "password").equals(hashPass));
        } else {
            return false;
        }
    }

    /**
     *
     * @param username
     * @param password
     * @return true if successful, false if username is taken
     */
    public static boolean registerUser(String username, String password) {
        auth();
        if(!jedis.exists("user:" + username)) {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
            //Change the byte[] from MessageDigest into a string
            String hashPass = Base64.getEncoder().encodeToString(digest.digest(password.getBytes()));
            jedis.hset("user:" + username, "password", hashPass);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fetches all data associated with a user
     * @param username
     * @return User object
     */
    public static User getUserData(String username) {
        auth();
        User user = new User();
        user.setUsername(username);
        user.setEntries(getUserEntries(user));
        return user;
    }

    /**
     * Fetches a users address book entries
     * @param user a  user object
     * @return an arrray of entries or an entry[1] array with empty values if none
     */
    public static Entry[] getUserEntries(User user) {
        auth();
        String listKey = user.getUsername() + ":contactList";
        //Ensure that they have any entries
        if (jedis.exists(listKey)) {
            //Redis range -1 acquires all values under the key in the list
            String[] entryKeys = jedis.zrange(listKey, 0, -1).toArray(new String[0]);
            Entry[] contacts = new Entry[entryKeys.length];
            //Arrays to maintain sorted ordering since a for:each isn't guaranteed to iterate in order
            for (int i = 0; i < entryKeys.length; i++)
                contacts[i] = getEntry(user, entryKeys[i]);
            return contacts;
        } else { //If they have no contacts, return an empty array
            Entry[] defaultArray = {new Entry("","","","","",0)};
            return defaultArray;
        }
    }

    /**
     * Adds a contact to a users contactList
     * @param user user object containing username
     * @param firstName Must be present
     * @param lastName Must be present
     * @param address Must be present
     * @param email nullable
     * @param phone nullable
     * @return true on success, false otherwise
     */
    public static boolean addEntry(User user, String firstName, String lastName, String address,
                                   String email, String phone) {

        if(user == null || firstName == null || lastName == null || address == null)
            return false;

        auth();

        String listKey = user.getUsername() + ":contactList";
        String entryKey = firstName.toLowerCase().replaceAll(" ", "")
                + lastName.toLowerCase().replaceAll(" ", "");

        boolean isNewKey = (jedis.zadd(listKey, 0, entryKey, ZAddParams.zAddParams().nx()) == 1);

        int duplicateIndex = 0;
        while (!isNewKey) {
            duplicateIndex++;
            isNewKey = (jedis.zadd(listKey, 0, entryKey + duplicateIndex,
                    ZAddParams.zAddParams().nx()) == 1);
        }
        //If it was not a new key, append an index to the stored key (i.e. johnsmith, johnsmith1)
        if(duplicateIndex != 0)
            entryKey = entryKey + duplicateIndex;

        String hKey = user.getUsername() + ":" + entryKey;
        jedis.hset(hKey, "firstName", firstName);
        jedis.hset(hKey, "lastName", lastName);
        jedis.hset(hKey, "address", address);
        jedis.hset(hKey, "index", Integer.toString(duplicateIndex));
        if (email != null)
            jedis.hset(hKey, "email", email);
        if (phone != null)
            jedis.hset(hKey, "phone", phone);

        return true;
    }

    /**
     * Gets all info pertaining to an entry
     * @param user user object containing username
     * @param entryKey key to retrieve
     * @return populated Entry object
     */
    public static Entry getEntry(User user, String entryKey) {
        auth();
        String hKey = user.getUsername() + ":" + entryKey;
        Map<String, String> value = jedis.hgetAll(hKey);
        String firstName = value.get("firstName");
        String lastName = value.get("lastName");
        String address = value.get("address");
        String email = value.get("email");
        String phone = value.get("phone");
        int index = Integer.parseInt(value.get("index"));
        return (new Entry(firstName, lastName, email, address, phone, index));
    }

    /**
     * Searches entries by first name last name; case and space insensitive
     * ***This function uses redis DB searching, not a local search of the users entry array***
     * @param user
     * @param search string to search for
     * @return array of all entries matching the search term
     */
    public static Entry[] searchEntries(User user, String search) {
        auth();
        if(search.contains(" "))
            search = search.replaceAll(" ", "");

        String listKey = user.getUsername() + ":contactList";
        String min = "[" + search.toLowerCase();
        //~ is the second highest ASCII character, which causes zrangebylex to return anything
        // starting with the search term
        String max = "[" + search.toLowerCase() + "~";
        String[] resultKeys = jedis.zrangeByLex(listKey, min, max).toArray(new String[0]);
        Entry[] result = new Entry[resultKeys.length];
        for(int i = 0; i < resultKeys.length; i++)
            result[i] = getEntry(user, resultKeys[i]);
        return result;
    }

    /**
     * Updates an entry in the database
     * @param user
     * @param entry
     * @param newFirstName
     * @param newLastName
     * @param address
     * @param email
     * @param phone
     */
    public static void updateEntry(User user, Entry entry, String newFirstName, String newLastName, String address, String email, String phone) {
        //If the first and last name didn't change, we don't need to change out keys
        if(entry.getFirstName().equalsIgnoreCase(newFirstName) && entry.getLastName().equalsIgnoreCase(newLastName)) {
            String hkey = user.getUsername() + ":" + entry.getKeyString();
            jedis.hset(hkey, "firstName", newFirstName);
            jedis.hset(hkey, "lastName", newLastName);
            jedis.hset(hkey, "address", address);
            if(email != null)
                jedis.hset(hkey, "email", email);
            if(phone != null)
                jedis.hset(hkey, "phone", phone);
        } else {
            //Keys need to be changed to maintain sort ordering
            //This is essentially a brand new contact so we treat it as such
            delete(user, entry);
            addEntry(user, newFirstName, newLastName, address, email, phone);
        }

    }

    /**
     * Deletes an entry from the database
     * @param user
     * @param entry
     */
    public static void delete(User user, Entry entry) {
        auth();
        String keyName = entry.getKeyString();
        jedis.zrem(user.getUsername() + ":contactList", keyName);
        jedis.del(user.getUsername() + ":" + keyName);
    }
}
