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
package org.renjin.gcc.runtime;

import java.lang.invoke.MethodHandle;

public class OffsetPtr implements Ptr {
  private Ptr ptr;
  private int offset;

  public OffsetPtr(Ptr ptr, int offset) {
    this.ptr = ptr;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    return ptr.getArray();
  }

  @Override
  public int getOffsetInBytes() {
    return ptr.getOffsetInBytes() + offset;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes == 0) {
      return this;
    }
    return ptr.pointerPlus(this.offset + bytes);
  }

  @Override
  public boolean getBoolean() {
    return ptr.getBoolean(this.offset);
  }

  @Override
  public boolean getBoolean(int offset) {
    return ptr.getBoolean(this.offset + offset);
  }

  @Override
  public void setBoolean(int offset, boolean value) {
    ptr.setBoolean(this.offset + offset, value);
  }

  @Override
  public void setBoolean(boolean value) {
    ptr.setBoolean(this.offset, value);
  }

  @Override
  public byte getByte() {
    return ptr.getByte(offset);
  }

  @Override
  public byte getByte(int offset) {
    return ptr.getByte(this.offset + offset);
  }

  @Override
  public void setByte(byte value) {
    ptr.setByte(this.offset, value);
  }

  @Override
  public void setByte(int offset, byte value) {
    ptr.setByte(this.offset + offset, value);
  }

  @Override
  public short getShort() {
    return ptr.getShort(this.offset);
  }

  @Override
  public short getShort(int offset) {
    return ptr.getShort(this.offset + offset);
  }

  @Override
  public short getAlignedShort(int index) {
    return ptr.getShort(this.offset + (index * ShortPtr.BYTES));
  }

  @Override
  public void setShort(short value) {
    ptr.setShort(this.offset, value);
  }

  @Override
  public void setAlignedShort(int index, short shortValue) {
    ptr.setShort(this.offset + (index * ShortPtr.BYTES), shortValue);
  }

  @Override
  public void setShort(int offset, short value) {
    ptr.setShort(this.offset + offset, value);
  }

  @Override
  public char getChar() {
    return ptr.getChar(this.offset);
  }

  @Override
  public char getAlignedChar(int index) {
    return ptr.getChar(this.offset + (index * CharPtr.BYTES));
  }

  @Override
  public char getChar(int offset) {
    return ptr.getChar(this.offset + offset);
  }

  @Override
  public void setChar(char value) {
    ptr.setChar(this.offset, value);
  }

  @Override
  public void setAlignedChar(int index, char value) {
    ptr.setChar(this.offset + (index * CharPtr.BYTES), value);
  }

  @Override
  public void setChar(int offset, char value) {
    ptr.setChar(this.offset + offset, value);
  }

  @Override
  public double getDouble() {
    return ptr.getDouble(this.offset);
  }

  @Override
  public double getDouble(int offset) {
    return ptr.getDouble(this.offset + offset);
  }

  @Override
  public double getAlignedDouble(int index) {
    return ptr.getDouble(this.offset + (index * DoublePtr.BYTES));
  }

  @Override
  public void setDouble(double value) {
    ptr.setDouble(this.offset, value);
  }

  @Override
  public void setDouble(int offset, double value) {
    ptr.setDouble(this.offset + offset, value);
  }

  @Override
  public void setAlignedDouble(int index, double value) {
    ptr.setDouble(this.offset + (index * DoublePtr.BYTES), value);
  }

  @Override
  public double getReal96() {
    return ptr.getReal96(this.offset);
  }

  @Override
  public double getReal96(int offset) {
    return ptr.getReal96(this.offset + offset);
  }

  @Override
  public double getAlignedReal96(int index) {
    return ptr.getReal96(this.offset + (index * Double96Ptr.BYTES));
  }

  @Override
  public void setReal96(double value) {
    ptr.setReal96(this.offset, value);
  }

  @Override
  public void setReal96(int offset, double value) {
    ptr.setReal96(this.offset + offset, value);
  }

  @Override
  public void setAlignedReal96(int index, double value) {
    ptr.setReal96(this.offset + (index * Double96Ptr.BYTES), value);
  }

  @Override
  public float getFloat() {
    return ptr.getFloat(this.offset);
  }

  @Override
  public float getFloat(int offset) {
    return ptr.getFloat(this.offset + offset);
  }

  @Override
  public float getAlignedFloat(int index) {
    return ptr.getFloat(this.offset + (index * FloatPtr.BYTES));
  }

  @Override
  public void setFloat(float value) {
    ptr.setFloat(this.offset, value);
  }

  @Override
  public void setAlignedFloat(int index, float value) {
    ptr.setFloat(this.offset + (index * FloatPtr.BYTES), value);
  }

  @Override
  public void setFloat(int offset, float value) {
    ptr.setFloat(this.offset + offset, value);
  }

  @Override
  public int getInt() {
    return ptr.getInt(this.offset);
  }

  @Override
  public int getInt(int offset) {
    return ptr.getInt(this.offset + offset);
  }

  @Override
  public int getAlignedInt(int index) {
    return ptr.getInt(this.offset + (index * IntPtr.BYTES));
  }

  @Override
  public void setInt(int value) {
    ptr.setInt(this.offset, value);
  }

  @Override
  public void setInt(int offset, int value) {
    ptr.setInt(this.offset + offset, value);
  }

  @Override
  public void setAlignedInt(int index, int value) {
    ptr.setInt(this.offset + (index * IntPtr.BYTES), value);
  }

  @Override
  public long getLong() {
    return ptr.getLong(this.offset);
  }

  @Override
  public long getLong(int offset) {
    return ptr.getLong(this.offset + offset);
  }

  @Override
  public long getAlignedLong(int index) {
    return ptr.getLong(this.offset + (index * LongPtr.BYTES));
  }

  @Override
  public void setLong(long value) {
    ptr.setLong(this.offset, value);
  }

  @Override
  public void setLong(int offset, long value) {
    ptr.setLong(this.offset + offset, value);
  }

  @Override
  public void setAlignedLong(int index, long value) {
    ptr.setLong(this.offset + (index * LongPtr.BYTES), value);
  }

  @Override
  public Ptr getPointer() {
    return ptr.getPointer(this.offset);
  }

  @Override
  public Ptr getPointer(int offset) {
    return ptr.getPointer(this.offset + offset);
  }

  @Override
  public Ptr getAlignedPointer(int index) {
    return ptr.getPointer(this.offset + (index * 4));
  }

  @Override
  public void setPointer(Ptr value) {
    ptr.setPointer(this.offset, value);
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    ptr.setPointer(this.offset + offset, value);
  }

  @Override
  public void setAlignedPointer(int index, Ptr value) {
    ptr.setPointer(this.offset + (index * 4), value);
  }

  @Override
  public int toInt() {
    return offset;
  }

  @Override
  public void memset(int byteValue, int n) {
    for (int i = 0; i < n; i++) {
      ptr.setByte(this.offset + i, (byte)byteValue);
    }
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    for (int i = 0; i < numBytes; i++) {
      ptr.setByte(this.offset + i, source.getByte(i));
    }
  }

  @Override
  public void memmove(Ptr source, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int memcmp(Ptr other, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr copyOf(int offset, int numBytes) {
    return ptr.copyOf(this.offset + offset, numBytes);
  }

  @Override
  public Ptr copyOf(int numBytes) {
    return ptr.copyOf(this.offset, numBytes);
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public MethodHandle toMethodHandle() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int compareTo(Ptr o) {
    return AbstractPtr.compare(this, o);
  }

  @Override
  public Ptr withOffset(int offset) {
    return ptr.pointerPlus(offset - ptr.getOffsetInBytes());
  }


}
