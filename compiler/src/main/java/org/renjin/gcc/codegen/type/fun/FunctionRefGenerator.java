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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;

/**
 * Emits the bytecode necessary to push a method handle onto the stack
 */
public class FunctionRefGenerator implements JExpr {

  private Handle handle;

  public FunctionRefGenerator(Handle handle) {
    this.handle = handle;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    mv.visitLdcInsn(handle);
  }
}
