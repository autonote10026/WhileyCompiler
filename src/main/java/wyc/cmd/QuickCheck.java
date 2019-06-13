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
package wyc.cmd;

import static wyil.lang.WyilFile.DECL_function;
import static wyil.lang.WyilFile.DECL_method;
import static wyil.lang.WyilFile.DECL_rectype;
import static wyil.lang.WyilFile.DECL_type;
import static wyil.lang.WyilFile.TYPE_array;
import static wyil.lang.WyilFile.TYPE_bool;
import static wyil.lang.WyilFile.TYPE_byte;
import static wyil.lang.WyilFile.TYPE_function;
import static wyil.lang.WyilFile.TYPE_int;
import static wyil.lang.WyilFile.TYPE_method;
import static wyil.lang.WyilFile.TYPE_nominal;
import static wyil.lang.WyilFile.TYPE_null;
import static wyil.lang.WyilFile.TYPE_property;
import static wyil.lang.WyilFile.TYPE_record;
import static wyil.lang.WyilFile.TYPE_reference;
import static wyil.lang.WyilFile.TYPE_staticreference;
import static wyil.lang.WyilFile.TYPE_union;
import static wyil.lang.WyilFile.TYPE_variable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import jmodelgen.core.Domain;
import jmodelgen.util.Domains;
import wybs.lang.Build;
import wybs.lang.SyntacticItem;
import wybs.util.AbstractCompilationUnit.Tuple;
import wybs.util.AbstractCompilationUnit.Value;
import wyc.Activator;
import wyc.util.ErrorMessages;
import wycc.WyProject;
import wycc.cfg.Configuration;
import wycc.cfg.Configuration.Schema;
import wycc.lang.Command;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.Interpreter;
import wyil.interpreter.Interpreter.CallStack;
import wyil.lang.WyilFile;
import wyil.lang.WyilFile.Decl;
import wyil.lang.WyilFile.Expr;
import wyil.lang.WyilFile.QualifiedName;
import wyil.lang.WyilFile.Type;
import wyil.lang.WyilFile.Type.Callable;

/**
 * <p>
 * Implements a command for randomised testing of functions and methods based on
 * their specifications. This is roughly in line with the "QuickCheck" series of
 * tools, though customised for Whiley. As an example, consider the following
 * function:
 * </p>
 *
 * <pre>
 * function max(int[] items) -> (int max)
 * requires |items| > 0
 * ensures ...:
 *     ...
 * </pre>
 *
 * <p>
 * This function is intended for computing the max of a (non-empty) integer
 * array. The quick check functionality will generate a number of random inputs
 * for this function. In this case, it will generate random arrays upto a given
 * size (say, maximum of three elements). Example inputs generated would be then
 * <code>[]</code>, <code>[1]</code>, <code>[1,2]</code>,<code>[1,2,3]</code>
 * and many more. In fact, the decision as to what integer values to generate is
 * determined by the underling range of integers being considered.
 * </p>
 * <p>
 * Having generated one or more candidate inputs, these will then be tested
 * against the precondition to check whether they are "meaningless" or not. That
 * is, whether or not they are valid inputs for this particular function. Those
 * which fail the precondition (including all invariants on parameter types) are
 * rejected. Those remaining are then passed into the function, and any faults
 * arising are noted along with any inputs for which the generated output(s)
 * fail the post-condition.
 * </p>
 *
 *
 *
 * @author David J. Pearce
 *
 */
public class QuickCheck implements Command {
	public static final Context DEFAULT_CONTEXT = new Context(-3, 3, 3, 3, 2, 2, new String[0], true, Integer.MAX_VALUE,
			Long.MAX_VALUE);
	// Configuration Options
	public static Trie MIN_CONFIG_OPTION = Trie.fromString("check/min");
	public static Trie MAX_CONFIG_OPTION = Trie.fromString("check/max");
	public static Trie LENGTH_CONFIG_OPTION = Trie.fromString("check/length");
	public static Trie DEPTH_CONFIG_OPTION = Trie.fromString("check/depth");
	public static Trie WIDTH_CONFIG_OPTION = Trie.fromString("check/width");
	public static Trie ROTATION_CONFIG_OPTION = Trie.fromString("check/rotation");
	public static Trie LIMIT_CONFIG_OPTION = Trie.fromString("check/limit");
	public static Trie METHODS_CONFIG_OPTION = Trie.fromString("check/methods");
	public static Trie TIMEOUT_CONFIG_OPTION = Trie.fromString("check/timeout");
	public static Trie IGNORES_CONFIG_OPTION = Trie.fromString("check/ignores");
	// Configuration Defaults
	public static Value.Int MIN_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getIntegerMinimum());
	public static Value.Int MAX_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getIntegerMaximum());
	public static Value.Int LENGTH_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getMaxArrayLength());
	public static Value.Int DEPTH_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getRecursiveTypeDepth());
	public static Value.Int WIDTH_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getAliasingWidth());
	public static Value.Int ROTATION_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getLambdaWidth());
	public static Value.Int LIMIT_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getTestLimit());
	public static Value.Bool METHODS_DEFAULT = new Value.Bool(DEFAULT_CONTEXT.getMethodsFlag());
	public static Value.Int TIMEOUT_DEFAULT = new Value.Int(DEFAULT_CONTEXT.getTimeout());
	public static Value.Array IGNORES_DEFAULT = new Value.Array();
	/**
	 * The descriptor for this command.
	 */
	public static final Command.Descriptor DESCRIPTOR = new Command.Descriptor() {
		@Override
		public String getName() {
			return "check";
		}

		@Override
		public String getDescription() {
			return "Perform randomised testing of functions and methods";
		}

		@Override
		public List<Option.Descriptor> getOptionDescriptors() {
			return Arrays.asList(
					Command.OPTION_NONNEGATIVE_INTEGER("limit",
						"Specify limit on test inputs to try for each function or method"),
					Command.OPTION_NONNEGATIVE_INTEGER("min",
						"Specify minimum integer value which can be generated"),
					Command.OPTION_NONNEGATIVE_INTEGER("max",
						"Specify maximum integer value which can be generated"),
					Command.OPTION_NONNEGATIVE_INTEGER("length",
						"Specify maximum length of a generated array"),
					Command.OPTION_NONNEGATIVE_INTEGER("depth",
						"Specify maximum depth of a recurisive type"),
					Command.OPTION_FLAG("methods", "Specify whether or not to include methods"),
					Command.OPTION_NONNEGATIVE_INTEGER("timeout",
							"Specify timeout (in seconds) to spend on each function or method")
					);
		}

		@Override
		public Schema getConfigurationSchema() {
			return Configuration.fromArray(
					Configuration.UNBOUND_INTEGER(MIN_CONFIG_OPTION, "Specify minimum integer value which can be generated",
							MIN_DEFAULT),
					Configuration.UNBOUND_INTEGER(MAX_CONFIG_OPTION, "Specify maximum integer value which can be generated",
							MAX_DEFAULT),
					Configuration.UNBOUND_INTEGER(LENGTH_CONFIG_OPTION, "Specify maximum length of a generated array", LENGTH_DEFAULT),
					Configuration.UNBOUND_INTEGER(DEPTH_CONFIG_OPTION, "Specify maximum depth of a recurisive type",
							DEPTH_DEFAULT),
					Configuration.UNBOUND_INTEGER(WIDTH_CONFIG_OPTION, "Specify aliasing width to use for reference types",
							WIDTH_DEFAULT),
					Configuration.UNBOUND_INTEGER(ROTATION_CONFIG_OPTION, "Specify rotation to use for synthesized lambdas",
							ROTATION_DEFAULT),
					Configuration.UNBOUND_BOOLEAN(METHODS_CONFIG_OPTION, "Specify whether or not to include methods",
							METHODS_DEFAULT),
					Configuration.UNBOUND_INTEGER(LIMIT_CONFIG_OPTION, "Specify limit on test inputs to try for each function or method",
							LIMIT_DEFAULT),
					Configuration.UNBOUND_INTEGER(TIMEOUT_CONFIG_OPTION, "Specify timeout (in seconds) to spend on each function or method",
							TIMEOUT_DEFAULT),
					Configuration.UNBOUND_STRING_ARRAY(IGNORES_CONFIG_OPTION,
							"Specify items (e.g. functions or methods) which should be ignored", IGNORES_DEFAULT));
		}

		@Override
		public List<Descriptor> getCommands() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public Command initialise(Command environment, Configuration configuration) {
			return new QuickCheck(((WyProject) environment).getBuildProject(), configuration, System.out, System.err);
		}

	};

	/**
	 * Provides a generic place to which normal output should be directed. This
	 * should eventually be replaced.
	 */
	private final PrintStream sysout;

	/**
	 * Provides a generic place to which error output should be directed. This
	 * should eventually be replaced.
	 */
	private final PrintStream syserr;

	/**
	 * The configuration associated with this command.
	 */
	private final Configuration configuration;

	/**
	 * The enclosing project for this build
	 */
	private final Build.Project project;

	/**
	 * The interpreter instance used for executing code.
	 */
	private ExtendedInterpreter interpreter;

	/**
	 * Cache of previously computed values. This is useful for reducing memory
	 * requirements. Furthermore, it is necessary to ensure that aliasing bugs are
	 * identified.
	 */
	private final HashMap<Type,Domain<RValue>> cache;

	/**
	 * Provides the output chanel for information about the quick check process.
	 */
	private StructuredLogger<LogEntry> logger;

	public QuickCheck(Build.Project project, Configuration configuration, OutputStream sysout, OutputStream syserr) {
		this.project = project;
		this.configuration = configuration;
		this.sysout = new PrintStream(sysout);
		this.syserr = new PrintStream(syserr);
		this.cache = new HashMap<>();
		// Default logger just reports up to project logger
		this.logger = new StructuredLogger<LogEntry>() {

			@Override
			public void logTimedMessage(LogEntry result, long time, long memory) {
				project.getLogger().logTimedMessage(result.toString(), time, memory);
			}

		};
	}

	@Override
	public Descriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public void initialise() {

	}

	@Override
	public void finalise() {

	}

	public void setLogger(StructuredLogger<LogEntry> logger) {
		this.logger = logger;
	}

	@Override
	public boolean execute(Template template) throws Exception {
		// Extract configuration options
		int minInteger = configuration.get(Value.Int.class,MIN_CONFIG_OPTION).unwrap().intValue();
		int maxInteger = configuration.get(Value.Int.class,MAX_CONFIG_OPTION).unwrap().intValue();
		int maxArrayLength = configuration.get(Value.Int.class,LENGTH_CONFIG_OPTION).unwrap().intValue();
		int maxTypeDepth = configuration.get(Value.Int.class,DEPTH_CONFIG_OPTION).unwrap().intValue();
		int maxAliasingWidth = configuration.get(Value.Int.class,WIDTH_CONFIG_OPTION).unwrap().intValue();
		int maxRotationWidth = configuration.get(Value.Int.class,ROTATION_CONFIG_OPTION).unwrap().intValue();
		int testLimit = configuration.get(Value.Int.class,LIMIT_CONFIG_OPTION).unwrap().intValue();
		long timeout = configuration.get(Value.Int.class,TIMEOUT_CONFIG_OPTION).unwrap().longValue();
		boolean methodsFlag = configuration.get(Value.Bool.class,METHODS_CONFIG_OPTION).unwrap();
		String[] ignores = toStringArray(configuration.get(Value.Array.class,IGNORES_CONFIG_OPTION));
		Trie pkg = Trie.fromString(configuration.get(Value.UTF8.class, Activator.PKGNAME_CONFIG_OPTION).unwrap());
		// Extract command-line options
		Command.Options options = template.getOptions();
		//
		if(options.has("limit")) {
			testLimit = options.get("limit", Integer.class);
		}
		if(options.has("min")) {
			minInteger = options.get("min", Integer.class);
		}
		if(options.has("max")) {
			maxInteger = options.get("max", Integer.class);
		}
		if(options.has("length")) {
			maxArrayLength = options.get("length", Integer.class);
		}
		if(options.has("depth")) {
			maxTypeDepth = options.get("depth", Integer.class);
		}
		if(options.has("methods")) {
			methodsFlag = options.get("methods", Boolean.class);
		}
		if(options.has("timeout")) {
			timeout = options.get("timeout", Integer.class);
		}
		// Specify directory where generated WyIL files are dumped.
		Trie target = Trie.fromString(configuration.get(Value.UTF8.class, Activator.TARGET_CONFIG_OPTION).unwrap());
		//
		Path.Root binaryRoot = project.getRoot().createRelativeRoot(target);
		//

		if (binaryRoot.exists(pkg, WyilFile.ContentType)) {
			// Yes, it does so reuse it.
			Path.Entry<WyilFile> binary = binaryRoot.get(pkg, WyilFile.ContentType);
			// Read the target wyilfile
			WyilFile wf = binary.read();
			// Construct initial context
			Context context = DEFAULT_CONTEXT.setIntegerRange(minInteger, maxInteger).setArrayLength(maxArrayLength)
					.setTypeDepth(maxTypeDepth).setAliasingWidth(maxAliasingWidth).setLambdaWidth(maxRotationWidth)
					.setIgnores(ignores).setTestLimit(testLimit).setTimeout(timeout);
			logger.logTimedMessage(new Summary(context), 0,0);
			// Perform the check
			boolean OK = check(wf, context);
			//
			if(!OK) {
				// FIXME: this does not seem like a good solution :|
				List<wybs.lang.Build.Task> tasks = project.getTasks();
				// Look for error messages
				for (wybs.lang.Build.Task task : tasks) {
					wycc.commands.Build.printSyntacticMarkers(syserr, task.getSources(), task.getTarget());
				}
			}
			// Print out any
			return OK;
		} else {
			return false;
		}
	}

	public boolean check(WyilFile parent, Context context) throws IOException {
		// Initialise Interpreter
		this.interpreter = new ExtendedInterpreter(this.syserr, context);
		// Construct extended context
		ExtendedContext eContext = interpreter.getExtendedContext();
		// Initialise by context
		eContext.initialise(project,parent);
		//
		return check(parent, eContext);
	}

	public boolean check(WyilFile parent, ExtendedContext context) throws IOException {
		boolean OK = true;
		for (Decl.Unit unit : parent.getModule().getUnits()) {
			OK &= check(parent,unit, context);
		}
		return OK;
	}

	private boolean check(WyilFile parent, Decl.Unit unit, ExtendedContext context) throws IOException {
		boolean OK = true;
		//
		for (Decl d : unit.getDeclarations()) {
			if (d instanceof Decl.Named) {
				OK &= check((Decl.Named) d, parent, context);
			}
		}

		return OK;
	}

	private boolean check(Decl.Named d, WyilFile parent, ExtendedContext context) throws IOException {
		// Check whether declaration should be checked or not
		if(context.isIgnored(d)) {
			// No, this declaration was explicitly marked as ignored.
			logger.logTimedMessage(new Skipped(d), 0,0);
			return true;
		} else {
			// Yes, this declaration should be checked.
			Runtime runtime = Runtime.getRuntime();
			long time = System.currentTimeMillis();
			long memory = runtime.freeMemory();
			try {
				switch (d.getOpcode()) {
				case DECL_method:
				case DECL_function:
					return check((Decl.FunctionOrMethod) d, parent, context);
				case DECL_rectype:
				case DECL_type:
					return check((Decl.Type) d, context);
				}
				return true;
			} catch (Interpreter.TimeoutException e) {
				// Done
				time = System.currentTimeMillis() - time;
				memory = memory - runtime.freeMemory();
				logger.logTimedMessage(new Timeout(d,context.getTimeout()), time, memory);
				return false;
			} catch(Throwable t) {
				time = System.currentTimeMillis() - time;
				memory = memory - runtime.freeMemory();
				logger.logTimedMessage(new InternalFailure(d,t), time, memory);
				return false;
			}
		}
	}

	/**
	 * Apply quick check to a given function or method. First, we generate inputs
	 * which meet the preconditions of the function or method in question. Second,
	 * we execute the function in question over the given inputs and observe any
	 * runtime faults arising.
	 *
	 * @param fm
	 * @param parent
	 * @param context
	 * @return
	 * @throws IOException
	 */
	private boolean check(Decl.FunctionOrMethod fm, WyilFile parent, ExtendedContext context) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		long time = System.currentTimeMillis();
		long memory = runtime.freeMemory();
		// Set default result
		boolean result = true;
		// Check whether skipping  method
		if(fm instanceof Decl.Method && !context.getMethodsFlag()) {
			// Yes, skip this method
			time = System.currentTimeMillis() - time;
			memory = memory - runtime.freeMemory();
			logger.logTimedMessage(new Skipped(fm), time, memory);
		} else {
			// Get appropriate generators for each parameter
			Domain<RValue>[] generators = constructGenerators(fm.getParameters(), context);
			//
			List<RValue[]> inputs = generateValidInputs(fm.getRequires(), fm.getParameters(), context, generators);
			//
			long total = calculateTotalInputs(generators);
			//
			CallStack frame = context.getFrame().enter(fm);
			//
			for(RValue[] args : inputs) {
				// Invoke the method!!
				if (!execute(parent, fm.getQualifiedName(), fm.getType(), frame.clone(), args)) {
					// Failed, so exit early
					result=false;
					break;
				}
			}
			time = System.currentTimeMillis() - time;
			memory = memory - runtime.freeMemory();
			//
			logger.logTimedMessage(new Result(fm, result, inputs.size(), total), time, memory);
		}
		return result;
	}

	private boolean execute(WyilFile context, QualifiedName name, Type.Callable signature, CallStack frame, RValue... args) throws IOException {
		// FIXME: approach for constructing stack frame inefficient!
		//
		try {
			interpreter.execute(name, signature, frame, args);
			//
			return true;
		} catch (Interpreter.RuntimeError e) {
			// Add appropriate syntax error to the syntactic item where the error arose.
			ErrorMessages.syntaxError(e.getElement(), e.getErrorCode());
			// FIXME: need better error reporting here
			//System.out.println("FRAME: " + name + "(" + Arrays.deepToString(args) + "," + e.getFrame().getLocals() + ")");
			// Done
			return false;
		}
	}

	private boolean check(Decl.Type t, ExtendedContext context) {
		Runtime runtime = Runtime.getRuntime();
		long time = System.currentTimeMillis();
		long memory = runtime.freeMemory();
		//
		CallStack frame = context.getFrame().enter(t);
		// Get an appropriate generator for the underlying type
		Domain<RValue> generator = constructGenerator(t.getType(), context);
		// iterate through all values in the generator to see whether any pass the
		// invariant and, hence, are valid instances of this invariant.
		Domain<RValue> domain = generateValidInputs(t.getInvariant(), t.getVariableDeclaration(), generator, context, frame);
 		//
		time = System.currentTimeMillis() - time;
		memory = memory - runtime.freeMemory();
		//
		logger.logTimedMessage(new Result(t, true, domain.size(), generator.size()), time, memory);
		//
		return domain.size() > 0;
	}

	/**
	 * Generate the valid inputs for a given set of predicates. This is called to
	 * determine the inputs for a given function or method. Inputs which cause
	 * faults (e.g. out-of-bounds errors) are discarded as these are of no interest.
	 * In other words, valid inputs must be defined.
	 *
	 * @param predicate
	 * @param variable
	 * @param domain
	 * @param frame
	 * @return
	 */
	private Domain<RValue> generateValidInputs(Tuple<Expr> predicate, Decl.Variable variable, Domain<RValue> domain, ExtendedContext context, CallStack frame) {
		//
		ArrayList<RValue> results = new ArrayList<>();
		//
		if(context.getTestLimit() != Integer.MAX_VALUE) {
			domain = Domains.Sample(domain,context.getTestLimit());
		}
		//
		for(int i=0;i!=domain.size();++i) {
			RValue input = domain.get(i);
			try {
				// Construct the stack frame
				frame.putLocal(variable.getName(), input);
				// execute invariant
				if(execute(predicate,frame)) {
					results.add(input);
				}
			} catch(Interpreter.RuntimeError e) {

			}
		}
		// FIXME: not very efficient?
		return Domains.Finite(results.toArray(new RValue[results.size()]));
	}

	/**
	 * Generate the valid inputs for a given set of predicates. This is called to
	 * determine the inputs for a given function or method. Inputs which cause
	 * faults (e.g. out-of-bounds errors) are discarded as these are of no interest.
	 * In other words, valid inputs must be defined.
	 *
	 * @param predicate
	 * @param variables
	 * @param context
	 * @param generators
	 * @return
	 */
	private List<RValue[]> generateValidInputs(Tuple<Expr> predicate, Tuple<Decl.Variable> variables, ExtendedContext context, Domain<RValue>... generators) {
		if(variables.size() != generators.length) {
			throw new IllegalArgumentException("invalid number of generators");
		}
		Domain<RValue[]> domain = Domains.Product(generators);
		//
		if(context.getTestLimit() != Integer.MAX_VALUE) {
			domain = Domains.Sample(domain,context.getTestLimit());
		}
		//
		CallStack frame = context.getFrame();
		//
		ArrayList<RValue[]> results = new ArrayList<>();
		//
		for(long i=0;i!=domain.size();++i) {
			RValue[] inputs = domain.get(i);
			try {
				// Construct the stack frame
				for (int j = 0; j != inputs.length; ++j) {
					// update environment
					frame.putLocal(variables.get(j).getName(), inputs[j]);
				}
				// execute invariant
				if(execute(predicate,frame)) {
					results.add(inputs);
				}
			} catch(Interpreter.RuntimeError e) {
			}
		}
		//
		return results;
	}

	/**
	 * Execute a sequence of boolean expressions in a given context. If one fails,
	 * then execution is terminated accordingly.
	 *
	 * @param predicate
	 * @param frame
	 * @return
	 */
	private boolean execute(Tuple<Expr> predicate, CallStack frame) {
		for (int i = 0; i != predicate.size(); ++i) {
			if (!execute(predicate.get(i), frame)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Execute a given boolean expression in a given context.
	 *
	 * @param predicate
	 * @param frame
	 * @return
	 */
	private boolean execute(Expr predicate, CallStack frame) {
		RValue.Bool b = interpreter.executeExpression(RValue.Bool.class,predicate,frame);
		return b.boolValue();
	}

	private Domain<RValue>[] constructGenerators(Tuple<Decl.Variable> parameters, ExtendedContext context) {
		Domain<RValue>[] generators = new Domain[parameters.size()];
		//
		for (int i = 0; i != parameters.size(); ++i) {
			generators[i] = constructGenerator(parameters.get(i).getType(), context);
		}
		return generators;
	}

	/**
	 * Construct a generator (i.e. domain) for a given type. This is responsible for
	 * enumerating elements of that type within some finite bounds. Observe that
	 * this caches the constructed domains for two reasons: firstly, to save memory;
	 * secondly, to ensure possible aliasing between reference types.
	 *
	 * @param type
	 * @param context
	 * @return
	 */
	private Domain<RValue> constructGenerator(Type type, ExtendedContext context) {
		Domain<RValue> result = cache.get(type);
		if (result == null) {
			switch (type.getOpcode()) {
			case TYPE_null:
				result = constructGenerator((Type.Null) type, context);
				break;
			case TYPE_bool:
				result = constructGenerator((Type.Bool) type, context);
				break;
			case TYPE_byte:
				result = constructGenerator((Type.Byte) type, context);
				break;
			case TYPE_int:
				result = constructGenerator((Type.Int) type, context);
				break;
			case TYPE_array:
				result = constructGenerator((Type.Array) type, context);
				break;
			case TYPE_record:
				result = constructGenerator((Type.Record) type, context);
				break;
			case TYPE_nominal:
				result = constructGenerator((Type.Nominal) type, context);
				break;
			case TYPE_union:
				result = constructGenerator((Type.Union) type, context);
				break;
			case TYPE_variable:
				result = constructGenerator((Type.Variable) type, context);
				break;
			case TYPE_staticreference:
			case TYPE_reference:
				result = constructGenerator((Type.Reference) type, context);
				break;
			case TYPE_function:
				result = constructGenerator((Type.Function) type, context);
				break;
			case TYPE_method:
				result = constructGenerator((Type.Method) type, context);
				break;
			case TYPE_property:
			default:
				// NOTE: this should be dead code.
				throw new RuntimeException("unknown type encountered (" + type + ")");
			}
			// Store the computed result in the cache. This is important to reduce memory
			// footprints. In some cases, this reduction can be quite significant.
			cache.put(type,result);
		}
		return result;
	}

	private Domain<RValue> constructGenerator(Type.Null type, ExtendedContext context) {
		return Domains.Finite(RValue.Null);
	}

	private Domain<RValue> constructGenerator(Type.Bool type, ExtendedContext context) {
		return Domains.Finite(RValue.False, RValue.True);
	}

	private Domain<RValue> constructGenerator(Type.Int type, ExtendedContext context) {
		return new Domains.Adaptor<Integer, RValue>(Domains.Int(context.getIntegerMinimum(), context.getIntegerMaximum())) {
			@Override
			public RValue.Int get(Integer i) {
				return RValue.Int(BigInteger.valueOf(i));
			}
		};
	}

	private Domain<RValue> constructGenerator(Type.Byte type, ExtendedContext context) {
		return new Domains.Adaptor<Integer,RValue>(Domains.Int(context.getIntegerMinimum(), context.getIntegerMaximum())) {
			@Override
			public RValue.Byte get(Integer value) {
				return RValue.Byte((byte) (int) value);
			}
		};
	}

	private Domain<RValue> constructGenerator(Type.Array type, ExtendedContext context) {
		Domain<RValue> generator = constructGenerator(type.getElement(), context);
		//
		return new Domains.Adaptor<List<RValue>,RValue>(Domains.List(0,context.getMaxArrayLength(), generator)) {
			@Override
			public RValue.Array get(List<RValue> list) {
				// FIXME: could be more efficient
				return RValue.Array(list.toArray(new RValue[list.size()]));
			}
		};
	}

	private Domain<RValue> constructGenerator(Type.Record type, ExtendedContext context) {
		// FIXME: need to support open records!
		Tuple<Type.Field> fields = type.getFields();
		Domain<RValue>[] generators = new Domain[fields.size()];
		// Construct a generator for each field
		for(int i=0;i!=fields.size();++i) {
			generators[i] = constructGenerator(fields.get(i).getType(), context);
		}
		//
		return new Domains.Adaptor<RValue[],RValue>(Domains.Product(generators)) {
			@Override
			public RValue.Record get(RValue[] vals) {
				Tuple<Type.Field> typeFields = type.getFields();
				RValue.Field[] fields = new RValue.Field[vals.length];
				//
				for (int i = 0; i != fields.length; ++i) {
					fields[i] = RValue.Field(typeFields.get(i).getName(), vals[i]);
				}
				// Done
				return RValue.Record(fields);
			}
		};
	}

	private Domain<RValue> constructGenerator(Type.Nominal type, ExtendedContext context) {
		Decl.Type decl = type.getLink().getTarget();
		int depth = context.depth(decl);
		//
		if (depth == context.getRecursiveTypeDepth()) {
			// NOTE: we've reached the maximum depth to explore for recursive types.
			// Therefore, we simply return the empty domain.
			return Domains.EMPTY;
		} else {
			// =============================================================================
			// FIXME: we're not using any form of cache here which could potentially improve
			// performance dramatically. A key aspect is that this must be for the concrete
			// type, to ensure that type parameters are included. Furthermore, handling of
			// recursive types needs to be done with care.
			// =============================================================================
			context.enter(decl);
			// Get an appropriate generator for the underlying type/
			Domain<RValue> generator = constructGenerator(type.getConcreteType(), context);
			//
			CallStack frame = context.getFrame().enter(decl);
			// iterate through all values in the generator to see whether any pass the
			// invariant and, hence, are valid instances of this invariant.
			Domain<RValue> domain = generateValidInputs(decl.getInvariant(), decl.getVariableDeclaration(), generator, context, frame);
			//
			context.leave(decl);
			//
			return domain;
		}
	}

	private Domain<RValue> constructGenerator(Type.Union type, ExtendedContext context) {
		// Construct generators for each subtype
		Domain<RValue>[] generators = new Domain[type.size()];
		for(int i=0;i!=type.size();++i) {
			generators[i] = constructGenerator(type.get(i), context);
		}
		// Return the union of all domains
		return Domains.Union(generators);
	}

	/**
	 * Construct a generator for a reference type. This is a bit challenging because
	 * we want to ensure that the *same* references are always returned from this
	 * domain. This sets up the possibility of aliasing.
	 *
	 * @param type
	 * @param context
	 * @return
	 */
	private Domain<RValue> constructGenerator(Type.Reference type, ExtendedContext context) {
		int width = context.getAliasingWidth();
		// NOTE: this is not done lazily which potentially could be problematic if
		// sampling was used.
		Domain<RValue> element = constructGenerator(type.getElement(), context);
		// Construct a unique object for each possible element value.
		RValue.Reference[] refs = new RValue.Reference[(int) element.size() * width];
		// Construct our set of references.
		for(int i = 0;i!=element.size();++i) {
			for(int j=0;j!=width;++j) {
				// Construct multiple references to a cell with the same value. This has the
				// effect of ensuring the possibility of references to different cells which
				// hold the same value.
				refs[(i*width)+j] = RValue.Reference(RValue.Cell(element.get(i)));
			}
		}
		//
		return Domains.Finite(refs);
	}

	/**
	 * <p>
	 * Construct a generator for a function type. This is tricky because it relies
	 * on synthesising a function somehow. In essence, we need to generate mappings
	 * from input values to output values. This is very expensive in general. There
	 * are some strategies that can help. For example, we can explore all known
	 * functions and look for matches.
	 * </p>
	 * <p>
	 * To synthesize a function, inputs are mapped to outputs in a linear fashion.
	 * That is, the first possible input is mapped to the first possible output, and
	 * so on. A key factor is that we need to ensure the same outputs are returned
	 * for the same inputs.
	 * </p>
	 *
	 * @param type
	 * @param context
	 * @return
	 */
	private Domain<RValue> constructGenerator(Type.Function type, ExtendedContext context) {
		Domain<RValue[]> outputs = constructGenerator(type.getReturns(),context);
		RValue[] lambdas = new RValue[context.getLambdaWidth()];
		for(int i=0;i!=context.getLambdaWidth();++i) {
			// Apply the rotation (for i > 1)
			Domain<RValue[]> tmp = i == 0 ? outputs : Rotate(outputs,i);
			// Construct the (deterministic) lambda
			lambdas[i] = new SynthesizedLambda(type,tmp);
		}
		return Domains.Finite(lambdas);
	}

	/**
	 * Construct a generator for a method type.
	 *
	 * @param type
	 * @param context
	 * @return
	 */
	private Domain<RValue> constructGenerator(Type.Method type, ExtendedContext context) {
		Domain<RValue[]> outputs = constructGenerator(type.getReturns(),context);
		RValue[] lambdas = new RValue[context.getLambdaWidth()];
		// FIXME: should make this non-deterministic!!
		for(int i=0;i!=context.getLambdaWidth();++i) {
			// Apply the rotation (for i > 1)
			Domain<RValue[]> tmp = i == 0 ? outputs : Rotate(outputs,i);
			// Construct the (deterministic) lambda
			lambdas[i] = new SynthesizedLambda(type,tmp);
		}
		return Domains.Finite(lambdas);

	}

	/**
	 * construct a generator from a tuple of types.
	 *
	 * @param types
	 * @param context
	 * @return
	 */
	private Domain<RValue[]> constructGenerator(Tuple<Type> types, ExtendedContext context) {
		Domain<RValue>[] generators = new Domain[types.size()];
		// Construct a generator for each type
		for(int i=0;i!=types.size();++i) {
			generators[i] = constructGenerator(types.get(i),context);
		}
		// Construct the product of generators
		return Domains.Product(generators);
	}

	private Domain<RValue> constructGenerator(Type.Variable type, ExtendedContext context) {
		return new Domains.Adaptor<Integer,RValue>(Domains.Int(context.getIntegerMinimum(), context.getIntegerMaximum())) {
			@Override
			public RValue.Int get(Integer i) {
				return RValue.Int(BigInteger.valueOf(i));
			}
		};
	}

	private final static <T> Domain<T> Rotate(Domain<T> domain, long rotation) {
		final long size = domain.size();
		return new Domain<T>() {

			@Override
			public long size() {
				return size;
			}

			@Override
			public T get(long index) {
				// apply rotation
				index = (index + rotation) % size;
				//
				return domain.get(index);
			}

			@Override
			public Domain<T> slice(long start, long end) {
				start = (start - rotation) % size;
				end = (end - rotation) % size;
				return domain.slice(start, end);
			}
		};
	}

	private final class SynthesizedLambda extends RValue.Lambda {
		private final Type.Callable type;
		private final ArrayList<RValue[]> inputs;
		private final Domain<RValue[]> outputs;
		private final long size;

		public SynthesizedLambda(Type.Callable type, Domain<RValue[]> outputs) {
			this.type = type;
			this.inputs = new ArrayList<>();
			this.outputs = outputs;
			this.size = outputs.size();
		}

		@Override
		public RValue[] execute(Interpreter interpreter, RValue[] arguments, SyntacticItem context) {
			// Check through previous memoizations
			for(int i=0;i!=inputs.size();++i) {
				if(Arrays.equals(arguments, inputs.get(i))) {
					return outputs.get(i % size);
				}
			}
			// Not matched so create a new one
			int r = inputs.size();
			inputs.add(arguments);
			return outputs.get(r % size);
		}

		@Override
		public Callable getType() {
			return type;
		}

	}

	private static long calculateTotalInputs(Domain<?>... domains) {
		long max = 1;
		for(int i=0;i!=domains.length;++i) {
			max = max * domains[i].size();
		}
		//
		return max;
	}

	/**
	 * Format the name and signature of a named declaration in a nice fashion for
	 * writing to the log file.
	 *
	 * @param d
	 * @return
	 */
	private static String toNameString(Decl.Named<?> d) {
		String kind;
		String rest;
		if(d instanceof Decl.Function) {
			Type.Callable t = ((Decl.Function)d).getType();
			kind = "function";
			rest = d.getQualifiedName().toString() + t.getParameters() + "->" + t.getReturns();
		} else if(d instanceof Decl.Method) {
			Decl.Method m = (Decl.Method) d;
			Type.Method t = m.getType();
			// FIXME: this needs to be improved!
			kind = "method";
			rest = m.getQualifiedName() + toMethodParametersString(t.getLifetimeParameters(),m.getTemplate()) + t.getParameters() + "->" + t.getReturns();

		} else if(d instanceof Decl.Type) {
			kind = "type";
			rest = ((Decl.Type)d).getQualifiedName().toString();
		} else {
			throw new RuntimeException("unknown declaration encountered: " + d);
		}
		// Remove all whitespace
		rest = rest.replace(" ", "_");
		//
		return kind + " " + rest;
	}

	private static String toNameString(Decl.Named<?> d, Tuple<Type> parameters) {
		String name = toNameString(d);
		if(parameters.size() > 0) {
			return name + "<" + parameters.toBareString() + ">";
		} else {
			return name;
		}
	}

	private static String toMethodParametersString(Tuple<? extends SyntacticItem> lifetimes, Tuple<? extends SyntacticItem> variables) {
		if(lifetimes.size() == 0 && variables.size() == 0) {
			return "";
		} else if(lifetimes.size() > 0) {
			return "<" + lifetimes.toBareString() + ">";
		} else  if(variables.size() > 0) {
			return "<" + variables.toBareString() + ">";
		} else {
			return "<" + lifetimes.toBareString() + ", " + variables.toBareString() + ">";
		}
	}

	/**
	 * Convert a value array into a string array.
	 *
	 * @param array
	 * @return
	 */
	public static String[] toStringArray(Value.Array array) {
		String[] items = new String[array.size()];
		for(int i=0;i!=array.size();++i) {
			items[i] = array.get(i).toString();
		}
		return items;
	}

	/**
	 * Provides various mechanisms for controlling the construction of generators,
	 * such as limiting the maximum depth of recursive types.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Context {
		private int min;
		private int max;
		private int length;
		private int depth;
		private int width;
		private int rotation;
		private int limit;
		private String[] ignores;
		private long timeout;
		private boolean methods;

		private Context(int minInt, int maxInt, int maxLen, int maxDepth, int width, int rotation, String[] ignores,
				boolean methods, int limit, long timeout) {
			this.min = minInt;
			this.max = maxInt;
			this.length = maxLen;
			this.depth = maxDepth;
			this.width = width;
			this.rotation = rotation;
			this.ignores = ignores;
			this.methods = methods;
			this.limit = limit;
			this.timeout = timeout;
		}

		private Context(Context context) {
			this.min = context.min;
			this.max = context.max;
			this.length = context.length;
			this.depth = context.depth;
			this.width = context.width;
			this.rotation = context.rotation;
			this.methods = context.methods;
			this.ignores = context.ignores;
			this.limit = context.limit;
			this.timeout = context.timeout;
		}

		public int getIntegerMinimum() {
			return min;
		}

		public int getIntegerMaximum() {
			return max;
		}

		public Context setIntegerRange(int min, int max) {
			Context context = new Context(this);
			context.min = min;
			context.max = max;
			return context;
		}

		public int getMaxArrayLength() {
			return length;
		}

		public Context setArrayLength(int length) {
			Context context = new Context(this);
			context.length = length;
			return context;
		}

		public int getRecursiveTypeDepth() {
			return depth;
		}

		public Context setTypeDepth(int depth) {
			Context context = new Context(this);
			context.depth = depth;
			return context;
		}

		public int getAliasingWidth() {
			return width;
		}

		public Context setAliasingWidth(int width) {
			Context context = new Context(this);
			context.width = width;
			return context;
		}

		public int getLambdaWidth() {
			return rotation;
		}

		public Context setLambdaWidth(int width) {
			Context context = new Context(this);
			context.rotation = width;
			return context;
		}

		public boolean getMethodsFlag() {
			return methods;
		}

		public Context setMethodsFlag(boolean flag) {
			Context context = new Context(this);
			context.methods = flag;
			return context;
		}

		public String[] getIgnores() {
			return ignores;
		}

		public boolean isIgnored(Decl.Named decl) {
			String s = decl.getQualifiedName().toString();
			for(int i=0;i!=ignores.length;++i) {
				if(s.endsWith(ignores[i])) {
					return true;
				}
			}
			return false;
		}

		public Context setIgnores(String[] ignores) {
			Context context = new Context(this);
			context.ignores = ignores;
			return context;
		}

		public int getTestLimit() {
			return limit;
		}

		public Context setTestLimit(int limit) {
			Context context = new Context(this);
			context.limit = limit;
			return context;
		}

		public long getTimeout() {
			return timeout;
		}

		public Context setTimeout(long timeout) {
			Context context = new Context(this);
			context.timeout = timeout;
			return context;
		}
	}

	private static class ExtendedContext extends Context {
		// Stores the based frame
		private HashMap<Decl,Integer> depths = new HashMap<>();
		private final CallStack frame;
		private final long timeoutMillis;

		public ExtendedContext(CallStack frame, Context context) {
			super(context);
			this.frame = frame;
			//
			long timeout = context.getTimeout();
			//
			if(timeout != Long.MAX_VALUE) {
				timeoutMillis = timeout * 1000;
			} else {
				timeoutMillis = Long.MAX_VALUE;
			}
			//
		}

		public CallStack getFrame() {
			return frame.setTimeout(timeoutMillis);
		}

		public long getTimeoutMillis() {
			return timeoutMillis;
		}

		/**
		 * Initialise a base stack frame from for this project, such that it can be used
		 * to execute functions and methods within the project. This includes all
		 * modules which this project depends upon.
		 */
		public void initialise(Build.Project project, WyilFile context) throws IOException {
			// Load all relevant modules
			frame.load(context);
			// Load all dependencies
			for(Build.Package p : project.getPackages()) {
				// FIXME: is this the right way to determine the binary file from a given
				// package?
				List<Path.Entry<WyilFile>> entries = p.getRoot().get(Content.filter("**/*", WyilFile.ContentType));
				//
				for(Path.Entry<WyilFile> e : entries) {
					frame.load(e.read());
				}
			}
		}


		public int depth(Decl decl) {
			Integer depth = depths.get(decl);
			if(depth == null) {
				return 0;
			} else {
				return depth;
			}
		}

		public void enter(Decl decl) {
			Integer depth = depths.get(decl);
			int d = depth == null ? 0 : depth;
			depths.put(decl, d+1);
		}

		public void leave(Decl decl) {
			Integer depth = depths.get(decl);
			int d = depth == null ? 0 : depth;
			depths.put(decl, d-1);
		}
	}

	private class ExtendedInterpreter extends Interpreter {
		private final ExtendedContext context;

		public ExtendedInterpreter(PrintStream debug, Context context) {
			super(debug);
			this.context = new ExtendedContext(new CallStack(),context);
		}

		public ExtendedContext getExtendedContext() {
			return context;
		}

		@Override
		public RValue[] execute(Decl.Callable lambda, CallStack frame, RValue[] args, SyntacticItem item) {
			if (lambda instanceof Decl.Method && lambda.getBody().size() == 0) {
				Domain<RValue[]> returns = constructGenerator(lambda.getType().getReturns(), context);
				// FIXME: could return randomly here
				return returns.get(0);
			} else {
				return super.execute(lambda, frame, args, item);
			}
		}
	}

	private static class CachingInterpreter extends Interpreter {
		private final Cache cache = new Cache();

		public CachingInterpreter(PrintStream debug) {
			super(debug);
		}

		@Override
		public RValue[] execute(Decl.Callable lambda, CallStack frame, RValue[] args, SyntacticItem context) {
			switch (lambda.getOpcode()) {
			// cache functions
			case DECL_function:
				RValue[] outputs = cache.get(lambda, args);
				if (outputs != null) {
//					System.out.println("*** CACHE HIT: " + lambda.getName() + " : " + lambda.getType() + ", "
//							+ Arrays.toString(args) + " => " + Arrays.toString(outputs));
					return outputs;
				} else {
					outputs = super.execute(lambda, frame, args, context);
					cache.put(lambda, args, outputs);
					return outputs;
				}
			}
			// Default back
			return super.execute(lambda, frame, args, context);
		}

		private static class Cache {
			// FIXME: this is not exactly efficient.
			private final IdentityHashMap<Decl.Callable,Map<List<RValue>,RValue[]>> cache = new IdentityHashMap<>();

			public RValue[] get(Decl.Callable decl, RValue[] inputs) {
				Map<List<RValue>,RValue[]> entry = cache.get(decl);
				if(entry != null) {
					return entry.get(Arrays.asList(inputs));
				}
				return null;
			}

			public void put(Decl.Callable decl, RValue[] inputs, RValue[] outputs) {
				Map<List<RValue>,RValue[]> entry = cache.get(decl);
				if(entry == null) {
					entry = new HashMap<>();
					cache.put(decl,entry);
				}
				entry.put(Arrays.asList(inputs), outputs);
			}
		}
	}

	public static interface StructuredLogger<T> {
		public void logTimedMessage(T result, long time, long memory);
	}

	public static interface LogEntry {

	}

	/**
	 * A QuickCheck result is generate for each
	 * @author David J. Pearce
	 *
	 */
	public static abstract class AbstractLogEntry implements LogEntry {
		protected final Decl.Named item;

		public AbstractLogEntry(Decl.Named item) {
			this.item = item;
		}

		public Decl.Named getItem() {
			return item;
		}
	}

	/**
	 * Indicates that the given item was skipped.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Skipped extends AbstractLogEntry {
		public Skipped(Decl.Named item) {
			super(item);
		}

		@Override
		public String toString() {
			return "Skipped " + toNameString(item);
		}
	}

	/**
	 * Indicates that the given item caused some kind of internal failure (e.g. a
	 * stack overflow, etc).
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class InternalFailure extends AbstractLogEntry {
		private final Throwable ex;

		public InternalFailure(Decl.Named item, Throwable ex) {
			super(item);
			this.ex = ex;
		}

		@Override
		public String toString() {
			return "Failure(" + ex.getClass().getSimpleName() + ", " + ex.getMessage() + ") "+ toNameString(item);
		}

		public Throwable getThrowable() {
			return ex;
		}
	}

	public static class Timeout extends AbstractLogEntry {
		private final long timeout;

		public Timeout(Decl.Named item, long timeout) {
			super(item);
			this.timeout = timeout;
		}

		@Override
		public String toString() {
			return "Timeout(" + timeout + "s) " + toNameString(item);
		}
	}

	/**
	 * Indicates that a QuickCheck result was obtained for a given named declaration
	 * (e.g. a function or type declaration).
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Result extends AbstractLogEntry {
		/**
		 * Indicates whether check successful or not. That is, whether or not some kind
		 * of violation was detected.
		 */
		private boolean success;
		/**
		 * Indicates the total number of inputs generated.
		 */
		private long total;
		/**
		 * Indicates the total number of inputs checked.
		 */
		private long checked;

		public Result(Decl.Named n, boolean success, long checked, long total) {
			super(n);
			this.success = success;
			this.checked = checked;
			this.total = total;
		}

		/**
		 * Check whether any problems were encountered during the run.
		 *
		 * @return
		 */
		public boolean isChecked() {
			return success;
		}

		/**
		 * Get total space of inputs determined for this run.
		 *
		 * @return
		 */
		public long getTotalInputs() {
			return total;
		}

		/**
		 * Get number of inputs actually used for checking. This will be lower that the
		 * total in the cases where some inputs don't meet preconditions or type
		 * invariants, etc.
		 *
		 * @return
		 */
		public long getCheckedInputs() {
			return checked;
		}
		@Override
		public String toString() {
			String label = success ? "Checked " : "Failed ";
			double percent = total == 0 ? 0 : (checked * 100) / total;
			return label + toNameString(item) + " (" + checked + "/" + total + "=" + percent +"%)";
		}
	}

	/**
	 * Provides summary information on the QuickCheck configuration being used.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Summary implements LogEntry {
		private final Context context;
		public Summary(Context context) {
			this.context = context;
		}
		@Override
		public String toString() {
			return "Check configuration has ints (" + context.getIntegerMinimum() + ".." + context.getIntegerMaximum()
					+ "), array lengths (max " + context.getMaxArrayLength() + "), type depths (max "
					+ context.getRecursiveTypeDepth() + ")";
		}
	}
}
