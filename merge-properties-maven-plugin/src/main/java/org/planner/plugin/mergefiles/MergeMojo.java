/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.planner.plugin.mergefiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Merges multiple properties files into one.
 *

 * @goal merge
 * @phase process-sources
 * @requiresProject
 */
public class MergeMojo extends AbstractMojo {
	
	private static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * The properties files to merge. <br>
	 * 
	 * @parameter
	 * @required
	 */
	private Merge[] merges;

	/**
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		for (Merge merge : merges) {
			
			// remove target file if present...
			//if (merge.getTargetFile().exists()) {
			//	merge.getTargetFile().delete();
			//}

			// merge the files into the target directory
			int numMergedFiles = mergeFiles(merge);

			getLog().info("Finished Appending: " + numMergedFiles + " files to the target file: " + merge.getTargetFile().getAbsolutePath() + ".");
		}
	}

	private int mergeFiles(Merge merge) throws MojoExecutionException {

		// read the encoding to use
		String encoding = DEFAULT_ENCODING;
		if (merge.getEncoding() != null && merge.getEncoding().length() > 0) {
			encoding = merge.getEncoding();
		}
		
		int numMergedFiles = 0 ;
		final Properties mergedLines = new Properties();
		final Properties excludedKeys = new Properties();
		String[] excludes = merge.getExcludes() ;
		if ( excludes != null )
		{
			for ( String line : excludes )
			{
				excludedKeys.put(line, "") ;
			}
		}
		
		final List<String> lines = new ArrayList<String>() ;
		final List<File> propertiesFiles = Arrays.asList(merge.getFiles());
		for (File propertiesFile : propertiesFiles) {
			InputStream fin = null;
			try {
				if (propertiesFile.isDirectory())
					throw new MojoExecutionException("File "
							+ propertiesFile.getAbsolutePath()
							+ " is directory!");
				if (!propertiesFile.exists())
					throw new MojoExecutionException("File "
							+ propertiesFile.getAbsolutePath()
							+ " does not exist!");
				
				fin = new FileInputStream(propertiesFile);
				Reader reader = null;
				try {
					//now add the line in the file to lines contains excluding
					reader = new InputStreamReader(fin, encoding);
					BufferedReader input = new BufferedReader(reader);
					String line = null ;
					while ( (line = input.readLine() ) != null ) {
						String key = getKey( line ) ;
						if (key != null ) {
							if ( excludedKeys.containsKey( key ) )
								getLog().warn( "Line discarded: " + line ) ;
							else if ( mergedLines.containsKey(key) )
							{
								mergedLines.put(key, line) ;
								getLog().warn( "Line merged: " + line ) ;
							} else {
								mergedLines.put(key, line) ;
								lines.add( line ) ;
							}
						} else {
							lines.add( line ) ;
						}
					}
					getLog().info("Appending file: " + propertiesFile.getAbsolutePath() + " to the target file: " + merge.getTargetFile().getAbsolutePath() + "...");
				} catch (IOException ioe) {
					throw new MojoExecutionException("Failed to append file: " + propertiesFile.getAbsolutePath() + " to output file", ioe);
				}
				numMergedFiles++;
				
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("Could not find file: "
						+ propertiesFile.getAbsolutePath(), e);
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						// no can do
					}
				}
			}
		}
		
		//now save the line
		Writer writer = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(merge.getTargetFile());
			writer = new OutputStreamWriter(fos, encoding);
			BufferedWriter output = new BufferedWriter(writer);
			for (String line : lines ) {
				String value = getKeyedValue( line, mergedLines ) ;
				if ( value != null )
    				output.write( value, 0, value.length() ) ;
				else
    				output.write( line, 0, line.length() ) ;
					
				output.newLine() ;
			}
			output.flush();
		} catch (IOException ioe) {
			throw new MojoExecutionException("Failed to save to output file: " + merge.getTargetFile().getAbsolutePath(), ioe);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// no can do
				}
			}
		}
		return numMergedFiles;
	}
	
	private String getKey( String line )
	{
		String key = null ;
		if ( line.length() > 0  && line.charAt( 0 ) !='#')
		{   // not a comment
  		    int n = line.indexOf( "=" ) ;
		    if ( n != -1 )
		    {
			   key = line.substring( 0, n ).trim() ;
		     }
		}
		return key ;
	}

	private String getKeyedValue( String line, Properties properties )
	{
		int n = line.indexOf( "=" ) ;
		String key = null ;
		if ( n != -1 )
		{
			key = line.substring( 0, n ).trim() ;
			return properties.getProperty(key) ;
		} else
			return null ;
	}
}
