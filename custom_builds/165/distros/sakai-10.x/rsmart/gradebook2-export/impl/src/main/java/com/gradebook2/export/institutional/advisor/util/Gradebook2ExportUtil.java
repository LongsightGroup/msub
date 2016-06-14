package com.gradebook2.export.institutional.advisor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 28, 2011
 * Time: 8:32:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class Gradebook2ExportUtil {


    public static String formattedDate() {
        Date todaysDate = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        return df.format(todaysDate);
    }


    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


}
