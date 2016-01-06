package com.gradebook2.export.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Mar 24, 2011
 * Time: 1:38:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Gradebook2Impl extends SimpleJdbcTemplate{

    private static final Log log = LogFactory.getLog(Gradebook2Impl.class);

    public Gradebook2Impl(DataSource dataSource){
        super(dataSource);

    }

    /**
     * Checks to see if collection has already been created
     * @param collectionId
     * @return
     */
    public  boolean checkIfCollectionAlreadyExits(String collectionId) {
        List checkFoCollectionId = null;
        String query;
        try {
            query = "SELECT COLLECTION_ID FROM CONTENT_COLLECTION WHERE COLLECTION_ID=?";
            checkFoCollectionId = queryForList(query, new Object[]{collectionId});
        } catch (Exception e) {
             log.debug("Exception checking if collection Id" + collectionId + "exits", e);
        }

        if (checkFoCollectionId != null && !checkFoCollectionId.isEmpty()) {
            return true;
        }

        return false;

    }

}
