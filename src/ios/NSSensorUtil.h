//
//  NSSensorUtil.h
//  HelloCordova
//
//  Created by 梁仲太 on 2018/6/5.
//

#import <Foundation/Foundation.h>

/* 弧度转角度 */
#define SK_RADIANS_TO_DEGREES(radian) \
((radian) * (180.0 / M_PI))
/* 角度转弧度 */
#define SK_DEGREES_TO_RADIANS(angle) \
((angle) / 180.0 * M_PI)  

@interface NSSensorUtil : NSObject

+(NSMutableArray<NSNumber *> *)getOrientation:(NSMutableArray<NSNumber *> *)R andValues:(NSMutableArray<NSNumber *> *)values;
+(NSMutableArray<NSNumber *> *)getRotationMatrix:(NSMutableArray<NSNumber *> *)R andValues:(NSMutableArray<NSNumber *> *)I andGravity:(NSMutableArray<NSNumber *> *)gravity andFloat:(NSMutableArray<NSNumber *> *)geomagnetic;
+(NSMutableArray<NSNumber *> *)initArray:(NSInteger)count;

@end
