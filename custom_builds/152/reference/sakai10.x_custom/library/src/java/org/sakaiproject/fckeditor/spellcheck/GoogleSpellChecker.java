package org.sakaiproject.fckeditor.spellcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.sakaiproject.util.ResourceLoader;
import org.xeustechnologies.googleapi.spelling.*;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/31/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleSpellChecker {
    protected static Log log = LogFactory.getLog(GoogleSpellChecker.class);

    private String textInput;
    private JSONArray words = new JSONArray();
    private JSONArray suggestions = new JSONArray();
    private JSONArray suggestionsArray = new JSONArray();

    private JSONArray wordsArray = new JSONArray();

    private String error;
    public GoogleSpellChecker(String textInput) {
        this.textInput = textInput;
        words.add(0, wordsArray);
        suggestions.add(0,suggestionsArray);

        try {
            textInput = Jsoup.parse(URLDecoder.decode(textInput, "UTF-8")).body().text();
            SpellChecker spellCheck = new SpellChecker();

            spellCheck.setLanguage(getLanguageFromUserLocale());
            SpellRequest spellRequest = new SpellRequest();

            log.debug("google spell checker initialized");

            spellRequest.setText(textInput);
            SpellResponse spellResponse = spellCheck.check(spellRequest);

            log.debug("google spell checker response received for input: " + spellRequest.getText());
            convertResponseToJson(textInput, spellResponse);

        } catch (Exception e) {
            log.warn("spellchecker error parsing input: " + textInput + ":" + e.getMessage(), e );
            error = e.getLocalizedMessage();
            return;
        }


    }

    protected Language getLanguageFromUserLocale() {
        String userLocaleLanguageCode = getUserLanguageCode();
        for (Language googleSpellLanguage : Language.values()) {
            if (googleSpellLanguage.code().equals(userLocaleLanguageCode)) {
                return googleSpellLanguage;
            }
        }

        log.debug("could not find a google spell check language to match the Sakai user language of " + userLocaleLanguageCode +
                " defaulting to English because we have the biggest guns");
        return Language.ENGLISH;
    }

    public String getUserLanguageCode() {
        Locale locale = (new ResourceLoader()).getLocale();
        return locale.getLanguage();
    }

    protected void convertResponseToJson(String textInput, SpellResponse spellResponse) {



        int index = 0;
        if(spellResponse.getCorrections() != null){
            for(SpellCorrection correction : spellResponse.getCorrections()){
                JSONArray currentSuggestionsArray = new JSONArray();

                wordsArray.add(index,textInput.substring(correction.getOffset(), correction.getOffset() + correction.getLength()));

                List<String> suggestionList = Arrays.asList(correction.getWords());

                if(!suggestionList.isEmpty()){

                    for(Iterator it = suggestionList.iterator(); it.hasNext();){
                        currentSuggestionsArray.add(it.next().toString());
                    }
                }
                suggestionsArray.add(index, currentSuggestionsArray);
                index++;
            }
        }
        log.debug("words json=" + words.toJSONString());
        log.debug("suggestions json=" + suggestions.toJSONString());
    }

    public String getTextInput() {
        return textInput;
    }

    public void setTextInput(String textInput) {
        this.textInput = textInput;
    }

    public String getWordsAsJSON() {
        return words.toJSONString();
    }

    public String getSuggestionsAsJSON() {
        return suggestions.toJSONString();
    }

    public JSONArray getWords() {
        return words;
    }

    public JSONArray getSuggestions() {
        return suggestions;
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return (error != null && error.length() > 0);
    }
}
