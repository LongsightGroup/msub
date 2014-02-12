package org.sakaiproject.component.app.roster;

import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.PhotoService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class LongsightPhotoService implements PhotoService {

	private static final Log log = LogFactory.getLog(LongsightPhotoService.class);

	/** Dependency: userDirectoryService */
	private UserDirectoryService userDirectoryService;

	public byte[] getPhotoAsByteArray(String userId, boolean hasSitePermission) {
		// String basepath = "/mnt/sakai/pepp1/photos";
		String basepath =  ServerConfigurationService.getString("longsight.photo.directory", "/mnt/master/walsh/photos");
		String filename = basepath + "/" + userId + ".jpg";

		try {
			User user = userDirectoryService.getUser(userId);
			userId = user.getEid();
		
			String firstLetter = userId.substring(0,1);
			filename = basepath + "/" + firstLetter + "/" + userId + ".jpg";
			
		}
		catch (UserNotDefinedException ee) {
			System.out.println("Could not find user: " + userId);
		}

		File file = new File(filename);

		try {
			return getBytesFromFile(file);
		}
		catch (IOException e) {
			System.out.println("Could not find file: " + filename);
			return null;
		}
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

	/**
	 * @param userDirectoryService
	 *        The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		System.out.println("setUserDirectoryService(userDirectoryService " + userDirectoryService + ")");

		this.userDirectoryService = userDirectoryService;
	}
}
