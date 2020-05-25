#!/bin/bash

gcc-4.8 -shared -xc++ -I `gcc-4.8 -print-file-name=plugin`/include -fPIC \
    -fno-rtti -O2 compiler/src/main/resources/org/renjin/gcc/plugin.c \
    -lstdc++ -shared-libgcc -o plugin-4.8.so
