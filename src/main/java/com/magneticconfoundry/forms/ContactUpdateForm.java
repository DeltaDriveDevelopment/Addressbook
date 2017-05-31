package com.magneticconfoundry.forms;

/**
 * Created by Tyler on 5/29/2017.
 * Handles form validation and updating/deleting contacts from the DB
 */

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.magneticconfoundry.Addressbook;
import com.magneticconfoundry.databaseUtils.Entry;
import com.magneticconfoundry.databaseUtils.Redis;
import com.magneticconfoundry.databaseUtils.User;
import com.magneticconfoundry.validators.PhoneValidator;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;


public class ContactUpdateForm extends Form {
    TextField firstNameField, lastNameField, addressField, emailField, phoneField;
    private Button updateButton, deleteButton;
    Entry entry;
    User user;
    Addressbook addressbook;

    public ContactUpdateForm(String id, Entry entry, User user, Addressbook addressbook) {
        super(id);
        this.entry = entry;
        this.user = user;
        this.addressbook = addressbook;
        init();
    }

    /**
     * Initialize components
     */
    private void init() {
        firstNameField = new TextField("First", Model.of(entry.getFirstName()));
        firstNameField.setRequired(true);
        lastNameField = new TextField("Last", Model.of(entry.getLastName()));
        lastNameField.setRequired(true);
        addressField = new TextField("Address", Model.of(entry.getAddress()));
        addressField.setRequired(true);
        emailField = new TextField("Email", Model.of(entry.getEmail()));
        emailField.add(EmailAddressValidator.getInstance());
        emailField.setRequired(false);
        phoneField = new TextField("Phone", Model.of(entry.getPhone()));
        phoneField.setRequired(false);
        phoneField.add(new PhoneValidator());
        add(firstNameField);
        add(lastNameField);
        add(addressField);
        add(emailField);
        add(phoneField);
        updateButton = new Button("update") {

            public void onSubmit() {
                String email = emailField.getInput();
                String phone = phoneField.getInput();

                //email and phone fields are only validated if they contain something, so double check they
                //aren't empty
                if (!emailField.isValid() || email.equalsIgnoreCase(""))
                    email = null;
                if (!phoneField.isValid() || phone.equalsIgnoreCase(""))
                    phone = null;
                else {
                    PhoneNumberUtil util = PhoneNumberUtil.getInstance();
                    Phonenumber.PhoneNumber number = null;
                    try {
                        number = util.parse(phoneField.getInput(), "US");
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                    phone = util.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                }
                updateContact(entry, firstNameField.getInput(), lastNameField.getInput(), addressField.getInput(),
                        email, phone);
            }

            public void onError() {
                //If the user cleared any of the required fields, replace the content that was in them
                if (firstNameField.getInput().equalsIgnoreCase(""))
                    firstNameField.setModel(Model.of(entry.getFirstName()));
                if (lastNameField.getInput().equalsIgnoreCase(""))
                    lastNameField.setModel(Model.of(entry.getLastName()));
                if (addressField.getInput().equalsIgnoreCase(""))
                    addressField.setModel(Model.of(entry.getAddress()));
            }
        };
        add(updateButton);

        deleteButton = new Button("delete") {
            public void onSubmit() {
                deleteContact(user, entry);
            }
        };
        add(deleteButton);
    }

    /**
     * Deletes entry from database
     * @param user
     * @param entry
     */
    private void deleteContact(User user, Entry entry) {
        Redis.delete(user, entry);
        user.setEntries(Redis.getUserEntries(user));
        addressbook.updateList(user.getEntries());
    }

    /**
     * Updates a contact in the database
     * @param entry
     * @param newFirstName
     * @param newLastName
     * @param address
     * @param email
     * @param phone
     */
    public void updateContact(Entry entry, String newFirstName, String newLastName, String address, String email, String phone) {
        Redis.updateEntry(user, entry, newFirstName, newLastName, address, email, phone);
        user.setEntries(Redis.getUserEntries(user));
        addressbook.updateList(user.getEntries());
    }
}
