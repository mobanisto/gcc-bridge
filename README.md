# Port of the GCC-Bridge plugin to GCC 4.8

Run this to try compiling the plugin using Gradle:

    ./gradlew compilePlugin

Alternatively, run this directly:

    gcc-4.8 -shared -xc++ -I `gcc-4.8 -print-file-name=plugin`/include -fPIC
    -fno-rtti -O2 compiler/src/main/resources/org/renjin/gcc/plugin.c
    -lstdc++ -shared-libgcc -o plugin.so

Standard output:

    compiler/src/main/resources/org/renjin/gcc/plugin.c:18:0: warning: "_GNU_SOURCE" redefined [enabled by default]
     #define _GNU_SOURCE
     ^
    <command-line>:0:0: note: this is the location of the previous definition
    compiler/src/main/resources/org/renjin/gcc/plugin.c: In function ‘void dump_global_vars()’:
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1134:15: error: ‘varpool_nodes’ was not declared in this scope
       for (node = varpool_nodes; node; node = node->next)
                   ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1134:49: error: ‘struct varpool_node’ has no member named ‘next’
       for (node = varpool_nodes; node; node = node->next)
                                                     ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1136:31: error: ‘struct varpool_node’ has no member named ‘decl’
             dump_global_var(node->decl);
                                   ^
    In file included from compiler/src/main/resources/org/renjin/gcc/plugin.c:34:0:
    compiler/src/main/resources/org/renjin/gcc/plugin.c: In function ‘void dump_aliases()’:
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1159:39: error: ‘struct cgraph_node’ has no member named ‘decl’
         if (DECL_ASSEMBLER_NAME_SET_P (n->decl)) {
                                           ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:48:67: note: in definition of macro ‘CODE_CONTAINS_STRUCT’
     #define CODE_CONTAINS_STRUCT(CODE, STRUCT) (tree_contains_struct[(CODE)][(STRUCT)])
                                                                       ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:3125:26: note: in expansion of macro ‘TREE_CODE’
       (CODE_CONTAINS_STRUCT (TREE_CODE (NODE), TS_DECL_WITH_VIS))
                              ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:3131:4: note: in expansion of macro ‘HAS_DECL_ASSEMBLER_NAME_P’
       (HAS_DECL_ASSEMBLER_NAME_P (NODE) \
        ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1159:9: note: in expansion of macro ‘DECL_ASSEMBLER_NAME_SET_P’
         if (DECL_ASSEMBLER_NAME_SET_P (n->decl)) {
             ^
    In file included from compiler/src/main/resources/org/renjin/gcc/plugin.c:34:0:
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1159:39: error: ‘struct cgraph_node’ has no member named ‘decl’
         if (DECL_ASSEMBLER_NAME_SET_P (n->decl)) {
                                           ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:882:50: note: in definition of macro ‘CONTAINS_STRUCT_CHECK’
     #define CONTAINS_STRUCT_CHECK(T, ENUM)          (T)
                                                      ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:3132:7: note: in expansion of macro ‘DECL_WITH_VIS_CHECK’
        && DECL_WITH_VIS_CHECK (NODE)->decl_with_vis.assembler_name != NULL_TREE)
           ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1159:9: note: in expansion of macro ‘DECL_ASSEMBLER_NAME_SET_P’
         if (DECL_ASSEMBLER_NAME_SET_P (n->decl)) {
             ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1162:81: error: ‘struct cgraph_node’ has no member named ‘decl’
               json_string_field("alias",  IDENTIFIER_POINTER(DECL_ASSEMBLER_NAME(n->decl)));
                                                                                     ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:883:32: note: in definition of macro ‘TREE_CHECK’
     #define TREE_CHECK(T, CODE)   (T)
                                    ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:1482:19: note: in expansion of macro ‘IDENTIFIER_NODE_CHECK’
       ((const char *) IDENTIFIER_NODE_CHECK (NODE)->identifier.id.str)
                       ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1162:39: note: in expansion of macro ‘IDENTIFIER_POINTER’
               json_string_field("alias",  IDENTIFIER_POINTER(DECL_ASSEMBLER_NAME(n->decl)));
                                           ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1162:58: note: in expansion of macro ‘DECL_ASSEMBLER_NAME’
               json_string_field("alias",  IDENTIFIER_POINTER(DECL_ASSEMBLER_NAME(n->decl)));
                                                              ^
    In file included from compiler/src/main/resources/org/renjin/gcc/plugin.c:34:0:
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1164:52: error: ‘struct cgraph_node’ has no member named ‘decl’
               json_bool_field("public", TREE_PUBLIC(n->decl));
                                                        ^
    /usr/lib/gcc/x86_64-linux-gnu/4.8/plugin/include/tree.h:1184:29: note: in definition of macro ‘TREE_PUBLIC’
     #define TREE_PUBLIC(NODE) ((NODE)->base.public_flag)
                                 ^
    compiler/src/main/resources/org/renjin/gcc/plugin.c: At global scope:
    compiler/src/main/resources/org/renjin/gcc/plugin.c:1205:18: error: ‘PROP_referenced_vars’ was not declared in this scope
           PROP_cfg | PROP_referenced_vars,     /* properties_required */
                      ^
