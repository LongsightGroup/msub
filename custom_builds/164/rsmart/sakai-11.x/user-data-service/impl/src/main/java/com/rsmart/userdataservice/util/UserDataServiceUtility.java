package com.rsmart.userdataservice.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 19, 2011
 * Time: 9:01:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserDataServiceUtility {


    /**
     * give the right format to put into the database
     *
     * @return
     */
    public static String getFormttedDate(){
        Date todayDate = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
        return df.format(todayDate);
    }
}
