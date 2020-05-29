#!/bin/bash

gcc-7 -shared -xc++ -I `gcc-7 -print-file-name=plugin`/include -fPIC \
    -fno-rtti -O2 compiler/src/main/resources/org/renjin/gcc/plugin.c \
    -lstdc++ -shared-libgcc -o plugin.so
