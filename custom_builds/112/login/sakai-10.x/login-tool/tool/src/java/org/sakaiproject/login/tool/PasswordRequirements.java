package org.sakaiproject.login.tool;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;

public class PasswordRequirements {
       private static Log M_log = LogFactory.getLog(LoginUtils.class);

       private static PasswordRequirements instance = null;

       private int passwordMinimumLength = -1;
       private int passwordMinimumNumbers = -1;
       private int passwordMinimumUpperCaseLetters = -1;
       private int passwordMinimumLowerCaseLetters = -1;

       public static PasswordRequirements getInstance() {
               if (instance == null) {
                       instance = new PasswordRequirements();
               }

               return instance;
       }

       public void init() {
               getInstance();
       }

       private PasswordRequirements() {
               getPasswordMinimumLength();
               getPasswordMinimumAmountOfUpperCaseLetters();
               getPasswordMinimumAmountOfLowerCaseLetters();
               getPasswordMinimumAmountOfNumbers();

               checkAndCorrectSettings();
       }

       /**
        * Check the retrieved values. The minimum password length cannot be lower than all those values combined. Adjust the minimum password length accordingly 
        */
       private void checkAndCorrectSettings() {
               if (passwordMinimumLength < (passwordMinimumNumbers
                               + passwordMinimumLowerCaseLetters + passwordMinimumUpperCaseLetters)) {
                       passwordMinimumLength = passwordMinimumNumbers
                                       + passwordMinimumLowerCaseLetters
                                       + passwordMinimumUpperCaseLetters;
               }
       }

       /**
        * Get a password setting from the sakai.properties file. This method should only be used for password settings, as internally it's converted to an Integer.
        * @param property The property to retrieve
        * @return Integer of the found value, 0 if value is invalid or not set.
        */
       private int getPasswordPropertyInt(String property) {
               String value = ServerConfigurationService.getString(property);

               int intValue = 0;
               if (null != value && !"".equals(value)) {
                       try {
                               intValue = Integer.valueOf(value);
                               intValue = (intValue < 0) ? 0 : intValue;
                       } catch (NumberFormatException e) {
                               M_log.warn("The "
                                               + property
                                               + " property in sakai.properties is NOT a number. Please leave empty if this property should not be used.");
                       }
               }

               return intValue;
       }

       /**
        * @return
        */
       public int getPasswordMinimumLength() {
               if (passwordMinimumLength == -1)
                       passwordMinimumLength = getPasswordPropertyInt("password.requirements.min.length");
               return passwordMinimumLength;

       }

       /**
        * @return
        */
       public int getPasswordMinimumAmountOfUpperCaseLetters() {
               if (passwordMinimumUpperCaseLetters == -1)
                       passwordMinimumUpperCaseLetters = getPasswordPropertyInt("password.requirements.min.letters.uppercase");
               return passwordMinimumUpperCaseLetters;
       }

       /**
        * @return
        */
       public int getPasswordMinimumAmountOfLowerCaseLetters() {
               if (passwordMinimumLowerCaseLetters == -1)
                       passwordMinimumLowerCaseLetters = getPasswordPropertyInt("password.requirements.min.letters.lowercase");
               return passwordMinimumLowerCaseLetters;
       }

       /**
        * @return
        */
       public int getPasswordMinimumAmountOfNumbers() {
               if (passwordMinimumNumbers == -1)
                       passwordMinimumNumbers = getPasswordPropertyInt("password.requirements.min.numbers");
               return passwordMinimumNumbers;
       }

       /**
        * Check if a password has enough occurrences of the provided regex.
        * @param password The password
        * @param regex The regex
        * @param minimumOccurrences The minimum amount of occurrences which should be present in the password
        * @return True if the password contains enough occurrences of the provided regex.
        */
       private boolean passwordContainsEnoughOfRegex(String password, String regex, int minimumOccurrences){
               boolean containsEnough = false;

               int occurrences = password.split(regex, -1).length - 1;
               if(occurrences >= minimumOccurrences){
                       containsEnough = true;
               }
               return containsEnough;
       }

       /**
        * Check if the password is valid by checking if it meets the requirements.
        * @param password The password
        * @return True if the password meets the requirements. False otherwise.
        */
       private boolean isValidPassword(String password) {
         boolean enoughNumbers = passwordContainsEnoughOfRegex(password, "[\\d]", passwordMinimumNumbers);
         boolean enoughLowerCase = passwordContainsEnoughOfRegex(password, "[a-z]", passwordMinimumLowerCaseLetters);
         boolean enoughUpperCase = passwordContainsEnoughOfRegex(password, "[A-Z]", passwordMinimumUpperCaseLetters);


         if(enoughNumbers && enoughLowerCase && enoughUpperCase){
                 return true;
         }else{
                 return false;
         }
       }

       /**
        * Check if the given password meets the password requirements.
        * @param password
        * @return True if the password meets the requirements
        */
       public boolean passwordMeetsRequirements(String password) {
               boolean passwordValid = false;

               if (password.length() < getPasswordMinimumLength())
                       return false;

               if (passwordMinimumNumbers > 0 || passwordMinimumLowerCaseLetters > 0
                               || passwordMinimumUpperCaseLetters > 0) {
                       passwordValid = isValidPassword(password);

               } else {
                       passwordValid = true;
               }

               return passwordValid;
       }

}
