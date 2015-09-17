package com.rsmart.customer.integration.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Sep 28, 2010
 * Time: 3:10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseSeparatedFileProcessor extends BaseFileProcessor
{
    /** Log */
    private static final Log logger = LogFactory
            .getLog(BaseFileProcessor.class);

    /** Line Token */
    private String token = "|";

    /**
     * Process
     */
    public void processFormattedFile(BufferedReader fr, FileProcessorState state)
        throws Exception
    {
        String temp = fr.readLine();
        String[] line = null;

        while (temp != null) {
            state.setRecordCnt( state.getRecordCnt() + 1 );

            StringTokenizer tok = new StringTokenizer(temp, token, false);

            if( state.getColumns() != tok.countTokens() ) {
                state.appendError("Wrong Number Columns Row" + state.getRecordCnt()+ ", Saw"+ state.getColumns()+ ", Expected: " +temp.length() );
                state.setErrorCnt( state.getErrorCnt() + 1 );
            }
            else {
                line = new String[tok.countTokens()];

                for (int i = 0; i < line.length; i++) {
                    line[i] = tok.nextToken();
                }

                boolean headerPresent = state.isHeaderRowPresent();

                if ((headerPresent && state.getRecordCnt() > 1) || !headerPresent) {
                    try {
                        processRow(line, state);
                        state.setProcessedCnt( state.getProcessedCnt() + 1);
                    } catch (Exception err) {
                        logger.error(err);
                        state.appendError( "Row " + state.getRecordCnt() + " " + err.getMessage() );
                        state.setErrorCnt( state.getErrorCnt() + 1 );
                    }
                }
            }

            temp = fr.readLine();
        }

        fr.close();
    }

    /**
     * Get Token
     *
     * @return String
     */
    public String getToken() {
        return token;
    }

    /**
     * Set Token
     *
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }


}
