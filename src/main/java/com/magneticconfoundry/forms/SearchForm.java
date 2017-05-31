package com.magneticconfoundry.forms;

import com.magneticconfoundry.Addressbook;
import com.magneticconfoundry.databaseUtils.Redis;
import com.magneticconfoundry.databaseUtils.User;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * Created by Tyler on 5/28/2017.
 * Handles searching of contact list
 *
 * Only currently supports searches that start with first name followed by last name
 * Other search methods would be implemented here
 */
public class SearchForm extends Form{
    private Addressbook addressbook;
    private TextField searchInput;
    private Button searchButton, clearButton;
    private User user;

    public SearchForm(String id, User user, Addressbook addressbook) {
        super(id);
        this.addressbook = addressbook;
        this.user = user;

        searchInput = new TextField("searchInput", Model.of(""));

        searchButton = new Button("searchButton") {
            public void onSubmit() {
                search();
            }
        };
        clearButton = new Button("clearSearchButton") {
            public void onSubmit() {
                searchInput.setDefaultModelObject("");
                resetContacts();
            }
        };
        add(searchInput);
        add(searchButton);
        add(clearButton);
    }

    /**
     * Initiates a search and updates the list with the returned arrays
     */
    private void search() {
        addressbook.updateList(Redis.searchEntries(user, searchInput.getInput()));
    }

    /**
     * Clears the search and resets the table back to the full list of contacts
     */
    private void resetContacts() {
        addressbook.updateList(user.getEntries());
    }
}
