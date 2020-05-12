# Port of the GCC-Bridge plugin to GCC 4.8

On Ubuntu, install the following packages:

    apt install openjdk-8-jdk make gcc-4.8 gcc-4.8-plugin-dev gfortran-4.8 g++-4.8 gcc-4.8.multilib g++-4.8-multilib unzip libz-dev linux-libc-dev:i386

Run this to try compiling the plugin using Gradle:

    ./gradlew compilePlugin

Alternatively, run this directly:

    gcc-4.8 -shared -xc++ -I `gcc-4.8 -print-file-name=plugin`/include -fPIC
    -fno-rtti -O2 compiler/src/main/resources/org/renjin/gcc/plugin.c
    -lstdc++ -shared-libgcc -o plugin.so
