package com.mobanisto.gcc.bridge;

import de.topobyte.utilities.apache.commons.cli.commands.options.BasicExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptionsFactory;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CompileGimple
{

    public static ExeOptionsFactory OPTIONS_FACTORY = new ExeOptionsFactory()
    {

        @Override
        public ExeOptions createOptions()
        {
            return new BasicExeOptions("[options] <output> <file...>");
        }

    };

    public static void main(String name, String[] args) throws Exception
    {
        if (args.length < 3) {
            System.out.println("usage: CompileGimple <output> <logs> <file...>");
            System.exit(1);
        }

        Path output = Paths.get(args[0]);
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }

        Path logDir = Paths.get(args[1]);
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir);
        }

        List<GimpleCompilationUnit> gcus = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            Path file = Paths.get(args[i]);
            GimpleCompilationUnit gcu = Gcc.parseGimple(file.toFile());
            gcus.add(gcu);
        }

        String packageName = "com.mobanisto.test";
        String mainClass = "Main";

        GimpleCompiler.IGNORE_ERRORS = true;

        GimpleCompiler compiler = new GimpleCompiler();
        compiler.addMathLibrary();
        compiler.setPackageName(packageName);
        compiler.setClassName(mainClass);
        compiler.setVerbose(true);
        compiler.addMathLibrary();
        compiler.setOutputDirectory(output.toFile());
        compiler.setLinkClassLoader(getLinkClassLoader());
        compiler.setLoggingDirectory(logDir.toFile());

        compiler.compile(gcus);
    }

    private static ClassLoader getLinkClassLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

}
