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
package org.renjin.gcc.link;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Charsets;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Properties;

/**
 * A named function or global variable.
 * 
 * <p>The LinkSymbol class provides an analogy for the linking stage of a native
 * build process.</p>
 * 
 * <p>When Gimple sources are compiled, we write a property file for each exported symbol
 * to {@code META-INF/org.renjin.gcc.symbols/SYMBOL_NAME} that points to the class in which
 * the function is implemented.</p>
 * 
 */
public class LinkSymbol {



  /**
   * The type of Symbol
   */
  public enum SymbolType {

    /**
     * References a static field
     */
    FIELD,

    /**
     * References a static method
     */
    METHOD,

    /**
     * References a static method that provides read-only access to a global variable
     */
    GETTER,

    /**
     * References a class that implements {@link org.renjin.gcc.codegen.call.CallGenerator}
     */
    CALL_GENERATOR
  }
  
  private String name;
  private SymbolType type;
  private String className;
  private String memberName;
  private String descriptor;
  
  private LinkSymbol() {
  }

  public static LinkSymbol forMethod(Method method, SymbolType type) {
    LinkSymbol symbol = new LinkSymbol();
    symbol.type = type;
    symbol.name = method.getName();
    symbol.className = Type.getInternalName(method.getDeclaringClass());
    symbol.memberName = method.getName();
    symbol.descriptor = Type.getMethodDescriptor(method);
    return symbol;
  }

  public static LinkSymbol forGetter(Type declaringClass, String name, String methodName, Type returnType) {
    LinkSymbol symbol = new LinkSymbol();
    symbol.type = SymbolType.GETTER;
    symbol.name = name;
    symbol.className = declaringClass.getInternalName();
    symbol.memberName = methodName;
    symbol.descriptor = Type.getMethodDescriptor(returnType);
    return symbol;
  }

  public static LinkSymbol forFunction(String name, Handle methodHandle) {
    LinkSymbol symbol = new LinkSymbol();
    symbol.type = SymbolType.METHOD;
    symbol.name = name;
    symbol.className = methodHandle.getOwner();
    symbol.memberName = methodHandle.getName();
    symbol.descriptor = methodHandle.getDesc();
    return symbol;
  }

  public static LinkSymbol forGlobalVariable(String name, Type declaringClass) {
    LinkSymbol symbol = new LinkSymbol();
    symbol.type = SymbolType.FIELD;
    symbol.name = name;
    symbol.className = declaringClass.getInternalName();
    symbol.memberName = name;
    return symbol;
  }


  public static LinkSymbol forField(Field field) {
    LinkSymbol symbol = new LinkSymbol();
    symbol.type = SymbolType.FIELD;
    symbol.name = field.getName();
    symbol.className = Type.getInternalName(field.getDeclaringClass());
    symbol.memberName = field.getName();
    return symbol;
  }

  public String getName() {
    return name;
  }

  public SymbolType getType() {
    return type;
  }

  public String getClassName() {
    return className;
  }

  public String getMemberName() {
    return memberName;
  }


  /**
   * Loads the {@code java.lang.reflect.Method} object referenced by this 
   * {@code LinkSymbol}.
   * @param classLoader
   * @throws IllegalStateException if this LinkSymbol does not refer to a symbol.
   * @return
   */
  public Method loadMethod(ClassLoader classLoader) {
    Class<?> owner = loadClass(classLoader);

    for (java.lang.reflect.Method method : owner.getMethods()) {
      if(method.getName().equals(memberName) &&
          Type.getMethodDescriptor(method).equals(descriptor)) {

        assertPublicStatic(method.getModifiers(), "method " + method.toString());

        return method;
      }
    }

    throw new InternalCompilerException(String.format("Symbol '%s' references non-existant method %s.%s (%s)",
        name, className, memberName, descriptor));
  }


  public Field loadField(ClassLoader linkClassLoader) {
    if(type != SymbolType.FIELD) {
      throw new IllegalStateException(
          String.format("Invalid link: Tried to link name '%s' to field, found symbol of type %s", name, type));
    }

    Class<?> owner = loadClass(linkClassLoader);

    Field field;
    try {
      field = owner.getField(memberName);
    } catch (NoSuchFieldException e) {
      throw new InternalCompilerException(String.format("Symbol '%s' references non-existant field %s.%s",
          name, className, memberName));


    }
    assertPublicStatic(field.getModifiers(), "field " + field);

    return field;
  }

  private Class<?> loadClass(ClassLoader classLoader) {
    Class<?> owner;
    try {
      owner = classLoader.loadClass(className.replace("/", "."));
    } catch (ClassNotFoundException e) {
      throw new InternalCompilerException(String.format("Could not load class %s referenced by symbol '%s'",
          className, type));
    }
    return owner;
  }


  private void assertPublicStatic(int modifiers, String memberName) {
    if(!Modifier.isPublic(modifiers)) {
      throw new InternalCompilerException(
          String.format("Symbol '%s' references non-public %s", name, memberName));
    }

    if(!Modifier.isStatic(modifiers)) {
      throw new InternalCompilerException(
          String.format("Symbol '%s' references non-static %s", name, memberName));
    }
  }


  /**
   * Writes this symbol out to an output.
   * 
   * <p>By convention, the path of the symbol will be 
   * {@code $outputDir/META-INF/org.renjin.gcc.symbols/$name}</p>
   * 
   * @param outputDir the root of a class output 
   */
  public void write(File outputDir) throws IOException {
    File symbolFile = new File(outputDir, getMetadataName());
    File symbolDir = symbolFile.getParentFile();
    if(!symbolDir.exists()) {
      boolean created = symbolDir.mkdirs();
      if(!created) {
        throw new IOException("Failed to create directory " + symbolDir.getAbsolutePath());
      }
    }

    try(Writer out = new OutputStreamWriter(new FileOutputStream(symbolFile), Charsets.UTF_8)) {
      out.write(toProperties());
    }
  }

  public String toProperties() {
    StringBuilder properties = new StringBuilder();
    properties.append("type=").append(type.name()).append("\n");
    properties.append("class=").append(className).append("\n");

    if(type == SymbolType.FIELD || type == SymbolType.METHOD || type == SymbolType.GETTER) {
      properties.append("member=").append(memberName).append("\n");
    }

    if(type == SymbolType.METHOD || type == SymbolType.GETTER) {
      properties.append("descriptor=").append(descriptor).append("\n");
    }
    return properties.toString();
  }

  public String getMetadataName() {
    return "META-INF/org.renjin.gcc.symbols/" + name;
  }

  public static LinkSymbol fromDescriptor(String symbolName, Properties properties) {

    LinkSymbol symbol = new LinkSymbol();
    symbol.name = symbolName;
    symbol.type = SymbolType.valueOf(properties.getProperty("type"));
    symbol.className = properties.getProperty("class");
    
    if(symbol.type == SymbolType.FIELD || symbol.type == SymbolType.METHOD || symbol.type == SymbolType.GETTER) {
      symbol.memberName = properties.getProperty("member");
      symbol.descriptor = properties.getProperty("descriptor");
    }

    if(symbol.type == SymbolType.METHOD || symbol.type == SymbolType.GETTER) {
      symbol.descriptor = properties.getProperty("descriptor");
    }


    return symbol;
  }

  /**
   * Looks up a LinkSymbol from the classpath.
   */
  public static Optional<LinkSymbol> lookup(ClassLoader classLoader, String name) throws IOException {
    try(InputStream in = classLoader.getResourceAsStream("META-INF/org.renjin.gcc.symbols/" + name)) {
      if (in == null) {
        return Optional.empty();
      }
      try (InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8)) {
        Properties properties = new Properties();
        properties.load(reader);

        return Optional.of(fromDescriptor(name, properties));
      }
    }
  }

}
