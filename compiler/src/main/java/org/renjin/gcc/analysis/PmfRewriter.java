/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.util.ListIterator;

/**
 * Rewrites Pointers to Member Function references.
 *
 * <p>GCC's C++ frontend (currently) compiles pointers to member functions in a way that stores information using
 * bit-twiddling of the underlying function pointer. Since we cannot twiddle the bits of a MethodHandle, this must
 * be adapted.
 *
 * <p>Since the code to implement PMFs is generated by the g++ frontend, it has a very predictable format and
 * we can simply rewrite the boiler-plate to something more JVM friendly.</p>
 */
public class PmfRewriter {


  private static final GimpleField FLAG_FIELD = new GimpleField("__pfn$flag", new GimpleIntegerType(32), 64);



  // The following block is used by GCC to invoke a PMF:

  //   MethodHandle TEMP1 = fn.__pfn		# actually contains a fun pointer
  //   int32 TEMP2 = TEMP1	            # convert to int
  //   MASK = TEMP2 & 1			        # is the first bit set?
  //   if(MASK == 0) then goto 3 else 4
  // <3>:
  //   # first bit is NOT set, this is a function pointer
  //   iftmp.0 = fn.__pfn
  //   goto bb5
  // <4>:
  //   # first bit is set, this is a vtbl offset
  //  T3137 = fn.__delta
  //  T3138 = T3137
  //  struct T3139 = dist + T3138
  //  vtbl T3140 = *T3139
  //  MethodHandle T3141 = fn.__pfn
  //  int32 T3142 = T3141 (int)fn.__pfn
  //  int32 T3143 = T3142 + -1 (int)fn.__pfn - 1
  //  uint32 T3144 = T3143	 (uint)fn.__pfn-1
  //  T3145 = T3140 + T3144
  //  iftmp.0 = *T3145
  // <5>:
  //  int32 T3146 = fn.__delta		 # 0
  //  unsigned int32 T3147 = T3146
  //  struct Dist * T3148 = dist + T3147
  //  real64T3152 = iftmp.0(T3148)
  //  T3149 = T3152
  //  T3150 = T3149 == expectedValue
  //  T3151 = T3150
  //  null = &assertTrue(&"CALL_MEMBER_FN(dist, fn)() == expectedValue<NULL>", T3151)
  //  gimple_return <null>


  public static void rewrite(Iterable<GimpleCompilationUnit> units) {
    for (GimpleCompilationUnit unit : units) {
      rewrite(unit);
    }
  }

  public static void rewrite(GimpleCompilationUnit unit) {

    boolean pmfUsed = false;

    // Update any PMF structs to include an extra flag field
    for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
      if(isPmfRecord(recordTypeDef)) {
        recordTypeDef.getFields().add(FLAG_FIELD);
        recordTypeDef.setSize(recordTypeDef.getSize() + FLAG_FIELD.getSize());
        pmfUsed = true;
      }
    }

    // If the record type is not present, rewriting won't be necessary
    if(pmfUsed) {
      for (GimpleFunction function : unit.getFunctions()) {
        rewriteFunction(function);
      }
    }
  }

  private static void rewriteFunction(GimpleFunction function) {


    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      ListIterator<GimpleStatement> it = basicBlock.getStatements().listIterator();
      while(it.hasNext()) {
        GimpleStatement statement = it.next();
        if(statement instanceof GimpleAssignment) {
          GimpleAssignment assignment = (GimpleAssignment) statement;

          // Check for assignment to PMF values
          if(isPfnRef(assignment.getLHS())) {
            rewriteAssignment(it, assignment);
          }

          // Check for uses of PMF values
          if(assignment.getOperator() == GimpleOp.COMPONENT_REF &&
              isPfnRef(assignment.getOperands().get(0))) {
            rewriteInvocation(it, assignment);
          }
        }
      }

    }
  }


  /**
   * Rewrite the assignment of a pointer to a member function. This takes a different form depending
   * on whether the class in question has a vtable or not.
   */
  private static void rewriteAssignment(ListIterator<GimpleStatement> it, GimpleAssignment assignment) {


    if(isFunctionPointerAssignment(assignment)) {
      // If the class of the member pointer doesn't have a vtable, then GCC will assign
      // the function pointer of the member directly to the __pfn field.

      // struct pmf_t *pmf;
      // pmf.__pnf = &&_ZN12Distribution9calc_meanEv
      // pmf.__delta = (int)0

      // This is ok, but update the flag field to zero as well in case it was previously set

      GimpleComponentRef pointerRef = (GimpleComponentRef) assignment.getLHS();
      GimpleAssignment flagAssignment = new GimpleAssignment(GimpleOp.INTEGER_CST,
          new GimpleComponentRef(pointerRef.getValue(), FLAG_FIELD.refTo()),
          new GimpleIntegerConstant(new GimpleIntegerType(32), 0));

      it.add(flagAssignment);

    } else if(assignment.getOperator() == GimpleOp.INTEGER_CST) {
      // BUT if the class DOES have a vtable, then an integer is assigned instead:
      //     stuct pmf_t *pmf;
      //     pmf.__pfn = (int)1     # vtbl offset?
      //     pmf.__delta = (int)0
      //
      // We want to rewrite this to be:
      //
      //    pmf.__pfn$flag = (int)1

      GimpleComponentRef lhs = (GimpleComponentRef) assignment.getLHS();
      lhs.setMember(FLAG_FIELD.refTo());
      lhs.setType(new GimpleIntegerType(32));

      GimpleIntegerConstant constant = (GimpleIntegerConstant) assignment.getOperands().get(0);
      constant.setType(new GimpleIntegerType(32));
    }
  }


  /**
   * Rewrite the invocation of a pointer to a member function. GCC writes out a check to see whether
   * the pointer is vtable-based or not.
   *
   */
  private static void rewriteInvocation(ListIterator<GimpleStatement> it, GimpleAssignment s0) {

    // GCC first writes out a runtime check to see if the pointer is vtable based or not:
    //   s0: TEMP1 = fn.__pfn
    //   s1: int32 TEMP2 = TEMP1
    //   s2: MASK = TEMP2 & 1
    //   s3: if(MASK == 0) then goto <VTBL> else <STATIC>

    // Verify that this is the code that we've found and then change it to check our extra flag field instead

    if(!(s0.getLHS() instanceof GimpleVariableRef)) {
      return;
    }
    GimpleVariableRef temp1 = (GimpleVariableRef) s0.getLHS();

    if(!it.hasNext()) {
      return;
    }
    GimpleStatement s1 = it.next();
    if(!(s1 instanceof GimpleAssignment)) {
      return;
    }
    GimpleAssignment a1 = (GimpleAssignment) s1;
    if (a1.getOperator() != GimpleOp.NOP_EXPR ||
        !(a1.getLHS() instanceof GimpleVariableRef) ||
        !a1.getOperands().get(0).equals(temp1)) {
      return;
    }

    GimpleVariableRef temp2 = (GimpleVariableRef) a1.getLHS();
    if(!temp2.getType().equals(new GimpleIntegerType(32))) {
      return;
    }

    // Change the field access to read the flag field instead
    // We change:
    //   s0: MethodHandle TEMP1 = fn.__pfn
    //   s1: int TEMP2 = TEMP1
    // to:
    //   s0: int TEMP2 = fn.__pfn$flag
    //   s1: <deleted>

    GimpleComponentRef componentRef = (GimpleComponentRef) s0.getOperands().get(0);
    componentRef.setMember(FLAG_FIELD.refTo());
    componentRef.setType(FLAG_FIELD.getType());

    s0.setLhs(temp2);
    it.remove();
  }

  private static boolean isFunctionPointerAssignment(GimpleAssignment assignment) {
    return assignment.getOperator() == GimpleOp.ADDR_EXPR &&
           assignment.getOperands().get(0) instanceof GimpleAddressOf &&
           ((GimpleAddressOf) assignment.getOperands().get(0)).getValue() instanceof GimpleFunctionRef;
  }

  private static boolean isPfnRef(GimpleExpr value) {

    if(!(value instanceof GimpleComponentRef)) {
      return false;
    }

    GimpleComponentRef componentRef = (GimpleComponentRef) value;

    return "__pfn".equals(componentRef.getMember().getName()) &&
           componentRef.getType().isPointerTo(GimpleFunctionType.class);
  }

  private static boolean isPmfRecord(GimpleRecordTypeDef recordTypeDef) {
    if(recordTypeDef.getFields().size() != 2) {
      return false;
    }

    GimpleField pfn = recordTypeDef.getFields().get(0);
    GimpleField delta = recordTypeDef.getFields().get(1);

    return pfn.getName().equals("__pfn") &&
           pfn.getType().isPointerTo(GimpleFunctionType.class) &&
           delta.getName().equals("__delta") &&
           delta.getType().equals(new GimpleIntegerType(32));

  }

}