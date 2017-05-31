package com.magneticconfoundry.validators;

/**
 * Validation method for phone numbers
 *
 * Uses Google's libphonenumber for validation
 * https://github.com/googlei18n/libphonenumber
 *
 * Supports validation and formatting of international numbers
 */

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class PhoneValidator implements IValidator<String> {
    @Override
    public void validate(IValidatable<String> iValidatable) {
        final String phoneNumber = iValidatable.getValue();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumberObj = phoneUtil.parse(phoneNumber, "US");
        } catch (NumberParseException e) {
            ValidationError error =  new ValidationError();
            error.setMessage("The value of 'Phone' is not a valid phone number.");
            iValidatable.error(error);
        }
    }
}
