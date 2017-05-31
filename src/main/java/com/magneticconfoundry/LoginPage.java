package com.magneticconfoundry;

/**
 * Created by Tyler on 5/24/2017.
 *
 * Main Login page handler
 */

import com.magneticconfoundry.forms.LoginForm;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class LoginPage extends WebPage {
    private static final long serialVersionUID = 2L;

    public LoginPage(final PageParameters parameters) {
        super(parameters);
        add(new LoginForm("loginForm"));
    }
}
