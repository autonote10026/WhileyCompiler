// Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
// All rights reserved.
//
// This software may be modified and distributed under the terms
// of the BSD license.  See the LICENSE file for details.

package wyc.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

import wybs.util.StdProject;
import wyc.Activator;
import wyc.builder.CompileTask;
import wyc.commands.Compile;
import wyc.lang.WhileyFile;
import wycc.util.ArrayUtils;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.DirectoryRoot;
import wyfs.util.VirtualRoot;

/**
 * This class implements an baseline ant task for compiling whiley files via ant
 * and an appropriate build.xml file. The following illustrates how this task
 * can be used in a build.xml file:
 *
 * <pre>
 * <taskdef name="wyc" classname="wyc.util.AntTask" classpath="lib/wyc.jar"/>
 * <wyc whileydir="stdlib" includes="whiley\/**\/*.whiley" excludes="whiley/io/**"/>
 * </pre>
 *
 * <p>
 * The first line defines the new task, and requires <code>wyc.jar</code> (which
 * contains this class) to be on the classpath; The second invokes the task to
 * compile all files rooted in the <code>stdlib/</code> directory which are in
 * the <code>whiley/</code> package, excluding those in <code>whiley/io</code>.
 * </p>
 *
 * <p>
 * <b>NOTE:</b> this class is intended to be overriden by the compiler backends
 * which compile Wyil bytecodes to different architectures (e.g. JVM, C,
 * JavaScript, etc).
 * </p>
 *
 *
 * @author David J. Pearce
 *
 */
public class AntTask extends MatchingTask {

	private Compile command;

	/**
	 * Construct a new instance of this command.
	 *
	 * @param registry
	 *            The content registry being used to match files to content
	 *            types.
	 * @throws IOException
	 */
	public AntTask() {
		Content.Registry registry = new Activator.Registry();
		this.command = new Compile(registry, Logger.NULL);

	}

	// =======================================================================
	// Configuration
	// =======================================================================

	public void setWhileypath(String dirs) throws IOException {
		// FIXME: could use org.apache.tools.ant.types.Path path here, as ant
		// autoconverts
		command.setWhileypath(dirs);
	}

	public void setWhileydir(File dir) throws IOException {
		command.setWhileydir(dir);
	}

	public void setWyildir(File dir) throws IOException {
		command.setWyildir(dir);
	}

	public void setWyaldir(File dir) throws IOException {
		command.setWyaldir(dir);
	}

	public void setVerify(boolean b) {
		command.setVerify(b);
	}

	public void setVerbose(boolean b) {
		command.setVerbose(b);
	}

	@Override
	public void setIncludes(String includes) {
		String[] split = includes.split(",");
		Content.Filter<WhileyFile> whileyFilter = null;
		for (String s : split) {
			if (s.endsWith(".whiley")) {
				String name = s.substring(0, s.length() - 7);
				Content.Filter<WhileyFile> nf1 = Content.filter(name, WhileyFile.ContentType);
				whileyFilter = whileyFilter == null ? nf1 : Content.or(nf1, whileyFilter);
			}
		}
		if (whileyFilter != null) {
			command.setIncludes(whileyFilter);
		}
	}

	@Override
	public void setExcludes(String excludes) {

	}

	// =======================================================================
	// Execute
	// =======================================================================

	@Override
	public void execute() throws BuildException {
		try {
			List<Path.Entry<WhileyFile>> files = command.getModifiedSourceFiles();
			Compile.Result r = command.execute(files);
			if (r == Compile.Result.SUCCESS) {
				log("Compiled " + files.size() + " source file(s)");
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
}
