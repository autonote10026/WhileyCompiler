// Copyright 2011 The Whiley Project Developers
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package wyc.task;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;

import wybs.lang.Build;
import wybs.util.AbstractBuildTask;
import wybs.util.AbstractCompilationUnit.Name;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.io.WhileyFileParser;
import wyc.lang.WhileyFile;
import wycc.util.Logger;
import wyfs.lang.Path;
import wyil.check.*;
import wyil.lang.Compiler;
import wyil.lang.WyilFile;
import wyil.transform.MoveAnalysis;
import wyil.transform.NameResolution;
import wyil.transform.RecursiveTypeAnalysis;

/**
 * Responsible for managing the process of turning source files into binary code
 * for execution. Each source file is passed through a pipeline of stages that
 * modify it in a variet y of ways. The main stages are:
 * <ol>
 * <li>
 * <p>
 * <b>Lexing and Parsing</b>, where the source file is converted into an
 * Abstract Syntax Tree (AST) representation.
 * </p>
 * </li>
 * <li>
 * <p>
 * <b>Name Resolution</b>, where the fully qualified names of all external
 * symbols are determined.
 * </p>
 * </li>
 * <li>
 * <p>
 * <b>Type Propagation</b>, where the types of all expressions are determined by
 * propagation from e.g. declared parameter types.
 * </p>
 * </li>
 * <li>
 * <p>
 * <b>WYIL Generation</b>, where the the AST is converted into the Whiley
 * Intermediate Language (WYIL). A number of passes are then made over this
 * before it is ready for code generation.
 * </p>
 * </li>
 * <li>
 * <p>
 * <b>Code Generation</b>. Here, the executable code is finally generated. This
 * could be Java bytecode, or something else (e.g. JavaScript).
 * </p>
 * </li>
 * </ol>
 * Every stage of the compiler can be configured by setting various options.
 * Stages can also be bypassed (typically for testing) and new ones can be
 * added.
 *
 * @author David J. Pearce
 *
 */
public final class CompileTask extends AbstractBuildTask<WhileyFile, WyilFile> {
	private final Logger logger;
	/**
	 * Specify whether verification enabled or not
	 */
	private boolean verification;
	/**
	 * Specify whether counterexample generation is enabled or not
	 */
	private boolean counterexamples;

	/**
	 * The source root to find Whiley files. This is far from ideal.
	 */
	private final Path.Root sourceRoot;

	/**
	 * Type checking stage. After name resolution, this must run before any other
	 * stage, as all other stages depend on it.
	 */
	private final FlowTypeCheck checker;

	/**
	 * The set of compiler checks. These check the generated WyilFile is valid.
	 */
	private final Compiler.Check[] stages;

	/**
	 * Optional stage
	 */
	private final VerificationCheck verifier;

	/**
	 * The set of transforms. These perform certain transformations on the generated
	 * WyilFile.
	 */
	private final Compiler.Transform[] transforms;

	public CompileTask(Build.Project project, Path.Root sourceRoot, Path.Entry<WyilFile> target,
			Collection<Path.Entry<WhileyFile>> sources) throws IOException {
		super(project, target, sources);
		// FIXME: shouldn't need source root
		this.sourceRoot = sourceRoot;
		// Instantiate type checker
		this.checker = new FlowTypeCheck();
		// Instantiate other checks
		this.stages = new Compiler.Check[] {
				new DefiniteAssignmentCheck(),
				new DefiniteUnassignmentCheck(),
				new FunctionalCheck(),
				new SignatureCheck(),
				new StaticVariableCheck(),
				new AmbiguousCoercionCheck()
			};
		// Extract the logger for debug information
		this.logger = project.getEnvironment().getLogger();
		//
		this.verifier = new VerificationCheck(project,target);
		// Instantiate various transformations
		this.transforms = new Compiler.Transform[] { new MoveAnalysis(), new RecursiveTypeAnalysis() };
	}

	public CompileTask setVerification(boolean flag) {
		this.verification = flag;
		return this;
	}

	public CompileTask setCounterExamples(boolean flag) {
		this.counterexamples = flag;
		return this;
	}

	@Override
	public Callable<Boolean> initialise() throws IOException {
		// Extract target and source files for compilation. This is the component which
		// requires I/O.
		WyilFile wyil = target.read();
		WhileyFile[] whileys = new WhileyFile[sources.size()];
		for (int i = 0; i != whileys.length; ++i) {
			whileys[i] = sources.get(i).read();
		}
		// Construct the lambda for subsequent execution. This will eventually make its
		// way into some kind of execution pool, possibly for concurrent execution with
		// other tasks.
		return () -> execute(wyil, whileys);
	}

	/**
	 * The business end of a compilation task. The intention is that this
	 * computation can proceed without performing any blocking I/O. This means it
	 * can be used in e.g. a forkjoin task safely.
	 *
	 * @param target
	 *            --- The WyilFile being written.
	 * @param sources
	 *            --- The WhileyFiles being compiled.
	 * @return
	 */
	public boolean execute(WyilFile target, WhileyFile... sources) {
		Task timer = new Task(logger);
		// FIXME: this is something of a hack to handle the fact that this is not an
		// incremental compiler! Basically, we always start from scratch no matter what.
		WyilFile.Decl.Module module = (WyilFile.Decl.Module) target.getRootItem();
		target.setRootItem(new WyilFile.Decl.Module(module.getName(), new Tuple<>(), new Tuple<>(), new Tuple<>()));
		//
		boolean r = true;
		// Parse source files into target
		for (int i = 0; i != sources.length; ++i) {
			// NOTE: this is somehow where we work out the initial deltas for incremental
			// compilation.
			WhileyFile source = sources[i];
			WhileyFileParser wyp = new WhileyFileParser(target, source);
			//
			r &= wyp.read();
		}
		timer.split("Parsed " + sources.length + " file(s)");
		// Perform name resolution.
		try {
			r = r && new NameResolution(project, target).apply();
		} catch(IOException e) {
			// FIXME: this is clearly broken.
			throw new RuntimeException(e);
		}
		timer.split("NameResolution");
		// ========================================================================
		// Flow Type Checking
		// ========================================================================
		r = r && checker.check(target);
		timer.split("FlowTypeCheck");
		// ========================================================================
		// Compiler Checks
		// ========================================================================
		for (int i = 0; i != stages.length; ++i) {
			r = r && stages[i].check(target);
			timer.split(stages[i].getClass().getSimpleName());
		}
		if(verification) {
			r = r && verifier.check(target,counterexamples);
			timer.split("Verification");
		}
		// Transforms
		if (r) {
			// Only apply if previous stages have all passed.
			for (int i = 0; i != transforms.length; ++i) {
				transforms[i].apply(target);
				timer.split(transforms[i].getClass().getSimpleName());
			}
		}
		// Collect garbage
		//target.gc();
		timer.done("Compiled "  + sources.length + " whiley file(s)");
		// Done
		return r;
	}

	private static class Task {
		private final Logger logger;
		private final long time;
		private final long memory;
		private long splitTime;
		private long splitMemory;

		public Task(Logger logger) {
			this.logger = logger;
			this.splitTime = System.currentTimeMillis();
			this.splitMemory = Runtime.getRuntime().freeMemory();
			this.time = splitTime;
			this.memory = splitMemory;
		}

		public void split(String msg) {
			long t = splitTime;
			long m = splitMemory;
			splitTime = System.currentTimeMillis();
			splitMemory = Runtime.getRuntime().freeMemory();
			logger.logTimedMessage(msg, splitTime-t, splitMemory-m);
		}

		public void done(String msg) {
			long t = System.currentTimeMillis();
			long m = Runtime.getRuntime().freeMemory();
			logger.logTimedMessage(msg, t-time, m-memory);
		}
	}
}
