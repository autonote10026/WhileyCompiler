// Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright
//      notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//      notice, this list of conditions and the following disclaimer in the
//      documentation and/or other materials provided with the distribution.
//    * Neither the name of the <organization> nor the
//      names of its contributors may be used to endorse or promote products
//      derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package wyil.util.path;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wyil.lang.ModuleID;
import wyil.lang.PkgID;
import wyil.util.path.BinaryDirectoryRoot.Entry;

public class SourceDirectoryRoot implements Path.Root {
	private static final FileFilter filter = new FileFilter() {
		public boolean accept(File file) {
			return file.getName().endsWith(".whiley");
		}
	};
	
	private final java.io.File dir;	
	private final BinaryDirectoryRoot bindir;
	
	/**
	 * Construct a directory root from a filesystem path expressed as a string,
	 * and an appropriate file filter. In converting the path to a File object,
	 * an IOException may arise if it is an invalid path.
	 * 
	 * @param path
	 *            --- location of directory on filesystem, expressed as a native
	 *            path (i.e. separated using File.separatorChar, etc)
	 * @param filter
	 *            --- filter which determines what constitutes a valid entry for
	 *            this directory.
	 * @throws IOException
	 */
	public SourceDirectoryRoot(String path, BinaryDirectoryRoot bindir) throws IOException {
		this.dir = new File(path);				
		this.bindir = bindir;
	}
	
	/**
	 * Construct a directory root from a filesystem path expressed as a string,
	 * and an appropriate file filter. In converting the path to a File object,
	 * an IOException may arise if it is an invalid path.
	 * 
	 * @param path
	 *            --- location of directory on filesystem, expressed as a native
	 *            path (i.e. separated using File.separatorChar, etc)
	 * @param filter
	 *            --- filter which determines what constitutes a valid entry for
	 *            this directory.
	 * @throws IOException
	 */
	public SourceDirectoryRoot(File dir, BinaryDirectoryRoot bindir) throws IOException {
		this.dir = dir;			
		this.bindir = bindir;
	}
	
	public boolean exists(PkgID pkg) throws IOException {		
		File location = new File(dir + File.separator + pkg.fileName());
		return location.exists() && location.isDirectory();
	}
	
	public List<Path.Entry> list(PkgID pkg) throws IOException {		
		File location = new File(dir + File.separator + pkg.fileName());

		if (location.exists() && location.isDirectory()) {
			ArrayList<Path.Entry> entries = new ArrayList<Path.Entry>();

			for (File file : location.listFiles(filter)) {
				String filename = file.getName();
				String name = filename.substring(0, filename.lastIndexOf('.'));
				ModuleID mid = new ModuleID(pkg, name);
				Entry srcEntry = new Entry(mid, file);
				Entry binEntry = null;
				
				// Now, see if there exists a binary version of this file which has
				// a modification date no earlier. Binary files are always preferred
				// over source entries.
				
				if (bindir != null) {
					binEntry = bindir.lookup(mid);					
				} else {
					File binFile = new File(name + ".class");
					if(binFile.exists()) {
						binEntry = new Entry(mid,binFile);
					}
				}
				
				if (binEntry != null && binEntry.lastModified() >= srcEntry.lastModified()) {
					entries.add(binEntry);
				} else {
					entries.add(srcEntry);
				}
			}			
			return entries;
		} else {			
			return Collections.EMPTY_LIST;
		}
	}
	
	public Entry lookup(ModuleID mid) throws IOException {
		File srcFile = new File(dir + File.separator + mid.fileName()
				+ ".whiley");
		if (srcFile.exists()) {
			Entry srcEntry = new Entry(mid, srcFile);
			Entry binEntry = null;
			
			// Now, see if there exists a binary version of this file which has
			// a modification date no earlier. Binary files are always preferred
			// over source entries.
			
			if (bindir != null) {
				binEntry = bindir.lookup(mid);					
			} else {
				File binFile = new File(dir + File.separator + mid.fileName()
						+ ".class");				
				if(binFile.exists()) {
					binEntry = new Entry(mid,binFile);
				}
			}
			
			if (binEntry != null && binEntry.lastModified() >= srcEntry.lastModified()) {
				return binEntry;
			} else {
				return srcEntry;
			}
		} else {
			return null; // not found
		}
	}

	public String toString() {
		return dir.getPath();
	}
}
