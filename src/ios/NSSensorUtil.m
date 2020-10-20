//
//  NSSensorUtil.m
//  HelloCordova
//
//  Created by 梁仲太 on 2018/6/5.
//

#import "NSSensorUtil.h"

@implementation NSSensorUtil

+(NSMutableArray<NSNumber *> *)getOrientation:(NSMutableArray<NSNumber *> *)R andValues:(NSMutableArray<NSNumber *> *)values{
    if (R.count == 9) {
        values[0] = [NSNumber numberWithFloat:atan2([R[1] floatValue], [R[4] floatValue])];
        values[1] = [NSNumber numberWithFloat:asin(-[R[7] floatValue])];
        values[2] = [NSNumber numberWithFloat:atan2(-[R[6] floatValue], [R[8] floatValue])];
    } else {
        values[0] = [NSNumber numberWithFloat:atan2([R[1] floatValue], [R[5] floatValue])];
        values[1] = [NSNumber numberWithFloat:asin(-[R[9] floatValue])];
        values[2] = [NSNumber numberWithFloat:atan2(-[R[8] floatValue], [R[10] floatValue])];
    }
    return values;
}
+(NSMutableArray<NSNumber *> *)getRotationMatrix:(NSMutableArray<NSNumber *> *)R andValues:(NSMutableArray<NSNumber *> *)I andGravity:(NSMutableArray<NSNumber *> *)gravity andFloat:(NSMutableArray<NSNumber *> *)geomagnetic{
    float Ax = [gravity[0] floatValue];
    float Ay = [gravity[1] floatValue];
    float Az = [gravity[2] floatValue];
    float Ex = [geomagnetic[0] floatValue];
    float Ey = [geomagnetic[1] floatValue];
    float Ez = [geomagnetic[2] floatValue];
    float Hx = Ey*Az - Ez*Ay;
    float Hy = Ez*Ax - Ex*Az;
    float Hz = Ex*Ay - Ey*Ax;
    float normH = (float)sqrt(Hx*Hx + Hy*Hy + Hz*Hz);
    if (normH < 0.1f) {
        // device is close to free fall (or in space?), or close to
        // magnetic north pole. Typical values are  > 100.
        return R;
    }
    
    float invH = 1.0f / normH;
    Hx *= invH;
    Hy *= invH;
    Hz *= invH;
    float invA = 1.0f / (float)sqrt(Ax*Ax + Ay*Ay + Az*Az);
    Ax *= invA;
    Ay *= invA;
    Az *= invA;
    float Mx = Ay*Hz - Az*Hy;
    float My = Az*Hx - Ax*Hz;
    float Mz = Ax*Hy - Ay*Hx;
    if (R != nil) {
        if (R.count == 9) {
            R[0] = [NSNumber numberWithFloat:Hx];     R[1] = [NSNumber numberWithFloat:Hy];     R[2] = [NSNumber numberWithFloat:Hz];
            R[3] = [NSNumber numberWithFloat:Mx];     R[4] = [NSNumber numberWithFloat:My];     R[5] = [NSNumber numberWithFloat:Mz];
            R[6] = [NSNumber numberWithFloat:Ax];     R[7] = [NSNumber numberWithFloat:Ay];     R[8] = [NSNumber numberWithFloat:Az];
        } else if (R.count == 16) {
            R[0]  = [NSNumber numberWithFloat:Hx];    R[1]  = [NSNumber numberWithFloat:Hy];    R[2]  = [NSNumber numberWithFloat:Hz];   R[3]  = [NSNumber numberWithInteger:0];
            R[4]  = [NSNumber numberWithFloat:Mx];    R[5]  = [NSNumber numberWithFloat:My];    R[6]  = [NSNumber numberWithFloat:Mz];   R[7]  = [NSNumber numberWithInteger:0];
            R[8]  = [NSNumber numberWithFloat:Ax];    R[9]  = [NSNumber numberWithFloat:Ay];    R[10] = [NSNumber numberWithFloat:Az];   R[11] = [NSNumber numberWithInteger:0];
            R[12] = [NSNumber numberWithInteger:0];   R[13] = [NSNumber numberWithInteger:0];   R[14] = [NSNumber numberWithInteger:0];    R[15] = [NSNumber numberWithInteger:1];
        }
    }
    
    if (I != nil) {
        // compute the inclination matrix by projecting the geomagnetic
        // vector onto the Z (gravity) and X (horizontal component
        // of geomagnetic vector) axes.
        float invE = 1.0f / (float)sqrt(Ex*Ex + Ey*Ey + Ez*Ez);
        float c = (Ex*Mx + Ey*My + Ez*Mz) * invE;
        float s = (Ex*Ax + Ey*Ay + Ez*Az) * invE;
        if (I.count == 9) {
            I[0] = [NSNumber numberWithInteger:1];     I[1] = [NSNumber numberWithInteger:0];   I[2] = [NSNumber numberWithInteger:0];
            I[3] = [NSNumber numberWithInteger:0];     I[4] = [NSNumber numberWithFloat:c];     I[5] = [NSNumber numberWithFloat:s];
            I[6] = [NSNumber numberWithInteger:0];     I[7] = [NSNumber numberWithFloat:-s];    I[8] = [NSNumber numberWithFloat:c];
        } else if (I.count== 16) {
            I[0] = [NSNumber numberWithInteger:1];     I[1] = [NSNumber numberWithInteger:0];    I[2] = [NSNumber numberWithInteger:0];
            I[4] = [NSNumber numberWithInteger:0];     I[5] = [NSNumber numberWithFloat:c];      I[6] = [NSNumber numberWithFloat:s];
            I[8] = [NSNumber numberWithInteger:0];     I[9] = [NSNumber numberWithFloat:-s];     I[10]= [NSNumber numberWithFloat:c];
            I[3] = I[7] = I[11] = I[12] = I[13] = I[14] = [NSNumber numberWithInteger:0];
            I[15] = [NSNumber numberWithInteger:1];
        }
    }
    
    return R;
}

+(NSMutableArray<NSNumber *> *)initArray:(NSInteger)count{
    NSMutableArray<NSNumber *> *array = [NSMutableArray array];
    for (int i=0; i<count; i++) {
        [array addObject:[NSNumber numberWithFloat:0.0]];
    }
    return array;
}

@end
