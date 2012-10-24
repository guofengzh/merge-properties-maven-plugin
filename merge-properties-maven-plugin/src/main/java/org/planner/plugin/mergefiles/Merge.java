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

import java.io.File;
import java.util.Arrays;

/**
 * The POJO to hold the merging configuration.
 * 
 */
public class Merge {
	/**
	 * The target file.
	 * 
	 * @parameter
	 * @required
	 */
	private File targetFile;

	/**
	 * The files to merge.
	 * 
	 * @parameter
	 * @required
	 */
	private File[] propertiesFiles;

	/**
	 * The lines to exclude.
	 * 
	 * @parameter
	 */
	private String[] excludes;
	
	/**
	 * The lines to exclude.
	 * 
	 * @parameter
	 */
	private String encoding;

	/**
	 * Returns the target file where the result of the merging should be saved.
	 * 
	 * @return The target file.
	 */
	public File getTargetFile() {
		return targetFile;
	}

	/**
	 * Returns the files that are to be merged.
	 * 
	 * @return The files to merge.
	 */
	public File[] getFiles() {
		return propertiesFiles.clone();
	}

    /**
     * @return  the optional set of lines excludes
     */
	public String[] getExcludes() {
		return this.excludes;
	}

	/**
	 * This is an optional encoding to use when reading/writing the files being merged, if not specified
	 * then UTF-8 will be used
	 * @parameter
	 * @return the encoding to use for reading/writing files
	 */
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public String toString() {
		return "Merge [files=" + Arrays.asList(propertiesFiles)
				+ ", targetFile=" + targetFile + "]";
	}
}
