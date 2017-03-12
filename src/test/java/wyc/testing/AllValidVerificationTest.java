// Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
// All rights reserved.
//
// This software may be modified and distributed under the terms
// of the BSD license.  See the LICENSE file for details.

package wyc.testing;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import wyc.commands.Compile;
import wyc.util.TestUtils;
import wycc.util.Pair;

/**
 * Run through all valid test cases with verification enabled. Since every test
 * file is valid, a successful test occurs when the compiler succeeds and, when
 * executed, the compiled file produces the expected output. Note that an
 * internal failure does not count as a valid pass, and indicates the test
 * exposed some kind of compiler bug.
 *
 * @author David J. Pearce
 *
 */
@RunWith(Parameterized.class)
public class AllValidVerificationTest {

	/**
	 * The directory containing the source files for each test case. Every test
	 * corresponds to a file in this directory.
	 */
	public final static String WHILEY_SRC_DIR = "tests/valid".replace('/', File.separatorChar);

	/**
	 * Ignored tests and a reason why we ignore them.
	 */
	public final static Map<String, String> IGNORED = new HashMap<>();

	static {
		// timeouts
		IGNORED.put("BoolList_Valid_3", "timeout");
		IGNORED.put("ConstrainedList_Valid_9", "timeout");
		IGNORED.put("ConstrainedList_Valid_21", "timeout");
		IGNORED.put("ConstrainedList_Valid_22", "timeout");
		IGNORED.put("ConstrainedList_Valid_2", "timeout");
		IGNORED.put("ConstrainedList_Valid_3", "timeout");
		IGNORED.put("ConstrainedList_Valid_6", "timeout");
		IGNORED.put("ConstrainedList_Valid_8", "timeout");
		IGNORED.put("ConstrainedList_Valid_23", "timeout");
		IGNORED.put("ConstrainedRecord_Valid_9", "timeout");
		IGNORED.put("Complex_Valid_1", "timeout");
		IGNORED.put("Ensures_Valid_3", "timeout");
		IGNORED.put("FunctionRef_Valid_4", "timeout");
		IGNORED.put("FunctionRef_Valid_11", "timeout");
		IGNORED.put("Lambda_Valid_3", "timeout");
		IGNORED.put("Lambda_Valid_4", "timeout");
		IGNORED.put("ListAssign_Valid_12", "timeout");
		IGNORED.put("ListAssign_Valid_3", "timeout");
		IGNORED.put("ListAssign_Valid_5", "timeout");
		IGNORED.put("ListAssign_Valid_8", "timeout");
		IGNORED.put("ListAccess_Valid_6", "timeout");
		IGNORED.put("OpenRecord_Valid_5", "timeout");
		IGNORED.put("OpenRecord_Valid_6", "timeout");
		IGNORED.put("RecordCoercion_Valid_1", "timeout");
		IGNORED.put("RecursiveType_Valid_24", "timeout");
		IGNORED.put("RecursiveType_Valid_4", "timeout");
		IGNORED.put("RecursiveType_Valid_7", "timeout");
		IGNORED.put("TypeEquals_Valid_25", "timeout");
		IGNORED.put("UnionType_Valid_23", "timeout");
		IGNORED.put("While_Valid_5", "timeout");
		IGNORED.put("While_Valid_11", "timeout");
		IGNORED.put("While_Valid_34", "timeout");
		IGNORED.put("While_Valid_37", "timeout");
		IGNORED.put("While_Valid_41", "timeout");
		IGNORED.put("While_Valid_42", "timeout");
		IGNORED.put("While_Valid_43", "timeout");
		IGNORED.put("While_Valid_45", "timeout");
		IGNORED.put("While_Valid_53", "timeout");
		IGNORED.put("While_Valid_54", "timeout");
		IGNORED.put("While_Valid_2", "timeout");
		IGNORED.put("While_Valid_16", "timeout");
		IGNORED.put("While_Valid_22", "timeout");
		IGNORED.put("While_Valid_26", "timeout");
		// unknown problems
		IGNORED.put("ConstrainedRecord_Valid_8", "?");
		IGNORED.put("Complex_Valid_3", "?");
		IGNORED.put("Lifetime_Lambda_Valid_4", "?");
		IGNORED.put("Record_Valid_3", "?");

		IGNORED.put("TypeEquals_Valid_55", "?");
		// known problems
		IGNORED.put("ConstrainedList_Valid_26", "equality array generator");
		IGNORED.put("ConstrainedList_Valid_28", "#666");
		IGNORED.put("ConstrainedNegation_Valid_2", "type test invariants");
		IGNORED.put("DoWhile_Valid_4", "typing problem");
		IGNORED.put("Fail_Valid_3", "typing problem");
		IGNORED.put("Function_Valid_11", "function overloading");
		IGNORED.put("Function_Valid_15", "field selector in type invariant");
		IGNORED.put("Function_Valid_18", "unknown");
		IGNORED.put("IntOp_Valid_1", "division?");
		IGNORED.put("Lambda_Valid_7", "incorrectly specified!");
		IGNORED.put("Lifetime_Lambda_Valid_7", "typing problem");
		IGNORED.put("ListGenerator_Valid_12", "equality array generator");
		IGNORED.put("Process_Valid_1", "references");
		IGNORED.put("Process_Valid_9", "references");
		IGNORED.put("Process_Valid_10", "references");
		IGNORED.put("RecordSubtype_Valid_1", "typing problem");
		IGNORED.put("RecordSubtype_Valid_2", "typing problem");
		IGNORED.put("RecursiveType_Valid_3", "typing problem");
		IGNORED.put("RecursiveType_Valid_12", "typing problem");
		IGNORED.put("RecursiveType_Valid_22", "typing problem");
		IGNORED.put("RecursiveType_Valid_28", "typing problem");
		IGNORED.put("Reference_Valid_2", "references");
		IGNORED.put("Reference_Valid_3", "references");
		IGNORED.put("Reference_Valid_6", "references");
		IGNORED.put("TypeEquals_Valid_3", "type test invariants");
		IGNORED.put("TypeEquals_Valid_23", "field selection in type invariant");
		IGNORED.put("TypeEquals_Valid_36", "flow typing");
		IGNORED.put("TypeEquals_Valid_37", "flow typing");
		IGNORED.put("TypeEquals_Valid_38", "flow typing");
		IGNORED.put("TypeEquals_Valid_41", "field selection in type invariant");
		IGNORED.put("TypeEquals_Valid_41", "flow typing");
		IGNORED.put("While_Valid_27", "non-linear arithmetic");
		IGNORED.put("While_Valid_32", "non-linear arithmetic");
	}

	/**
	 * The directory where compiler libraries are stored. This is necessary
	 * since it will contain the Whiley Runtime.
	 */
	public final static String WYC_LIB_DIR = "lib/".replace('/', File.separatorChar);

	//
	// Test Harness
	//

	/**
	 * Compile a syntactically invalid test case with verification enabled. The
	 * expectation is that compilation should fail with an error and, hence, the
	 * test fails if compilation does not.
	 *
	 * @param name
	 *            Name of the test to run. This must correspond to a whiley
	 *            source file in the <code>WHILEY_SRC_DIR</code> directory.
	 * @throws IOException
	 */
	protected void runTest(String name) throws IOException {
		File whileySrcDir = new File(WHILEY_SRC_DIR);
		// this will need to turn on verification at some point.
		name = WHILEY_SRC_DIR + File.separatorChar + name + ".whiley";

		Pair<Compile.Result,String> p = TestUtils.compile(
				whileySrcDir,      // location of source directory
				true,                // enable verification
				name);               // name of test to compile

		Compile.Result r = p.first();
		System.out.print(p.second());

		if (r != Compile.Result.SUCCESS) {
			fail("Test failed to compile!");
		} else if (r == Compile.Result.INTERNAL_FAILURE) {
			fail("Test caused internal failure!");
		}
	}

	// ======================================================================
	// Tests
	// ======================================================================

	// Parameter to test case is the name of the current test.
	// It will be passed to the constructor by JUnit.
	private final String testName;
	public AllValidVerificationTest(String testName) {
		this.testName = testName;
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return TestUtils.findTestNames(WHILEY_SRC_DIR);
	}

	// Skip ignored tests
	@Before
	public void beforeMethod() {
		String ignored = IGNORED.get(this.testName);
		Assume.assumeTrue("Test " + this.testName + " skipped: " + ignored, ignored == null);
	}

	@Test
	public void validVerification() throws IOException {
		if (new File("../../running_on_travis").exists()) {
			System.out.println(".");
		}
		runTest(this.testName);
	}
}
