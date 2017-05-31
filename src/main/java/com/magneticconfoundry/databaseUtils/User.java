package com.magneticconfoundry.databaseUtils;

/**
 *  Created by Tyler on 5/25/2017.
 *  Maintains all info pertaining to a user
 *
 *  Holds very little but this method allows us to easily expand/change data stored with a user
 */
public class User {
    private String username;
    private Entry[] entries;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Entry[] getEntries() {
        return entries;
    }

    public void setEntries(Entry[] entries) {
        this.entries = entries;
    }
}
