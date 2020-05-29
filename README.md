# Standalone GCC-Bridge

Differences to the original project:
* This project contains only the `compiler` and `runtime` modules from
  [Renjin](https://www.renjin.org/)'s [gcc-bridge
  module](https://github.com/bedatadriven/renjin/tree/master/tools/gcc-bridge)
* The project has been refactored to support GCC 7 instead of GCC 4.7
* It contains executables for running the GCC plugin from the command line

## Setup

On Ubuntu, install the following packages:

    apt install openjdk-8-jdk make gcc-7 gcc-7-plugin-dev gfortran-7 g++-7 gcc-7.multilib g++-7-multilib unzip libz-dev linux-libc-dev:i386 lib32stdc++-7-dev

Run this to try compiling the plugin using Gradle:

    ./gradlew compilePlugin

Alternatively, run this directly:

    gcc-7 -shared -xc++ -I `gcc-7 -print-file-name=plugin`/include -fPIC
    -fno-rtti -O2 compiler/src/main/resources/org/renjin/gcc/plugin.c
    -lstdc++ -shared-libgcc -o plugin.so

To build the executables, run this:

    ./gradlew createRuntime

## Usage

There are a number of executables available. This section explains basic usage
of them.

To compile a number of C source files to Gimple, run this:

    ./scripts/gcc-bridge compile-to-gimple gimple /path/to/some/*.c

This will create a directory `gimple` and generate a number of `*.s` and
`*.c.gimple` files for each `*.c` source file.

You can then compile the `*.c.gimple` files to `*.class`. To do so, run this:

    ./scripts/gcc-bridge compile-gimple compiled logs gimple/*.c.gimple

This will create directories `compiled` and `logs`. Compiled `*.class` files go
into the former and some logging goes into the latter.
