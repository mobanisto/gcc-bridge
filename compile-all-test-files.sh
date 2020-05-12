#!/bin/bash

DIR=$(dirname $0)

for f in compiler/src/test/resources/org/renjin/gcc/*.c; do
  echo "compiling $f"
  "$DIR/scripts/compile" test "$f"
done
