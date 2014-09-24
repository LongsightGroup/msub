/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.sakai.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 20, 2006
 * Time: 10:28:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class SqlExecute {
   static public void  main(String args[]){
      if (args.length !=5) {
         System.out.println("Usage:  SqlExecute [url] [driver] [user] [password] [sqlFile]");
         System.exit(1);
      }
      String url = args[0];
      String driverClassName = args[1];
      String username = args[2];
      String password = args[3];
      File sqlFile = new File(args[4]);
      String fileContents = getContents(sqlFile);
      String[] sql = fileContents.trim().split(";");
      Connection connection = null;

      try {
         Class.forName(driverClassName);
         connection = DriverManager.getConnection(url, username, password);
         for (int i = 0; i<sql.length;i++){
            Statement stmt = connection.createStatement();
            System.out.println(sql[i]);
            try {
               stmt.execute(sql[i]);
            } catch (Exception e){
               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            stmt.close();
         }
         connection.commit();
      } catch (Exception e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      } finally{
         try {
            connection.close();
         } catch (SQLException e) {
         }
      }
   }

/**
  * Fetch the entire contents of a text file, and return it in a String.
  * This style of implementation does not throw Exceptions to the caller.
  *
  * @param aFile is a file which already exists and can be read.
  */
  static public String getContents(File aFile) {
    //...checks on aFile are elided
    StringBuffer contents = new StringBuffer();

    //declared here only to make visible to finally clause
    BufferedReader input = null;
    try {
      //use buffering, reading one line at a time
      //FileReader always assumes default encoding is OK!
      input = new BufferedReader( new FileReader(aFile) );
      String line = null; //not declared within while loop
      /*
      * readLine is a bit quirky :
      * it returns the content of a line MINUS the newline.
      * it returns null only for the END of the stream.
      * it returns an empty String if two newlines appear in a row.
      */
      while (( line = input.readLine()) != null){
        contents.append(line);
        contents.append(System.getProperty("line.separator"));
      }
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex){
      ex.printStackTrace();
    }
    finally {
      try {
        if (input!= null) {
          //flush and close both "input" and its underlying FileReader
          input.close();
        }
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return contents.toString();
  }
}
