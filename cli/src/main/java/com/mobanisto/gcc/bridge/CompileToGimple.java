package com.mobanisto.gcc.bridge;

import de.topobyte.system.utils.SystemPaths;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.commands.RunnerException;
import de.topobyte.utilities.apache.commons.cli.commands.args.CommonsCliArguments;
import de.topobyte.utilities.apache.commons.cli.commands.options.CommonsCliExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptionsFactory;
import de.topobyte.utilities.apache.commons.cli.parsing.EnumArgument;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.renjin.gcc.Gcc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CompileToGimple
{

    private static final String OPTION_GCC_VERSION = "gcc-version";

    public static ExeOptionsFactory OPTIONS_FACTORY = new ExeOptionsFactory()
    {

        @Override
        public ExeOptions createOptions()
        {
            Options options = new Options();
            OptionHelper.addL(options, OPTION_GCC_VERSION, true, false, "gcc version to use");
            return new CommonsCliExeOptions(options, "[options] <output> <file...>");
        }

    };

    public static void main(String exename, CommonsCliArguments arguments) throws IOException, RunnerException
    {
        CommandLine line = arguments.getLine();
        List<String> args = line.getArgList();

        if (args.size() < 2) {
            arguments.getOptions().usage(exename);
            System.exit(1);
        }

        GccVersion gccVersion = GccVersion.GCC_7;

        EnumArgument<GccVersion> gccVersionArgument = new EnumArgument<>(GccVersion.class);
        if (line.hasOption(OPTION_GCC_VERSION)) {
            gccVersion = gccVersionArgument.parse(line.getOptionValue(OPTION_GCC_VERSION));
            if (gccVersion == null) {
                System.out.println(String.format("Invalid argument for '%s'", OPTION_GCC_VERSION));
                System.out.println("valid values: " + gccVersionArgument.getPossibleNames(true));
                System.exit(1);
            }
        }

        String egcc;
        String dirname;
        switch (gccVersion) {
            case GCC_4_7:
                egcc = "gcc-4.7";
                dirname = "bridge-4.7";
                break;
            case GCC_4_8:
                egcc = "gcc-4.8";
                dirname = "bridge-4.8";
                break;
            default:
            case GCC_7:
                egcc = "gcc-7";
                dirname = "bridge-7";
                break;
        }

        Path pathPluginLibrary = SystemPaths.CWD.resolve("compiler/build").resolve(dirname).resolve("bridge.so");

        Path output = Paths.get(args.get(0));
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }

        Gcc gcc = new Gcc(output.toFile());
        gcc.setGcc(egcc);
        gcc.setPluginLibrary(pathPluginLibrary.toFile());

        for (int i = 1; i < args.size(); i++) {
            Path file = Paths.get(args.get(i));
            gcc.compileToGimple(file.toFile());
        }
    }

}
