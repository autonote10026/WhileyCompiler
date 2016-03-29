package wyil.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wycc.lang.NameID;
import wycc.util.Pair;
import wyil.lang.Code.*;
import static wyil.lang.Code.*;
import static wyil.lang.CodeUtils.*;

public abstract class Codes {

	// ===============================================================
	// Bytecode Constructors
	// ===============================================================

	/**
	 * Construct an <code>assert</code> bytecode which represents a user-defined
	 * assertion check.
	 *
	 * @param message
	 *            --- message to report upon failure.
	 * @return
	 */
	public static Assert Assert(int block) {
		return new Assert(block);
	}

	/**
	 * Construct an <code>assume</code> bytecode which represents a user-defined
	 * assumption.
	 *
	 * @param message
	 *            --- message to report upon failure.
	 * @return
	 */
	public static Assume Assume(int block) {
		return new Assume(block);
	}

	public static Operator Operator(Type type, int[] targets, int[] operands, OperatorKind op) {
		return new Operator(type, targets, operands, op);
	}

	/**
	 * Construct a <code>const</code> bytecode which loads a given constant onto
	 * the stack.
	 *
	 * @param afterType
	 *            --- record type.
	 * @param field
	 *            --- field to write.
	 * @return
	 */
	public static Const Const(int target, Constant constant) {
		return new Const(target, constant);
	}

	/**
	 * Construct a <code>copy</code> bytecode which copies the value from a
	 * given operand register into a given target register.
	 *
	 * @param type
	 *            --- record type.
	 * @param reg
	 *            --- reg to load.
	 * @return
	 */
	public static Assign Assign(Type type, int target, int operand) {
		return new Assign(type, target, operand);
	}

	public static Convert Convert(Type from, int target, int operand, Type to) {
		return new Convert(from, target, operand, to);
	}

	public static final Debug Debug(int operand) {
		return new Debug(operand);
	}

	/**
	 * Construct a <code>fail</code> bytecode which halts execution by raising a
	 * fault.
	 *
	 * @param string
	 *            --- Message to give on error.
	 * @return
	 */
	public static Fail Fail() {
		return new Fail();
	}

	/**
	 * Construct a <code>fieldload</code> bytecode which reads a given field
	 * from a record of a given type.
	 *
	 * @param type
	 *            --- record type.
	 * @param field
	 *            --- field to load.
	 * @return
	 */
	public static FieldLoad FieldLoad(Type.EffectiveRecord type, int target,
			int operand, String field) {
		return new FieldLoad(type, target, operand, field);
	}

	/**
	 * Construct a <code>goto</code> bytecode which branches unconditionally to
	 * a given label.
	 *
	 * @param label
	 *            --- destination label.
	 * @return
	 */
	public static Goto Goto(String label) {
		return new Goto(label);
	}

	public static Invoke Invoke(Type.FunctionOrMethod fun, Collection<Integer> targets, Collection<Integer> operands,
			NameID name) {
		return new Invoke(fun, CodeUtils.toIntArray(targets), CodeUtils.toIntArray(operands), name);
	}

	public static Invoke Invoke(Type.FunctionOrMethod fun, int[] targets,
			int[] operands, NameID name) {
		return new Invoke(fun, targets, operands, name);
	}
	
	/**
	 * Construct an <code>invariant</code> bytecode which represents a user-defined
	 * loop invariant.
	 *
	 * @param message
	 *            --- message to report upon failure.
	 * @return
	 */
	public static Invariant Invariant(int block) {
		return new Invariant(block);
	}

	public static Lambda Lambda(Type.FunctionOrMethod fun, int target,
			Collection<Integer> operands, NameID name) {
		return new Lambda(fun, target, CodeUtils.toIntArray(operands), name);
	}

	public static Lambda Lambda(Type.FunctionOrMethod fun, int target,
			int[] operands, NameID name) {
		return new Lambda(fun, target, operands, name);
	}

	public static Loop Loop(int[] modifiedOperands, int block) {
		return new Loop(modifiedOperands,block);
	}

	/**
	 * Construct a <code>newrecord</code> bytecode which constructs a new record
	 * and puts it on the stack.
	 *
	 * @param type
	 * @return
	 */
	public static NewRecord NewRecord(Type.Record type, int target,
			Collection<Integer> operands) {
		return new NewRecord(type, target, CodeUtils.toIntArray(operands));
	}

	public static NewRecord NewRecord(Type.Record type, int target,
			int[] operands) {
		return new NewRecord(type, target, operands);
	}

	/**
	 * Construct a return bytecode which does return a value and, hence, its
	 * type automatically defaults to void.
	 *
	 * @return
	 */
	public static Return Return() {
		return new Return(new Type[0]);
	}

	/**
	 * Construct a return bytecode which reads a value from the operand register
	 * and returns it.
	 *
	 * @param type
	 *            --- type of the value to be returned (cannot be void).
	 * @param operand
	 *            --- register to read return value from.
	 * @return
	 */
	public static Return Return(Type[] types, int... operands) {
		return new Return(types, operands);
	}
	
	public static If If(Type type, int leftOperand, int rightOperand,
			Comparator cop, String label) {
		return new If(type, leftOperand, rightOperand, cop, label);
	}

	public static IfIs IfIs(Type type, int leftOperand, Type rightOperand,
			String label) {
		return new IfIs(type, leftOperand, rightOperand, label);
	}

	public static IndirectInvoke IndirectInvoke(Type.FunctionOrMethod fun,
			int[] targets, int operand, Collection<Integer> operands) {
		return new IndirectInvoke(fun, targets, operand, CodeUtils
				.toIntArray(operands));
	}

	public static IndirectInvoke IndirectInvoke(Type.FunctionOrMethod fun,
			int[] targets, int operand, int[] operands) {
		return new IndirectInvoke(fun, targets, operand, operands);
	}

	public static Label Label(String label) {
		return new Label(label);
	}

	public static final Nop Nop = new Nop();

	/**
	 * Construct a <code>switch</code> bytecode which pops a value off the
	 * stack, and switches to a given label based on it.
	 *
	 * @param type
	 *            --- value type to switch on.
	 * @param defaultLabel
	 *            --- target for the default case.
	 * @param cases
	 *            --- map from values to destination labels.
	 * @return
	 */
	public static Switch Switch(Type type, int operand, String defaultLabel,
			Collection<Pair<Constant, String>> cases) {
		return new Switch(type, operand, defaultLabel, cases);
	}
	
	public static NewObject NewObject(Type.Reference type, int target,
			int operand) {
		return new NewObject(type, target, operand);
	}

	public static Quantify Quantify(int startOperand, int endOperand, int indexOperand, int[] modifiedOperands,
			int block) {
		return new Quantify(startOperand, endOperand, indexOperand, modifiedOperands, block);
	}

	public static Update Update(Type beforeType, int target,
			Collection<Integer> operands, int operand, Type afterType,
			Collection<String> fields) {
		return new Update(beforeType, target,
				CodeUtils.toIntArray(operands), operand, afterType, fields);
	}

	public static Update Update(Type beforeType, int target, int[] operands,
			int operand, Type afterType, Collection<String> fields) {
		return new Update(beforeType, target, operands, operand,
				afterType, fields);
	}

	// ===============================================================
	// Bytecode Implementations
	// ===============================================================

	/**
	 * Represents a binary operator (e.g. '+','-',etc) that is provided to a
	 * <code>BinOp</code> bytecode.
	 *
	 * @author David J. Pearce
	 *
	 */
	public enum OperatorKind {
		// Unary
		NEG(0) {
			public String toString() {
				return "neg";
			}
		},
		INVERT(1) {
			public String toString() {
				return "invert";
			}
		},
		DEREFERENCE(2) {
			public String toString() {
				return "deref";
			}
		},
		ARRAYLENGTH(3) {
			public String toString() {
				return "length";
			}
		},		
		// Binary
		ADD(4) {
			public String toString() {
				return "add";
			}
		},
		SUB(5) {
			public String toString() {
				return "sub";
			}
		},
		MUL(6) {
			public String toString() {
				return "mul";
			}
		},
		DIV(7) {
			public String toString() {
				return "div";
			}
		},
		REM(8) {
			public String toString() {
				return "rem";
			}
		},
		BITWISEOR(9) {
			public String toString() {
				return "or";
			}
		},
		BITWISEXOR(10) {
			public String toString() {
				return "xor";
			}
		},
		BITWISEAND(11) {
			public String toString() {
				return "and";
			}
		},
		LEFTSHIFT(12) {
			public String toString() {
				return "shl";
			}
		},
		RIGHTSHIFT(13) {
			public String toString() {
				return "shr";
			}
		},
		INDEXOF(14) {
			public String toString() {
				return "indexof";
			}
		},
		ARRAYGENERATOR(15) {
			public String toString() {
				return "arraygen";
			}
		},
		ARRAYCONSTRUCTOR(16) {
			public String toString() {
				return "array";
			}
		};
		public int offset;

		private OperatorKind(int offset) {
			this.offset = offset;
		}
	};

	/**
	 * <p>
	 * A binary operation which reads two numeric values from the operand
	 * registers, performs an operation on them and writes the result to the
	 * target register. The binary operators are:
	 * </p>
	 * <ul>
	 * <li><i>add, subtract, multiply, divide, remainder</i>. Both operands must
	 * be either integers or reals (but not one or the other). A value of the
	 * same type is produced.</li>
	 * <li><i>bitwiseor, bitwisexor, bitwiseand</i></li>
	 * <li><i>leftshift,rightshift</i></li>
	 * </ul>
	 * For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int x, int y) -> int:
	 *     return ((x * y) + 1) / 2
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x, int y) -> int:
	 * body:
	 *     mul %2 = %0, %1   : int
	 *     const %3 = 1      : int
	 *     add %2 = %2, %3   : int
	 *     const %3 = 2      : int
	 *     const %4 = 0      : int
	 *     assertne %3, %4 "division by zero" : int
	 *     div %2 = %2, %3   : int
	 *     return %2         : int
	 * </pre>
	 *
	 * Here, the <code>assertne</code> bytecode has been included to check
	 * against division-by-zero. In this particular case the assertion is known
	 * true at compile time and, in practice, would be compiled away.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Operator extends AbstractBytecode<Type> {
		public final OperatorKind kind;

		private Operator(Type type, int[] targets, int[] operands,
				OperatorKind bop) {
			super(new Type[]{ type }, targets, operands);
			if (bop == null) {
				throw new IllegalArgumentException(
						"Operator kind cannot be null");
			}
			this.kind = bop;
		}

		@Override
		public int opcode() {			
			return OPCODE_neg + kind.offset;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return Operator(type(0), nTargets, nOperands, kind);
		}

		public int hashCode() {
			return kind.hashCode() + super.hashCode();
		}

		public boolean equals(Object o) {
			if (o instanceof Operator) {
				Operator bo = (Operator) o;
				return kind.equals(bo.kind) && super.equals(bo);
			}
			return false;
		}

		public String toString() {
			return kind + " %" + target(0) + " = " + arrayToString(operands()) + " : " + type(0);
		}
	}

	/**
	 * Reads a value from the operand register, converts it to a given type and
	 * writes the result to the target register. This bytecode is the only way
	 * to change the type of a value. It's purpose is to simplify
	 * implementations which have different representations of data types. A
	 * convert bytecode must be inserted whenever the type of a register
	 * changes. This includes at control-flow meet points, when the value is
	 * passed as a parameter, assigned to a field, etc. For example, the
	 * following Whiley code:
	 *
	 * <pre>
	 * function f(int x) -> real:
	 *     return x + 1
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x) -> real:
	 * body:
	 *     const %2 = 1           : int
	 *     add %1 = %0, %2        : int
	 *     convert %1 = %1 real   : int
	 *     return %1              : real
	 * </pre>
	 * <p>
	 * Here, we see that the <code>int</code> value in register <code>%1</code>
	 * must be explicitly converted into a <code>real</code> value before it can
	 * be returned from this function.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> In many cases, this bytecode may correspond to a nop on the
	 * hardware. Consider converting from <code>[any]</code> to <code>any</code>
	 * . On the JVM, <code>any</code> translates to <code>Object</code>, whilst
	 * <code>[any]</code> translates to <code>List</code> (which is an instance
	 * of <code>Object</code>). Thus, no conversion is necessary since
	 * <code>List</code> can safely flow into <code>Object</code>.
	 * </p>
	 *
	 */
	public static final class Convert extends AbstractBytecode<Type> {
		
		private Convert(Type from, int target, int operand, Type result) {
			super(new Type[]{from,result}, new int[]{target}, operand);
		}

		public Code clone(int[] nTargets, int[] nOperands) {
			return Convert(type(0), nTargets[0], nOperands[0], type(1));
		}

		public Type result() {
			return type(1);
		}
		
		public int opcode() {
			return OPCODE_convert;
		}

		public boolean equals(Object o) {
			return o instanceof Convert && super.equals(o);
		}

		public String toString() {
			return "convert %" + target(0) + " = %" + operand(0) + " " + result() + " : " + type(0);
		}
	}

	/**
	 * Writes a constant value to a target register. This includes
	 * <i>integers</i>, <i>rationals</i>, <i>lists</i>, <i>sets</i>,
	 * <i>maps</i>, etc. For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int x) -> int:
	 *     xs = {1,2.12}
	 *     return |xs| + 1
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x) -> int:
	 * body:
	 *     var xs
	 *     const %2 = 1               : int
	 *     convert %2 = % 2 int|real  : int
	 *     const %3 = 2.12            : real
	 *     convert %3 = % 3 int|real  : real
	 *     newset %1 = (%2, %3)       : {int|real}
	 *     assign %3 = %1             : {int|real}
	 *     lengthof %3 = % 3          : {int|real}
	 *     const %4 = 1               : int
	 *     add %2 = % 3, %4           : int
	 *     return %2                  : int
	 * </pre>
	 *
	 * Here, we see two kinds of constants values being used: integers (i.e.
	 * <code>const %2 = 1</code>) and rationals (i.e.
	 * <code>const %3 = 2.12</code>).
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Const extends AbstractBytecode<Type> {
		public final Constant constant;

		private Const(int target, Constant constant) {
			super(new Type[0],new int[]{target}, new int[0]);
			this.constant = constant;
		}

		public int opcode() {
			return OPCODE_const;
		}

		public int target() {
			return targets()[0];
		}

		public int hashCode() {
			return constant.hashCode() + targets()[0];
		}

		public boolean equals(Object o) {
			if (o instanceof Const) {
				Const c = (Const) o;
				return constant.equals(c.constant) && Arrays.equals(targets(),c.targets());
			}
			return false;
		}

		public String toString() {
			return "const %" + targets()[0] + " = " + constant + " : "
					+ constant.type();
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {			
			return new Const(nTargets[0],constant);
		}
	}

	/**
	 * Copy the contents from a given operand register into a given target
	 * register. For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int x) -> int:
	 *     x = x + 1
	 *     return x
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x) -> int:
	 * body:
	 *     assign %1 = %0      : int
	 *     const %2 = 1        : int
	 *     add %0 = %1, %2     : int
	 *     return %0           : int
	 * </pre>
	 *
	 * Here we see that an initial assignment is made from register
	 * <code>%0</code> to register <code>%1</code>. In fact, this assignment is
	 * unecessary but is useful to illustrate the <code>assign</code> bytecode.
	 *
	 * <p>
	 * <b>NOTE:</b> on many architectures this operation may not actually clone
	 * the data in question. Rather, it may copy the <i>reference</i> to the
	 * data and then increment its <i>reference count</i>. This is to ensure
	 * efficient treatment of large compound structures (e.g. lists, sets, maps
	 * and records).
	 * </p>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Assign extends AbstractBytecode<Type> {

		private Assign(Type type, int target, int operand) {
			super(type, target, operand);
		}

		public int opcode() {
			return OPCODE_assign;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return Assign(type(0), nTargets[0], nOperands[0]);
		}

		public boolean equals(Object o) {
			if (o instanceof Assign) {
				return super.equals(o);
			}
			return false;
		}

		public String toString() {
			return "assign %" + target(0) + " = %" + operand(0) + " " + " : " + type(0);
		}
	}

	/**
	 * Read a string from the operand register and prints it to the debug
	 * console. For example, the following Whiley code:
	 *
	 * <pre>
	 * method f(int x):
	 *     debug "X = " + x
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * method f(int x):
	 * body:
	 *     const %2 = "X = "       : string
	 *     convert %0 = %0 any     : int
	 *     invoke %0 (%0) whiley/lang/Any:toString : string(any)
	 *     strappend %1 = %2, %0   : string
	 *     debug %1                : string
	 *     return
	 * </pre>
	 *
	 * <b>NOTE</b> This bytecode is not intended to form part of the program's
	 * operation. Rather, it is to facilitate debugging within functions (since
	 * they cannot have side-effects). Furthermore, if debugging is disabled,
	 * this bytecode is a nop.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Debug extends AbstractBytecode<Type> {

		private Debug(int operand) {
			super(new Type[]{Type.Array(Type.T_INT,false)}, new int[0], operand);
		}

		public int opcode() {
			return OPCODE_debug;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return Debug(nOperands[0]);
		}

		public boolean equals(Object o) {
			return o instanceof Debug && super.equals(o);
		}

		public String toString() {
			return "debug %" + operands[0] + " " + " : " + types[0];
		}
	}

	/**
	 * An abstract class representing either an <code>assert</code> or
	 * <code>assume</code> bytecode block.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static abstract class AssertOrAssume extends AbstractCompoundBytecode {
		private AssertOrAssume(int block) {
			super(block, new Type[0], new int[0],new int[0]);
		}
	}
	/**
	 * Represents a block of bytecode instructions representing an assertion.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Assert extends AssertOrAssume {

		private Assert(int block) {
			super(block);
		}

		public int opcode() {
			return OPCODE_assert;
		}

		public String toString() {
			return "assert #" + block;
		}

		public boolean equals(Object o) {
			return o instanceof Assume && super.equals(o);
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {
			return this;
		}
	}

	/**
	 * Represents a block of bytecode instructions representing an assumption.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Assume extends AssertOrAssume {

		private Assume(int block) {
			super(block);
		}

		public int opcode() {
			return OPCODE_assume;
		}

		public String toString() {
			return "assume #" + block;
		}
		
		public boolean equals(Object o) {
			return o instanceof Assume && super.equals(o);			
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {
			return this;
		}
	}

	/**
	 * A bytecode that halts execution by raising a runtime fault. This bytecode
	 * signals that some has gone wrong, and is typically used to signal an
	 * assertion failure.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Fail extends Code.AbstractBytecode<Type> {
		private Fail() {	
			super(new Type[0],new int[0]);
		}

		@Override
		public int opcode() {
			return OPCODE_fail;
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {
			return this;
		}
		
		public String toString() {
			return "fail";			
		}

		
	}

	/**
	 * Reads a record value from an operand register, extracts the value of a
	 * given field and writes this to the target register. For example, the
	 * following Whiley code:
	 *
	 * <pre>
	 * type Point is {int x, int y}
	 *
	 * function f(Point p) -> int:
	 *     return p.x + p.y
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f({int x,int y} p) -> int:
	 * body:
	 *     fieldload %2 = %0 x    : {int x,int y}
	 *     fieldload %3 = %0 y    : {int x,int y}
	 *     add %1 = %2, %3        : int
	 *     return %1              : int
	 * </pre>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class FieldLoad extends AbstractBytecode<Type.EffectiveRecord> {
		public final String field;

		private FieldLoad(Type.EffectiveRecord type, int target, int operand, String field) {
			super((Type) type, target, operand);
			if (field == null) {
				throw new IllegalArgumentException(
						"FieldLoad field argument cannot be null");
			}
			this.field = field;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return FieldLoad(type(0), nTargets[0], nOperands[0], field);
		}

		public int opcode() {
			return OPCODE_fieldload;
		}

		public int hashCode() {
			return super.hashCode() + field.hashCode();
		}

		public Type fieldType() {
			return type(0).fields().get(field);
		}

		public boolean equals(Object o) {
			if (o instanceof FieldLoad) {
				FieldLoad i = (FieldLoad) o;
				return super.equals(i) && field.equals(i.field);
			}
			return false;
		}

		public String toString() {
			return "fieldload %" + target(0) + " = %" + operand(0) + " " + field
					+ " : " + type(0);
		}
	}

	/**
	 * Branches unconditionally to the given label. This is typically used for
	 * if/else statements. For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int x) -> int:
	 *     if x >= 0:
	 *         x = 1
	 *     else:
	 *         x = -1
	 *     return x
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x) -> int:
	 * body:
	 *     const %1 = 0             : int
	 *     iflt %0, %1 goto blklab0 : int
	 *     const %0 = 1             : int
	 *     goto blklab1
	 * .blklab0
	 *     const %0 = 1             : int
	 *     neg %0 = % 0             : int
	 * .blklab1
	 *     return %0                : int
	 * </pre>
	 *
	 * Here, we see the <code>goto</code> bytecode being used to jump from the
	 * end of the true branch over the false branch.
	 *
	 * <p>
	 * <b>Note:</b> in WyIL bytecode, <i>such branches may only go forward</i>.
	 * Thus, a <code>goto</code> bytecode cannot be used to implement the
	 * back-edge of a loop. Rather, a loop block must be used for this purpose.
	 * </p>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Goto extends AbstractBranchingBytecode {
		private Goto(String target) {
			super(target,new Type[0],new int[0]);
		}

		public int opcode() {
			return OPCODE_goto;
		}

		public Goto relabel(Map<String, String> labels) {
			String nlabel = labels.get(destination());
			if (nlabel == null) {
				return this;
			} else {
				return Goto(nlabel);
			}
		}

		public boolean equals(Object o) {
			return o instanceof Goto && super.equals(o);
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {
			return this;
		}		
		
		public String toString() {
			return "goto " + destination();
		}	
	}

	/**
	 * <p>
	 * Branches conditionally to the given label by reading the values from two
	 * operand registers and comparing them. The possible comparators are:
	 * </p>
	 * <ul>
	 * <li><i>equals (eq) and not-equals (ne)</i>. Both operands must have the
	 * given type.</li>
	 * <li><i>less-than (lt), less-than-or-equals (le), greater-than (gt) and
	 * great-than-or-equals (ge).</i> Both operands must have the given type,
	 * which additionally must by either <code>char</code>, <code>int</code> or
	 * <code>real</code>.</li>
	 * <li><i>element of (in).</i> The second operand must be a set whose
	 * element type is that of the first.</li>
	 * </ul>
	 * For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int x, int y) -> int:
	 *     if x < y:
	 *         return -1
	 *     else if x > y:
	 *         return 1
	 *     else:
	 *         return 0
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x, int y) -> int:
	 * body:
	 *     ifge %0, %1 goto blklab0 : int
	 *     const %2 = -1 : int
	 *     return %2 : int
	 * .blklab0
	 *     ifle %0, %1 goto blklab2 : int
	 *     const %2 = 1 : int
	 *     return %2 : int
	 * .blklab2
	 *     const %2 = 0 : int
	 *     return %2 : int
	 * </pre>
	 *
	 * <b>Note:</b> in WyIL bytecode, <i>such branches may only go forward</i>.
	 * Thus, an <code>ifgoto</code> bytecode cannot be used to implement the
	 * back-edge of a loop. Rather, a loop block must be used for this purpose.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class If extends AbstractBranchingBytecode {
		public final Comparator op;

		private If(Type type, int leftOperand, int rightOperand, Comparator op,
				String target) {
			super(target,new Type[]{type}, new int[0], leftOperand, rightOperand);
			this.op = op;
		}

		public If relabel(Map<String, String> labels) {
			String nlabel = labels.get(destination());
			if (nlabel == null) {
				return this;
			} else {
				return If(types[0], operands[0], operands[1], op, nlabel);
			}
		}

		public int opcode() {
			return OPCODE_ifeq + op.offset;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return If(types[0], nOperands[0], nOperands[1], op, destination());
		}

		public int hashCode() {
			return super.hashCode() + op.hashCode();
		}

		public boolean equals(Object o) {
			if (o instanceof If) {
				If ig = (If) o;
				return op == ig.op && super.equals(ig);
			}
			return false;
		}

		public String toString() {
			return "if" + op + " %" + operands[0] + ", %" + operands[1] + " goto " + destination() + " : " + types[0];
		}
	}

	/**
	 * Represents a comparison operator (e.g. '==','!=',etc) that is provided to
	 * a <code>IfGoto</code> bytecode.
	 *
	 * @author David J. Pearce
	 *
	 */
	public enum Comparator {
		EQ(0) {
			public String toString() {
				return "eq";
			}
		},
		NEQ(1) {
			public String toString() {
				return "ne";
			}
		},
		LT(2) {
			public String toString() {
				return "lt";
			}
		},
		LTEQ(3) {
			public String toString() {
				return "le";
			}
		},
		GT(4) {
			public String toString() {
				return "gt";
			}
		},
		GTEQ(5) {
			public String toString() {
				return "ge";
			}
		};
		public int offset;

		private Comparator(int offset) {
			this.offset = offset;
		}
	};

	/**
	 * Branches conditionally to the given label based on the result of a
	 * runtime type test against a value from the operand register. More
	 * specifically, it checks whether the value is a subtype of the type test.
	 * The operand register is automatically <i>retyped</i> as a result of the
	 * type test. On the true branch, its type is intersected with type test. On
	 * the false branch, its type is intersected with the <i>negation</i> of the
	 * type test. For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int|int[] x) -> int:
	 *     if x is int[]:
	 *         return |x|
	 *     else:
	 *         return x
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int|int[] x) -> int:
	 * body:
	 *     ifis %0, int[] goto lab    : int|int[]
	 *     return %0                  : int
	 * .lab
	 *     lengthof %0 = %0           : int[]
	 *     return %0                  : int
	 * </pre>
	 *
	 * Here, we see that, on the false branch, register <code>%0</code> is
	 * automatically given type <code>int</code>, whilst on the true branch it
	 * is automatically given type <code>int[]</code>.
	 *
	 * <p>
	 * <b>Note:</b> in WyIL bytecode, <i>such branches may only go forward</i>.
	 * Thus, an <code>ifis</code> bytecode cannot be used to implement the
	 * back-edge of a loop. Rather, a loop block must be used for this purpose.
	 * </p>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class IfIs extends AbstractBranchingBytecode {
		private IfIs(Type type, int leftOperand, Type rightOperand, String target) {
			super(target, new Type[] { type, rightOperand }, new int[0], leftOperand);
		}

		public int opcode() {
			return OPCODE_ifis;
		}
		
		public Type rightOperand() {
			return type(1);
		}

		public IfIs relabel(Map<String, String> labels) {
			String nlabel = labels.get(destination());
			if (nlabel == null) {
				return this;
			} else {
				return IfIs(types[0], operands[0], types[1], nlabel);
			}
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return IfIs(types[0], nOperands[0], types[1], destination());
		}

		public boolean equals(Object o) {
			return o instanceof IfIs && super.equals(o);
		}

		public String toString() {
			return "ifis" + " %" + operands[0] + ", " + types[1] + " goto " + destination() + " : " + types[0];
		}
	}

	/**
	 * Represents an indirect function call. For example, consider the
	 * following:
	 *
	 * <pre>
	 * function fun(function (int)->int f, int x) -> int:
	 *    return f(x)
	 * </pre>
	 *
	 * Here, the function call <code>f(x)</code> is indirect as the called
	 * function is determined by the variable <code>f</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class IndirectInvoke extends AbstractBytecode<Type.FunctionOrMethod> {

		/**
		 * Construct an indirect invocation bytecode which assigns to an
		 * optional target register the result from indirectly invoking a
		 * function in a given operand with a given set of parameter operands.
		 *
		 * @param type
		 * @param target Register (optional) to which result of invocation is assigned.
		 * @param operand Register holding function point through which indirect invocation is made.
		 * @param operands Registers holding parameters for the invoked function
		 */
		private IndirectInvoke(Type.FunctionOrMethod type, int[] targets,
				int operand, int[] operands) {
			super(new Type.FunctionOrMethod[]{type}, targets, append(operand,operands));
		}

		/**
		 * Return register holding the indirect function/method reference.
		 *
		 * @return
		 */
		public int reference() {
			return operands()[0];
		}

		/**
		 * Return register holding the ith parameter for the invoked function.
		 *
		 * @param i
		 * @return
		 */
		public int parameter(int i) {
			return operands()[i + 1];
		}

		/**
		 * Return registers holding parameters for the invoked function.
		 *
		 * @param i
		 * @return
		 */
		public int[] parameters() {
			return Arrays.copyOfRange(operands(),1,operands().length);
		}

		public int opcode() {
			return OPCODE_indirectinvoke;			
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return IndirectInvoke(type(0), nTargets, nOperands[0],
					Arrays.copyOfRange(nOperands, 1, nOperands.length));
		}

		public boolean equals(Object o) {
			return o instanceof IndirectInvoke && super.equals(o);
		}

		public String toString() {			
			return "indirectinvoke " + arrayToString(targets()) + " = %" + reference() + " "
					+ arrayToString(parameters()) + " : " + type(0);			
		}
	}
	
	/**
	 * Represents a block of bytecode instructions representing an assertion.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Invariant extends Assert {

		private Invariant(int block) {
			super(block);
		}

		public int opcode() {
			return OPCODE_invariant;
		}

		public String toString() {
			return "invariant";
		}

		public int hashCode() {
			return block;
		}

		public boolean equals(Object o) {
			if (o instanceof Invariant) {
				Invariant f = (Invariant) o;
				return block == f.block;
			}
			return false;
		}

		@Override
		public Invariant clone() {
			return this;
		}
	}

	/**
	 * Corresponds to a function or method call whose parameters are read from
	 * zero or more operand registers. If a return value is required, this is
	 * written to a target register afterwards. For example, the following
	 * Whiley code:
	 *
	 * <pre>
	 * function g(int x, int y, int z) -> int:
	 *     return x * y * z
	 *
	 * function f(int x, int y) -> int:
	 *     r = g(x,y,3)
	 *     return r + 1
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function g(int x, int y, int z) -> int:
	 * body:
	 *     mul %3 = %0, %1   : int
	 *     mul %3 = %3, %2   : int
	 *     return %3         : int
	 *
	 * function f(int x, int y) -> int:
	 * body:
	 *     const %2 = 3                    : int
	 *     invoke %2 = (%0, %1, %2) test:g   : int(int,int,int)
	 *     const %3 = 1                    : int
	 *     add %2 = (%2, %3)                : int
	 *     return %2                       : int
	 * </pre>
	 *
	 * Here, we see that arguments to the <code>invoke</code> bytecode are
	 * supplied in the order they are given in the function or method's
	 * declaration.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Invoke extends AbstractBytecode<Type.FunctionOrMethod> {
		public final NameID name;

		private Invoke(Type.FunctionOrMethod type, int[] targets, int[] operands,
				NameID name) {
			super(new Type.FunctionOrMethod[]{type}, targets, operands);
			this.name = name;
		}
				
		public int opcode() {
			return OPCODE_invoke;							
		}

		public int hashCode() {
			return name.hashCode() + super.hashCode();
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return Invoke(type(0), nTargets, nOperands, name);
		}

		public boolean equals(Object o) {
			if (o instanceof Invoke) {
				Invoke i = (Invoke) o;
				return name.equals(i.name) && super.equals(i);
			}
			return false;
		}

		public String toString() {
			return "invoke " + arrayToString(targets()) + " = " + arrayToString(operands()) + " " + name + " : "
					+ type(0);			
		}
	}

	public static final class Lambda extends AbstractBytecode<Type.FunctionOrMethod> {
		public final NameID name;

		private Lambda(Type.FunctionOrMethod type, int target, int[] operands,
				NameID name) {
			super(type, target, operands);
			this.name = name;
		}

		public int opcode() {
			return OPCODE_lambda;			
		}

		public int hashCode() {
			return name.hashCode() + super.hashCode();
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return Lambda(type(0), nTargets[0], nOperands, name);
		}

		public boolean equals(Object o) {
			if (o instanceof Lambda) {
				Lambda i = (Lambda) o;
				return name.equals(i.name) && super.equals(i);
			}
			return false;
		}

		public String toString() {
			return "lambda %" + target(0) + " = " + arrayToString(operands()) + " "
					+ name + " : " + type(0);
		}
	}

	/**
	 * Represents the labelled destination of a branch or loop statement.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Label implements Code {
		public final String label;

		private Label(String label) {
			this.label = label;
		}

		public int opcode() {
			return -1;
		}

		public Label relabel(Map<String, String> labels) {
			String nlabel = labels.get(label);
			if (nlabel == null) {
				return this;
			} else {
				return Label(nlabel);
			}
		}

		@Override
		public Code remap(Map<Integer, Integer> binding) {
			return this;
		}
		
		public int hashCode() {
			return label.hashCode();
		}

		public boolean equals(Object o) {
			if (o instanceof Label) {
				return label.equals(((Label) o).label);
			}
			return false;
		}

		public String toString() {
			return "." + label;
		}

		@Override
		public void registers(Set<Integer> register) {
			// TODO Auto-generated method stub			
		}
	}
	

	/**
	 * Represents a block of code which loops continuously until e.g. a
	 * conditional branch is taken out of the block. For example:
	 *
	 * <pre>
	 * function f() -> int:
	 *     r = 0
	 *     while r < 10:
	 *         r = r + 1
	 *     return r
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f() -> int:
	 * body:
	 *     const %0 = 0             : int
	 *     loop (%0)
	 *         const %1 = 10        : int
	 *         ifge %0, %1 goto blklab0 : int
	 *         const %1 = 1         : int
	 *         add %0 = %0, %1      : int
	 * .blklab0
	 *     return %0                : int
	 * </pre>
	 *
	 * <p>
	 * Here, we see a loop which increments an accumulator register
	 * <code>%0</code> until it reaches <code>10</code>, and then exits the loop
	 * block.
	 * </p>
	 * <p>
	 * The <i>modified operands</i> of a loop bytecode (shown in brackets
	 * alongside the bytecode) indicate those operands which are modified at
	 * some point within the loop.
	 * </p>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Loop extends AbstractCompoundBytecode {

		private Loop(int[] targets, int block, int... operands) {
			super(block, new Type[0], targets, operands);
		}

		public int opcode() {
			return OPCODE_loop;
		}

		public boolean equals(Object o) {
			if (o instanceof Loop) {
				Loop f = (Loop) o;
				return block == f.block && super.equals(f);
			}
			return false;
		}

		public int[] modifiedOperands() {
			return targets();
		}
		
		@Override
		public Loop clone(int[] nTargets, int[] nOperands) {
			return new Loop(nTargets, block, nOperands);
		}

		public String toString() {
			return "loop " + arrayToString(targets()) + " = " + block;
		}
	}

	public static final class Quantify extends Loop {
		
		private Quantify(int startOperand,int endOperand,
				int indexOperand, int[] targets, int block) {
			super(targets, block, startOperand, endOperand, indexOperand);
		}

		public int opcode() {
			return OPCODE_quantify;
		}
		
		public int startOperand() {
			return operands[0];
		}
		
		public int endOperand() {
			return operands[1];
		}
		
		public int indexOperand() {
			return operands[2];
		}
				
		public boolean equals(Object o) {
			if (o instanceof Quantify) {
				Quantify f = (Quantify) o;
				return super.equals(f);
			}
			return false;
		}

		public String toString() {
			return "quantify " + arrayToString(targets()) + " = #" + block() + arrayToString(operands());
		}
	}
	
	/**
	 * Represents a type which may appear on the left of an assignment
	 * expression. Arrays, Records and References are the
	 * only valid types for an lval.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static abstract class LVal<T> {
		protected T type;

		public LVal(T t) {
			this.type = t;
		}

		public T rawType() {
			return type;
		}
	}

	/**
	 * An LVal with array type.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class ArrayLVal extends LVal<Type.EffectiveArray> {
		public final int indexOperand;

		public ArrayLVal(Type.EffectiveArray t, int indexOperand) {
			super(t);
			this.indexOperand = indexOperand;
		}
	}

	/**
	 * An LVal with list type.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class ReferenceLVal extends LVal<Type.Reference> {
		public ReferenceLVal(Type.Reference t) {
			super(t);
		}
	}

	/**
	 * An LVal with record type.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class RecordLVal extends LVal<Type.EffectiveRecord> {
		public final String field;

		public RecordLVal(Type.EffectiveRecord t, String field) {
			super(t);
			this.field = field;
			if (!t.fields().containsKey(field)) {
				throw new IllegalArgumentException("invalid Record Type");
			}
		}
	}

	private static final class UpdateIterator implements Iterator<LVal> {
		private final ArrayList<String> fields;
		private final int[] operands;
		private Type iter;
		private int fieldIndex;
		private int operandIndex;
		private int index;

		public UpdateIterator(Type type, int level, int[] operands,
				ArrayList<String> fields) {
			this.fields = fields;
			this.iter = type;
			this.index = level;
			this.operands = operands;
		}

		public LVal next() {
			Type raw = iter;
			index--;
			if (iter instanceof Type.Reference) {
				Type.Reference ref = (Type.Reference) iter;
				iter = ref.element();
				return new ReferenceLVal(ref);
			} else if (iter instanceof Type.EffectiveArray) {
				Type.EffectiveArray list = (Type.EffectiveArray) iter;
				iter = list.element();
				return new ArrayLVal(list, operands[operandIndex++]);
			} else if (iter instanceof Type.EffectiveRecord) {
				Type.EffectiveRecord rec = (Type.EffectiveRecord) iter;
				String field = fields.get(fieldIndex++);
				iter = rec.fields().get(field);
				return new RecordLVal(rec, field);
			} else {
				throw new IllegalArgumentException("Invalid type for Update: " + iter);
			}
		}

		public boolean hasNext() {
			return index > 0;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"UpdateIterator is unmodifiable");
		}
	}

	/**
	 * <p>
	 * Pops a compound structure, zero or more indices and a value from the
	 * stack and updates the compound structure with the given value. Valid
	 * compound structures are lists, dictionaries, strings, records and
	 * references.
	 * </p>
	 * <p>
	 * Ideally, this operation is done in-place, meaning the operation is
	 * constant time. However, to support Whiley's value semantics this bytecode
	 * may require (in some cases) a clone of the underlying data-structure.
	 * Thus, the worst-case runtime of this operation is linear in the size of
	 * the compound structure.
	 * </p>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Update extends AbstractBytecode<Type>
			implements Iterable<LVal> {
		public final ArrayList<String> fields;

		/**
		 * Construct an Update bytecode which assigns to a given operand to a
		 * set of target registers. For indirect map/list updates, an additional
		 * set of operands is used to generate the appropriate keys. For field
		 * assignments, a set of fields is provided.
		 *
		 * @param beforeType
		 * @param target
		 *            Register being assigned
		 * @param operands
		 *            Registers used for keys on left-hand side in map/list
		 *            updates
		 * @param operand
		 *            Register on right-hand side whose value is assigned
		 * @param afterType
		 * @param fields
		 *            Fields for record updates
		 */
		private Update(Type beforeType, int target, int[] operands,
				int operand, Type afterType, Collection<String> fields) {
			super(new Type[]{beforeType,afterType}, new int[]{target}, append(operands,operand));
			if (fields == null) {
				throw new IllegalArgumentException(
						"FieldStore fields argument cannot be null");
			}
			this.fields = new ArrayList<String>(fields);
		}

		// Helper used for clone()
		private Update(Type[] types, int[] targets, int[] operands, Collection<String> fields) {
			super(types,targets,operands);
			if (fields == null) {
				throw new IllegalArgumentException(
						"FieldStore fields argument cannot be null");
			}
			this.fields = new ArrayList<String>(fields);
		}

		public int opcode() {
			return OPCODE_update;
		}

		/**
		 * Returns register from which assigned value is read. This is also
		 * known as the "right-hand side".
		 *
		 * @return
		 */
		public int result() {
			return operands()[operands().length-1];
		}

		/**
		 * Get the given key register (in order of appearance from the left)
		 * used in a map or list update.
		 *
		 * @param index
		 * @return
		 */
		public int key(int index) {
			return operands()[index];
		}

		/**
		 * Return the registers used to hold key values for map or list updates.
		 *
		 * @return
		 */
		public int[] keys() {
			return Arrays.copyOf(operands(),operands().length-1);
		}

		public int level() {
			int base = -1; // because last operand is rhs
			if (type(0) instanceof Type.Reference) {
				base++;
			}
			return base + fields.size() + operands().length;
		}

		public Iterator<LVal> iterator() {
			return new UpdateIterator(afterType(), level(), keys(), fields);
		}

		public Type afterType() {
			return types[1];
		}
		
		/**
		 * Extract the type for the right-hand side of this assignment.
		 *
		 * @return
		 */
		public Type rhs() {
			Type iter = afterType();

			int fieldIndex = 0;
			for (int i = 0; i != level(); ++i) {
				if (iter instanceof Type.Reference) {
					Type.Reference proc = Type.effectiveReference(iter);
					iter = proc.element();
				} else if (iter instanceof Type.EffectiveArray) {
					Type.EffectiveArray list = (Type.EffectiveArray) iter;
					iter = list.element();
				} else if (iter instanceof Type.EffectiveRecord) {
					Type.EffectiveRecord rec = (Type.EffectiveRecord) iter;
					String field = fields.get(fieldIndex++);
					iter = rec.fields().get(field);
				} else {
					throw new IllegalArgumentException("Invalid type for Update: " + iter);
				}
			}
			return iter;
		}

		@Override
		public final Code clone(int[] nTargets, int[] nOperands) {
			return new Update(types, nTargets, nOperands, fields);
		}

		public boolean equals(Object o) {
			if (o instanceof Update) {
				Update i = (Update) o;
				return super.equals(o) && fields.equals(i.fields);
			}
			return false;
		}

		public String toString() {
			String r = "%" + target(0);
			for (LVal lv : this) {
				if (lv instanceof ArrayLVal) {
					ArrayLVal l = (ArrayLVal) lv;
					r = r + "[%" + l.indexOperand + "]";
				} else if (lv instanceof RecordLVal) {
					RecordLVal l = (RecordLVal) lv;
					r = r + "." + l.field;
				} else {
					ReferenceLVal l = (ReferenceLVal) lv;
					r = "(*" + r + ")";
				}
			}
			return "update " + r + " = %" + result() + " : " + type(0) + " -> " + afterType();
		}
	}

	/**
	 * Constructs a new record value from the values of zero or more operand
	 * register, each of which is associated with a field name. The new record
	 * value is then written into the target register. For example, the
	 * following Whiley code:
	 *
	 * <pre>
	 * type Point is {real x, real y}
	 *
	 * function f(real x, real y) -> Point:
	 *     return {x: x, y: x}
	 * </pre>
	 *
	 * can be translated into the following WyIL:
	 *
	 * <pre>
	 * function f(real x, real y) -> Point:
	 * body:
	 *     assign %3 = %0         : real
	 *     assign %4 = %0         : real
	 *     newrecord %2 (%3, %4)  : {real x,real y}
	 *     return %2              : {real x,real y}
	 * </pre>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class NewRecord extends
			AbstractBytecode<Type.Record> {
		
		private NewRecord(Type.Record type, int target, int[] operands) {
			super(type, target, operands);
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {
			return NewRecord(type(0), nTargets[0], nOperands);
		}

		public int opcode() {
			return OPCODE_newrecord;
		}

		public boolean equals(Object o) {
			if (o instanceof NewRecord) {
				return super.equals(o);
			}
			return false;
		}

		public String toString() {
			return "newrecord %" + target(0) + " = " + arrayToString(operands()) + " : " + type(0);
		}
	}

	/**
	 * Represents a no-operation bytecode which, as the name suggests, does
	 * nothing.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Nop extends AbstractBytecode<Type> {
		private Nop() {
			super(new Type[0],new int[0]);
		}

		@Override
		public int opcode() {
			return OPCODE_nop;
		}

		@Override
		protected Code clone(int[] nTargets, int[] nOperands) {
			return this;
		}
		
		public boolean equals(Object o) {
			return o instanceof Nop;
		}
		
		public String toString() {
			return "nop";
		}

	
	}

	/**
	 * Returns from the enclosing function or method, possibly returning a
	 * value. For example, the following Whiley code:
	 *
	 * <pre>
	 * function f(int x, int y) -> int:
	 *     return x + y
	 * </pre>
	 *
	 * can be translated into the following WyIL:
	 *
	 * <pre>
	 * function f(int x, int y) -> int:
	 * body:
	 *     assign %3 = %0    : int
	 *     assign %4 = %1    : int
	 *     add %2 = % 3, %4  : int
	 *     return %2         : int
	 * </pre>
	 *
	 * Here, the
	 * <code>return<code> bytecode returns the value of its operand register.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Return extends AbstractBytecode<Type> {

		private Return(Type[] types, int... operands) {
			super(types, new int[0], operands);			
		}
		
		@Override
		public int opcode() {			
			return OPCODE_return;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return new Return(Arrays.copyOf(types, types.length), nOperands);
		}

		public boolean equals(Object o) {
			if (o instanceof Return) {
				return super.equals(o);
			}
			return false;
		}

		public String toString() {
			String r = "return";
			for(int i=0;i!=operands.length;++i) {
				if(i!=0) {
					r += ",";
				}
				r += " %" + operands[i];
			}
			return r;
		}
	}

	/**
	 * Performs a multi-way branch based on the value contained in the operand
	 * register. A <i>dispatch table</i> is provided which maps individual
	 * matched values to their destination labels. For example, the following
	 * Whiley code:
	 *
	 * <pre>
	 * function f(int x) -> string:
	 *     switch x:
	 *         case 1:
	 *             return "ONE"
	 *         case 2:
	 *             return "TWO"
	 *         default:
	 *             return "OTHER"
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * function f(int x) -> string:
	 * body:
	 *     switch %0 1->blklab1, 2->blklab2, *->blklab3
	 * .blklab1
	 *     const %1 = "ONE" : string
	 *     return %1 : string
	 * .blklab2
	 *     const %1 = "TWO" : string
	 *     return %1 : string
	 * .blklab3
	 *     const %1 = "OTHER" : string
	 *     return %1 : string
	 * </pre>
	 *
	 * Here, we see how e.g. value <code>1</code> is mapped to the label
	 * <code>blklab1</code>. Thus, if the operand register <code>%0</code>
	 * contains value <code>1</code>, then control will be transferred to that
	 * label. The final mapping <code>*->blklab3</code> covers the default case
	 * where the value in the operand is not otherwise matched.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Switch extends AbstractBytecode<Type> {
		public final ArrayList<Pair<Constant, String>> branches;
		public final String defaultTarget;

		Switch(Type type, int operand, String defaultTarget,
				Collection<Pair<Constant, String>> branches) {
			super(new Type[]{type}, new int[0], operand);
			this.branches = new ArrayList<Pair<Constant, String>>(branches);
			this.defaultTarget = defaultTarget;
		}

		@Override
		public int opcode() {
			return OPCODE_switch;
		}

		public Switch relabel(Map<String, String> labels) {
			ArrayList<Pair<Constant, String>> nbranches = new ArrayList();
			for (Pair<Constant, String> p : branches) {
				String nlabel = labels.get(p.second());
				if (nlabel == null) {
					nbranches.add(p);
				} else {
					nbranches.add(new Pair(p.first(), nlabel));
				}
			}

			String nlabel = labels.get(defaultTarget);
			if (nlabel == null) {
				return Switch(types[0], operands[0], defaultTarget, nbranches);
			} else {
				return Switch(types[0], operands[0], nlabel, nbranches);
			}
		}

		public boolean equals(Object o) {
			if (o instanceof Switch) {
				Switch ig = (Switch) o;
				return operands[0] == ig.operands[0]
						&& defaultTarget.equals(ig.defaultTarget)
						&& branches.equals(ig.branches) && types[0].equals(ig.types[0]);
			}
			return false;
		}

		public String toString() {
			String table = "";
			boolean firstTime = true;
			for (Pair<Constant, String> p : branches) {
				if (!firstTime) {
					table += ", ";
				}
				firstTime = false;
				table += p.first() + "->" + p.second();
			}
			table += ", *->" + defaultTarget;
			return "switch %" + operands[0] + " " + table;
		}

		@Override
		public Code clone(int[] nTargets, int[] nOperands) {
			return new Switch(types[0], nOperands[0], defaultTarget, branches);
		}

	}

	/**
	 * Instantiate a new object from the value in a given operand register, and
	 * write the result (a reference to that object) to a given target register.
	 * For example, the following Whiley code:
	 *
	 * <pre>
	 * type PointObj as &{real x, real y}
	 *
	 * method f(real x, real y) -> PointObj:
	 *     return new {x: x, y: y}
	 * </pre>
	 *
	 * can be translated into the following WyIL code:
	 *
	 * <pre>
	 * method f(int x, int y) -> &{real x,real y}:
	 * body:
	 *     newrecord %2 = (%0, %1)  : {real x,real y}
	 *     newobject %2 = %2        : ref {real x,real y}
	 *     return %2                : ref {real x,real y}
	 * </pre>
	 *
	 * <b>NOTE:</b> objects are unlike other data types in WyIL, in that they
	 * represent mutable state allocated on a heap. Thus, changes to an object
	 * within a method are visible to those outside of the method.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class NewObject extends AbstractBytecode<Type.Reference> {

		private NewObject(Type.Reference type, int target, int operand) {
			super(type, target, operand);
		}

		@Override
		public int opcode() {
			return OPCODE_newobject;
		}

		protected Code clone(int[] nTargets, int[] nOperands) {
			return NewObject(type(0), nTargets[0], nOperands[0]);
		}

		public boolean equals(Object o) {
			if (o instanceof NewObject) {
				return super.equals(o);
			}
			return false;
		}

		public String toString() {
			return "newobject %" + target(0) + " = %" + operand(0) + " : " + type(0);
		}
	}

	// =============================================================
	// Helpers
	// =============================================================
	private static int[] append(int[] operands, int operand) {
		int[] noperands = Arrays.copyOf(operands, operands.length+1);
		noperands[operands.length] = operand;
		return noperands;
	}

	private static int[] append(int operand, int[] operands) {
		int[] noperands = new int[operands.length+1];
		System.arraycopy(operands,0,noperands,1,operands.length);
		noperands[0] = operand;
		return noperands;
	}
}
