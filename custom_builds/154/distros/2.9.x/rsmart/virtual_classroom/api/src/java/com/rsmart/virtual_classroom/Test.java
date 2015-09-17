package com.rsmart.virtual_classroom;


import java.io.*;
import java.net.*;

public class Test {
   /*
   public static final void main(String args[]) {
      try {
        URL           elluminateServerURL            = new URL("http://localhost:80/webservice.event?WSDL");
        URLConnection elluminateLiveServerConnection = elluminateServerURL.openConnection();
        elluminateLiveServerConnection.setDoOutput(true);



        PrintWriter out = new PrintWriter(elluminateLiveServerConnection.getOutputStream());
        out.println("adapter=default&command=CreateMeeting&meetingName=MyFirstMeeting&startDate=01/06/2006 13:00:00 MST&endDate=01/06/2006 14:00:00 MST&facilitatorId=joneric");
        out.close();

        // elluminate will invoke the method on its server and return the result in an xml document
        BufferedReader in        = new BufferedReader(new InputStreamReader(elluminateLiveServerConnection.getInputStream()));
        String         inputLine = null;

        // here we are just printing the resulting xml out.  But you should probably put it into a jdom object.
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);

        in.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
     */
    public static void main(String[] args) throws Exception {

        if (args.length  < 2) {
            System.err.println("Usage:  java SOAPClient4XG " +
                               "http://soapURL soapEnvelopefile.xml" +
                               " [SOAPAction]");
				System.err.println("SOAPAction is optional.");
            System.exit(1);
        }

        String SOAPUrl      = args[0];
        String xmlFile2Send = args[1];

		  String SOAPAction = "";
        if (args.length  > 2)
				SOAPAction = args[2];

        // Create the connection where we're going to send the file.
        URL url = new URL(SOAPUrl);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;

        // Open the input file. After we copy it to a byte array, we can see
        // how big it is so that we can set the HTTP Cotent-Length
        // property. (See complete e-mail below for more on this.)

        FileInputStream fin = new FileInputStream(xmlFile2Send);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        // Copy the SOAP file to the open connection.
        copy(fin,bout);
        fin.close();

        byte[] b = bout.toByteArray();

        // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty( "Content-Length",
                                     String.valueOf( b.length ) );
        httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
		  httpConn.setRequestProperty("SOAPAction",SOAPAction);
        httpConn.setRequestMethod( "POST" );
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        

        // Everything's set up; send the XML that was read in to b.
        OutputStream out = httpConn.getOutputStream();
        out.write( b );
        out.close();

        // Read the response and write it to standard out.

        InputStreamReader isr =
            new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        String inputLine;

        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);

        in.close();
    }

  // copy method from From E.R. Harold's book "Java I/O"
  public static void copy(InputStream in, OutputStream out)
   throws IOException {

    // do not allow other threads to read from the
    // input or write to the output while copying is
    // taking place

    synchronized (in) {
      synchronized (out) {

        byte[] buffer = new byte[256];
        while (true) {
          int bytesRead = in.read(buffer);
          if (bytesRead == -1) break;
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }
}

