//
//  NSFaceUtil.m
//  HelloCordova
//
//  Created by 梁仲太 on 2018/6/29.
//

#import "NSFaceUtil.h"
#import <CoreImage/CoreImage.h>

@interface NSFaceUtil ()

@end

@implementation NSFaceUtil

/**
 *人脸检测
 *cordova,插件
 *lat:经度
 *lng:纬度
 *date:拍照日期
 */
-(void)checkFace:(UIImage *)image andVC:(Camera *)cordova andLat:(float)lat andLng:(float)lng andDate:(NSString *)date{
    [cordova successWithMessage:@[@"",[NSNumber numberWithInt:0],[NSNumber numberWithInt:0],[NSNumber numberWithInt:0],[NSNumber numberWithInt:0],@"",[NSNumber numberWithInteger:FACE_CHECKING]]];
    NSLog(@"image=%@",image);
    UIImageView *iv = [UIImageView new];
    iv.frame = CGRectMake(0, 0, [[UIScreen mainScreen] bounds].size.width, [[UIScreen mainScreen] bounds].size.height);
    iv.contentMode = UIViewContentModeScaleAspectFit;
    iv.image = image;
    
    CIImage* ciimage = [CIImage imageWithCGImage:iv.image.CGImage];
    NSDictionary* opts = [NSDictionary dictionaryWithObject:
                          CIDetectorAccuracyHigh forKey:CIDetectorAccuracy];
    CIDetector* detector = [CIDetector detectorOfType:CIDetectorTypeFace
                                              context:nil options:opts];
    //所有的人脸数据
    NSArray* features = [detector featuresInImage:ciimage];
    NSLog(@"检测到的人脸数=%ld个",features.count);
    if (features==nil||features.count == 0) {
        NSLog(@"未检测到人脸");
        [cordova faileWithMessage:@"照片检测不到人脸,请重新拍照"];
    }else if(features.count==1){
        NSLog(@"检测到人脸数为1");
        [cordova continueDisposeBitmap:image andLat:lat andLng:lng andDate:date];
    }else{
        NSLog(@"检测到人脸数大于1");
        [cordova faileWithMessage:@"检测到人脸数大于1，请重新拍照"];
    }
}

@end
