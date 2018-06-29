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

static NSInteger const CAMERA_TYPE_CAMERA = 0;
static NSInteger const CAMERA_TYPE_ALBUM   = 1;

@interface Camera : CDVPlugin
    
-(void)coolMethod:(CDVInvokedUrlCommand *)command;
-(void)successWithMessage:(NSArray *)messages;
-(void)faileWithMessage:(NSString *)message;
-(void)manager:(BOOL)start;
    
    @end
