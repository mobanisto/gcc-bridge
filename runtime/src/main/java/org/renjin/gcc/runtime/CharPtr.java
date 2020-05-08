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

import java.util.Arrays;

public class CharPtr extends AbstractPtr {

  public static final int BYTES = 2;

  public static final CharPtr NULL = new CharPtr();
  
  public final char[] array;
  public final int offset;
  
  private CharPtr() {
    this.array = null;
    this.offset = 0;
  }

  public CharPtr(char[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public CharPtr(char... array) {
    this.array = array;
    this.offset = 0;
  }

  @Override
  public char[] getArray() {
    return array;
  }

  @SuppressWarnings("deprecation")
  private int getOffset() {
    return offset;
  }

  @Override
  public int getOffsetInBytes() {
    throw new UnsupportedOperationException("TODO");
  }

  public static CharPtr malloc(int bytes) {
    return new CharPtr(new char[AbstractPtr.mallocSize(bytes, BYTES)]);
  }

  @Override
  public CharPtr realloc(int newSizeInBytes) {
    return new CharPtr(Realloc.realloc(array, offset, newSizeInBytes / 2));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes % BYTES == 0) {
      return new CharPtr(array, offset + (bytes / BYTES));
    } else {
      return new OffsetPtr(this, bytes);
    }
  }

  @Override
  public char getChar() {
    return array[this.offset];
  }

  @Override
  public void setAlignedChar(int index, char value) {
    this.array[this.offset + index] = value;
  }

  @Override
  public char getChar(int offset) {
    if(offset % BYTES == 0) {
      return this.array[this.offset + (offset / BYTES)];
    } else {
      return super.getChar(offset);
    }
  }

  @Override
  public void setChar(char value) {
    this.array[this.offset] = value;
  }

  @Override
  public void setChar(int offset, char value) {
    if(offset % BYTES == 0) {
      this.array[this.offset + (offset / BYTES)] = value;
    } else {
      super.setChar(offset, value);
    }
  }

  @Override
  public char getAlignedChar(int index) {
    return this.array[this.offset + index];
  }

  @Override
  public byte getByte(int byteIndex) {
    return getByteViaChar(byteIndex);
  }

  @Override
  public void setByte(int offset, byte value) {
    // TEMP: unsigned short arrays are being automatically typed as char arrays, and this function is called during initialisation.
    //       Copying the short implementation for now - but this should be redone properly in the future
    setByteViaShort(offset, value);
    //throw new UnsupportedOperationException("TODO");
  }

  // TEMP: unsigned short arrays are being automatically typed as char arrays, and this function is called during initialisation.
  //       Copying the short implementation for now - but this should be redone properly in the future
  @Override
  public void setAlignedShort(int index, short shortValue) {
    array[this.offset + index] = (char)shortValue;
  }

  @Override
  public int toInt() {
    return offset * 2;
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
  }

  public static Ptr int16Array(String string) {
    return new CharPtr(string.toCharArray());
  }

  public static CharPtr fromString(String string) {
    int nchars = string.length();
    char array[] = new char[nchars+1];
    System.arraycopy(string.toCharArray(), 0, array, 0, nchars);
    return new CharPtr(array);
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public String asString() {
    // look for null terminator
    int length;
    for(length=offset;length<array.length;++length) {
      if(array[length] == 0) {
        break;
      }
    }
    return new String(array, offset, length-offset);
  }
  
  public static CharPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).charPtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (CharPtr) voidPointer;
  }

  public static void memset(char[] array, int offset, int value, int length) {
    throw new UnsupportedOperationException("TODO");
  }
  
  public static char memset(int byteValue) {
    throw new UnsupportedOperationException("TODO");
  }

  public static void memcpy(CharPtr x, CharPtr y, int numBytes) {
    char[] arrayS = y.getArray();
    int offsetS = y.getOffset();
    int restY = arrayS.length - offsetS;
    if(restY > 0) {
      char[] carray = new char[numBytes];
      for(int i = 0, j = offsetS; j < arrayS.length && i < numBytes; j++, i++) {
        carray[i] = arrayS[j];
      }
      x = new CharPtr(carray);
    }
  }
}
