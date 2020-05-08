
#include <math.h>

#define NA_REAL 1.0
#define ISNAN __isnan
#define both_non_NA(a,b) (!ISNAN(a) && !ISNAN(b))
#define both_FINITE(a,b) both_non_NA(a,b)
#define _(x) x


static double R_dist_binary(double *x, int nr, int nc, int i1, int i2)
{
    int total, count, dist;
    int j;

    total = 0;
    count = 0;
    dist = 0;

    if(total == 0) return NA_REAL;
    if(count == 0) return 0;
    return (double) dist / count;
}

