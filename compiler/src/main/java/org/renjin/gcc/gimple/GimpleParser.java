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
package org.renjin.gcc.gimple;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parses a JSON-encoded {@link GimpleCompilationUnit} emitted from our GCC plugin
 */
public class GimpleParser {

  private final ObjectMapper mapper;

  public GimpleParser() {
    super();

    // Prevent Jackson from closing our Reader when parsing zip files
    JsonFactory jsonFactory = new MappingJsonFactory();
    jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

    SimpleModule gimpleModule = new SimpleModule("Gimple", Version.unknownVersion())
        .addDeserializer(GimpleOp.class, new GimpleOpDeserializer());

    mapper = new ObjectMapper(jsonFactory);
    mapper.registerModule(gimpleModule);
  }

  /**
   * Reads the compilation unit from the given {@code reader}. The {@code reader}
   * is <strong>not</strong> closed.
   */
  private GimpleCompilationUnit parse(Reader reader) throws IOException {
    GimpleCompilationUnit unit = mapper.readValue(reader, GimpleCompilationUnit.class);
    for (GimpleFunction function : unit.getFunctions()) {
      function.setUnit(unit);
    }
    for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
      varDecl.setUnit(unit);
      varDecl.setGlobal(true);
    }
    return unit;
  }

  public GimpleCompilationUnit parse(File file) throws IOException {
    try (FileReader reader = new FileReader(file)) {
      GimpleCompilationUnit unit = parse(reader);
      unit.setSourceFile(file);
      return unit;
    }
  }

  public List<GimpleCompilationUnit> parseZipFile(File zipFile) throws IOException {

    List<GimpleCompilationUnit> units = new ArrayList<>();
    try(ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))) {
      ZipEntry zipEntry;
      while((zipEntry = in.getNextEntry()) != null) {
        GimpleCompilationUnit unit = parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        unit.setSourceFile(new File(zipEntry.getName()));
        units.add(unit);
      }
    }
    return units;
  }

}
