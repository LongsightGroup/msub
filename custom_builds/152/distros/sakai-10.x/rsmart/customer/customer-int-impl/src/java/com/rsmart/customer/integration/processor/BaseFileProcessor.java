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

package com.rsmart.customer.integration.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rsmart.customer.integration.util.FileArchiveUtil;

/**
 * Abstract Base File Processor
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public abstract class BaseFileProcessor extends BaseProcessor implements FileProcessor {

	/** Log */
	private static final Log logger = LogFactory
			.getLog(BaseFileProcessor.class);

    private String
        filename = null;

    private int
        columns = -1;

    private boolean
        headerRowPresent = false;

	/** Header Row Flag */
	private boolean archive = false;
	
	/** Header Row Flag */
	private FileArchiveUtil fileArchiveUtil;

    public ProcessorState init (Map config)
    {
        BaseFileProcessorState
            fps = new BaseFileProcessorState();

        fps.setConfiguration(config);

        final String
            cName = this.getClass().getName();

        String
            baseDir = (String)getPropertyOrNull (config, "path.base"),
            fName = (String)getPropertyOrNull (config, cName + ".filename");

        String
            filename = null;

        if (fName == null || fName.trim().length() < 1)
        {
            filename = getFilename();
            logger.info( "processor configured to load default file: " + filename );
        }
        else
        {
            filename = "";

            if (baseDir != null)
            {
                filename = baseDir;
                if (!baseDir.endsWith(File.separator))
                {
                    filename += File.separator;
                }
            }
            filename += fName;

            logger.info( "processor configured to load override file: " + filename);
        }

        fps.setFilename(filename);

        int
            numCols = getColumns();

        if (config.containsKey(cName + ".columns"))
        {
            String
                cols = (String)config.get(cName + ".columns");

            try
            {
                numCols = Integer.parseInt(cols);
            }
            catch (NumberFormatException nfe)
            {
                logger.error ("improper number format specified in ProcessorState configuration for property " + cName + ".columns");
            }
        }

        fps.setColumns(numCols);

        boolean
            headerRow = isHeaderRowPresent();

        if (config.containsKey(cName + ".headerRowPresent"))
        {
            String
                hRows = (String)config.get(cName + ".headerRowPresent");

            headerRow = Boolean.parseBoolean(hRows);
        }

        fps.setHeaderRowPresent(headerRow);

        return fps;
    }

    protected static final Object getPropertyOrNull (final Map config, final String key)
    {
        if (config != null)
            return config.get(key);

        return null;
    }

	/**
	 * Process
	 */
	public void process(ProcessorState ps)
        throws Exception
    {
        if (!FileProcessorState.class.isAssignableFrom (ps.getClass()))
        {
            logger.error("could not proceed with ProcessorState of type " + ps.getClass().getName() +"; it is not of type FileProcessorState");
            throw new Exception ("process(...) called with a ProcessorState which is not a FileProcessorState");
        }

        final FileProcessorState
            state = (FileProcessorState) ps;

		state.setStartDate( new java.util.Date() );
		
		logger.info( getProcessorTitle() + " started " + state.getStartDate() );

		File
            dataFile = null;

        String
            filename = state.getFilename();

        dataFile = new File(filename);

		if( !dataFile.exists() ) {
			logger.error( filename + " not found" );
			state.appendError(filename + " not found");
			logger.error( getProcessorTitle() + " ended " + state.getEndDate() );
			return;
		}
		
		BufferedReader fr = new BufferedReader(new FileReader(dataFile));

        try
        {
            processFormattedFile(fr, state);
        }
        catch (Exception e)
        {
            logger.error ("file processing aborted for " + filename + " due to errors", e);
        }
        finally
        {
            try
            {
                fr.close();
            }
            catch (Exception e) {}
        }

		if(archive) {
			if( fileArchiveUtil != null ) {
				File dir = fileArchiveUtil.createArchiveFolder();
				dataFile.renameTo(new File(dir.getAbsoluteFile() + System.getProperty("file.separator") + dataFile.getName()));
			}
		}
		
		state.setEndDate( new java.util.Date() );
		
		logger.info( getProcessorTitle() + " ended " + state.getEndDate() );
		logger.info( getReport(state) );

        /* CLE-4700
           the only output given is if logger is set to info or above.  error output should
           be given in lower levels (eg. ERROR, WARN).  So if we are not in INFO level logging
           and we have errors, just dump the errors and skip the rest of the report.
         */
        if (state.getErrorCnt() > 0 && !logger.isInfoEnabled())
        {
            StringBuilder
                sb = new StringBuilder ("Errors were encountered while processing the integration file \"");
            sb.append(getFilename()).append("\":\n");

            for (Object error : state.getErrorList())
            {
                sb.append(error.toString()).append("\n");
            }

            logger.error(sb.toString());
        }        
	}

    public abstract void processFormattedFile(BufferedReader fr, FileProcessorState state) throws Exception;

	/**
	 *
	 * @return
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 *
	 * @param columns
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}

	/**
	 * Get File Name
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Set File Name
	 * 
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Is HeaderRowPresent
	 *
	 * @return boolean
	 */
	public boolean isHeaderRowPresent() {
		return headerRowPresent;
	}

	/**
	 * Set Header Flag
	 *
	 * @param present
	 */
	public void setHeaderRowPresent(boolean present) {
		this.headerRowPresent = present;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public FileArchiveUtil getFileArchiveUtil() {
		return fileArchiveUtil;
	}

	public void setFileArchiveUtil(FileArchiveUtil fileArchiveUtil) {
		this.fileArchiveUtil = fileArchiveUtil;
	}
	
}