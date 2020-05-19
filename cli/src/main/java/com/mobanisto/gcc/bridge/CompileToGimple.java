package com.mobanisto.gcc.bridge;

import de.topobyte.system.utils.SystemPaths;
import org.renjin.gcc.Gcc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompileToGimple {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("usage: Compile <output> <file...>");
            System.exit(1);
        }

        Path pathPluginLibrary = SystemPaths.CWD.resolve("compiler/build/bridge.so");

        Path output = Paths.get(args[0]);
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }

        Gcc gcc = new Gcc(output.toFile());
        gcc.setPluginLibrary(pathPluginLibrary.toFile());

        for (int i = 1; i < args.length; i++) {
            Path file = Paths.get(args[i]);
            gcc.compileToGimple(file.toFile());
        }
    }

}
