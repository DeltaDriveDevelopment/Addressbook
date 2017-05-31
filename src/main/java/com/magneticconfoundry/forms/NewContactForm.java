package com.magneticconfoundry.forms;

/**
 * Created by Tyler on 5/25/2017.
 * Handles adding new contacts to the database
 */

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.magneticconfoundry.Addressbook;
import com.magneticconfoundry.databaseUtils.Redis;
import com.magneticconfoundry.databaseUtils.User;
import com.magneticconfoundry.validators.PhoneValidator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.*;

public class NewContactForm extends Form{

    private RequiredTextField firstNameField, lastNameField, addressField;
    private TextField emailField, phoneField;
    private Label statusField;
    private Button addButton;
    private User user;
    private Addressbook addressbook;

    public NewContactForm(String id, User user, Addressbook addressbook) {
        super(id);
        this.user = user;
        this.addressbook = addressbook;
        statusField = new Label("addStatus", Model.of(""));
        firstNameField = new RequiredTextField("First Name", Model.of(""));
        firstNameField.setRequired(true);
        lastNameField = new RequiredTextField("Last Name", Model.of(""));
        lastNameField.setRequired(true);
        addressField = new RequiredTextField("Address", Model.of(""));
        addressField.setRequired(true);
        emailField = new TextField("Email", Model.of(""));
        emailField.add(EmailAddressValidator.getInstance());
        emailField.setRequired(false);
        phoneField = new TextField("Phone", Model.of(""));
        phoneField.setRequired(false);
        phoneField.add(new PhoneValidator());
        addButton = new Button("addContactButton"){
            public void onSubmit() {
                String firstName = firstNameField.getInput();
                String lastName = lastNameField.getInput();
                String address = addressField.getInput();
                String email = emailField.getInput();
                String phone = phoneField.getInput();
                PhoneNumberUtil util = PhoneNumberUtil.getInstance();
                try {
                    Phonenumber.PhoneNumber number = util.parse(phone, "US");
                    phone = util.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                } catch (NumberParseException e) {
                    e.printStackTrace();
                }
                addContact(firstName, lastName, address, email, phone);
                statusField.setDefaultModelObject("Contact Added");
                resetFields();
            }
        };

        add(firstNameField);
        add(lastNameField);
        add(addressField);
        add(emailField);
        add(phoneField);
        add(addButton);
        add(statusField);
    }

    /**
     * Clears fields
     */
    private void resetFields() {
        firstNameField.setModel(Model.of(""));
        lastNameField.setModel(Model.of(""));
        addressField.setModel(Model.of(""));
        emailField.setModel(Model.of(""));
        phoneField.setModel(Model.of(""));
    }

    /**
     * Adds Contact to the database
     * @param firstName
     * @param lastName
     * @param address
     * @param email
     * @param phone
     */
    private void addContact(String firstName, String lastName, String address, String email, String phone) {
        Redis.addEntry(user, firstName, lastName, address, email, phone);
        user.setEntries(Redis.getUserEntries(user));
        addressbook.updateList(user.getEntries());
    }
}
