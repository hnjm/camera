//
//  NSCamera.h
//  GoodJob
//
//  Created by 梁仲太 on 2018/5/17.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <CoreLocation/CLLocationManager.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>

//照相机
static NSInteger const CAMERA_TYPE_CAMERA = 0;
//相册
static NSInteger const CAMERA_TYPE_ALBUM   = 1;
//正常
static NSInteger const NORMAL = 0;
//正在人脸检测
static NSInteger const FACE_CHECKING = 1;

@interface Camera : CDVPlugin
    
-(void)coolMethod:(CDVInvokedUrlCommand *)command;
-(void)continueDisposeBitmap:(UIImage *)image andLat:(float)lat andLng:(float)lng andDate:(NSString *)date;
-(void)successWithMessage:(NSArray *)messages;
-(void)faileWithMessage:(NSString *)message;
-(void)manager:(BOOL)start;
    
    @end
