package com.magneticconfoundry.databaseUtils;

/**
 * Created by Tyler on 5/25/2017.
 * Maintains all info pertaining to a particular entry in the contact list
 */
public class Entry {

    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String email;
    private int index;

    public Entry(String firstName, String lastName, String email, String address, String phone, int index) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.index = index;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIndex(int index) { this.index = index; }

    public int getIndex() {return index; }

    public String getKeyString() {
        String hkey = firstName.toLowerCase().replaceAll(" ", "")
                + lastName.toLowerCase().replaceAll(" ", "");
        if(index != 0)
            hkey += index;
        return hkey;
    }
}
