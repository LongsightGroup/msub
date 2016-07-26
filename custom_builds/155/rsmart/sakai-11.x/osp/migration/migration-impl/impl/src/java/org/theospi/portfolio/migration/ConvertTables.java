package org.theospi.portfolio.migration;

import java.sql.*;/*
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

/*
 * generates script to convert mysql tables to type InnoDB
 */
public class ConvertTables {
   final static public void main(String[] args) {
   try {

            // Step 1: Load the JDBC driver.
            Class.forName("com.mysql.jdbc.Driver");

            // Step 2: Establish the connection to the database.
            String url = "jdbc:mysql://localhost:3306/rider";
            Connection conn = DriverManager.getConnection(url,"rider","rider");
            Statement stmt =conn.createStatement();
            stmt.execute("show tables");
      
            ResultSet resultSet =stmt.getResultSet();
            while (resultSet.next()) {
               System.out.println("alter table " + resultSet.getString(1) + " type InnoDb;");
            }
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }  
  

   }
}


