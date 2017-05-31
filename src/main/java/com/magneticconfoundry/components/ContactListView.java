package com.magneticconfoundry.components;

/**
 * Created by Tyler on 5/29/2017.
 *
 * Custom ListView implementation to allow for easier changing to the list view behavior if needed in the future
 */

import com.magneticconfoundry.Addressbook;
import com.magneticconfoundry.forms.ContactUpdateForm;
import com.magneticconfoundry.databaseUtils.Entry;
import com.magneticconfoundry.databaseUtils.User;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.util.List;

public class ContactListView extends ListView{
    TextField firstNameField, lastNameField, addressField, emailField, phoneField;
    private Button updateButton, deleteButton;
    List list;
    User user;
    Addressbook addressbook;

    public ContactListView(String id, List list, User user, Addressbook addressbook) {
        super(id, list);
        this.list = list;
        this.user = user;
        this.addressbook = addressbook;
    }

    @Override
    protected void populateItem(final ListItem item) {
        final Entry entry = (Entry) item.getModelObject();
        item.add(new ContactUpdateForm("contactUpdateForm", entry, user, addressbook));
    }
}
