//
//  NSFaceUtil.h
//  HelloCordova
//
//  Created by 梁仲太 on 2018/6/29.
//

#import <Foundation/Foundation.h>
#import "Camera.h"

@interface NSFaceUtil : NSObject

-(void)checkFace:(UIImage *)image andVC:(Camera *)cordova andLat:(float)lat andLng:(float)lng andDate:(NSString *)date andArray:(NSArray *)features andString:(NSString *)preTag andCameraType:(NSInteger)cameraType;

@end
