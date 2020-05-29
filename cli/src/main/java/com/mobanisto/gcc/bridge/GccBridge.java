package com.mobanisto.gcc.bridge;

import de.topobyte.utilities.apache.commons.cli.commands.ArgumentParser;
import de.topobyte.utilities.apache.commons.cli.commands.ExeRunner;
import de.topobyte.utilities.apache.commons.cli.commands.ExecutionData;
import de.topobyte.utilities.apache.commons.cli.commands.RunnerException;
import de.topobyte.utilities.apache.commons.cli.commands.options.DelegateExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptions;
import de.topobyte.utilities.apache.commons.cli.commands.options.ExeOptionsFactory;

import java.io.IOException;

public class GccBridge
{

    public static ExeOptionsFactory OPTIONS_FACTORY = new ExeOptionsFactory()
    {

        @Override
        public ExeOptions createOptions()
        {
            DelegateExeOptions options = new DelegateExeOptions();
            options.addCommand("compile-to-gimple", CompileToGimple.OPTIONS_FACTORY, CompileToGimple.class);
            options.addCommand("compile-gimple", CompileGimple.OPTIONS_FACTORY, CompileGimple.class);
            return options;
        }

    };

    public static void main(String[] args) throws IOException, RunnerException
    {
        ExeOptions options = OPTIONS_FACTORY.createOptions();
        ArgumentParser parser = new ArgumentParser("gcc-bridge", options);

        ExecutionData data = parser.parse(args);
        if (data != null) {
            ExeRunner.run(data);
        }
    }

}