package com.mobanisto.gcc.bridge;

import de.topobyte.system.utils.SystemPaths;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.commands.ArgumentParser;
import de.topobyte.utilities.apache.commons.cli.commands.ExeRunner;
import de.topobyte.utilities.apache.commons.cli.commands.ExecutionData;
import de.topobyte.utilities.apache.commons.cli.commands.RunnerException;
import de.topobyte.utilities.apache.commons.cli.commands.args.CommonsCliArguments;
import de.topobyte.utilities.apache.commons.cli.commands.options.CommonsCliExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptionsFactory;
import de.topobyte.utilities.apache.commons.cli.parsing.EnumArgument;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
        }

        Path pathPluginLibrary = SystemPaths.CWD.resolve("compiler/build/bridge-7/bridge.so");

        Path output = Paths.get(args.get(0));
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }

        Gcc gcc = new Gcc(output.toFile());
        gcc.setPluginLibrary(pathPluginLibrary.toFile());

        for (int i = 1; i < args.size(); i++) {
            Path file = Paths.get(args.get(i));
            gcc.compileToGimple(file.toFile());
        }
    }

}
