
#include <inttypes.h>

int bitwise_rshift(int x, int y) {
    return x >> y;
}

int bitwise_lshift(int x, int y) {
    return x << y;
}

int bitwise_xor(int x, int y) {
    return x ^ y;    
}

int bitwise_and(int x, int y) {
    return x & y;
}

int bitwise_or(int x, int y) {
    return x | y;
}

int bitwise_not(int x) {
    return ~x;
}

int64_t bitwise_not_long(int64_t x) {
    return ~x;
}

unsigned int bitwise_not_uint8(unsigned char x) {
    unsigned char y = ~x;
    int z = y;
    return z;
}

int byte_lshift(unsigned char x, unsigned char y) {
    x <<= y;
    return x;
}