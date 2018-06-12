//
//  NSCamera.m
//  GoodJob
//
//  Created by 梁仲太 on 2018/5/17.
//

#import "Camera.h"
#import "NSCameraUtil.h"
#import "MoLocationManager.h"
#import "CameraWindow.h"
#import "NSSensorUtil.h"
#import <CoreMotion/CoreMotion.h>

@interface Camera()<UIImagePickerControllerDelegate,UINavigationBarDelegate>

@property(nonatomic,strong)CameraWindow *window;
@property(nonatomic,copy)NSString *callbackId;
@property(nonatomic,copy)NSString *name;
@property(nonatomic,assign)CGFloat compression;
@property(nonatomic,assign)BOOL floatingAngle;
@property(nonatomic,strong)CMMotionManager *motionManager;
@property(nonatomic,assign)int angle;
@property(nonatomic,strong)NSMutableArray<NSNumber *> *accelerometerValues;//=new float[3];
@property(nonatomic,strong)NSMutableArray<NSNumber *> *magneticFieldValues;//=new float[3];
@property(nonatomic,strong)NSMutableArray<NSNumber *> *values;//=new float[3];
@property(nonatomic,strong)NSMutableArray<NSNumber *> *rotate;//=new float[9];
@property(nonatomic,assign)double angelX;
@property(nonatomic,assign)double angelY;
@property(nonatomic,assign)double angelZ;
@property(nonatomic,assign)NSInteger count;

@end

@implementation Camera
    
-(void)coolMethod:(CDVInvokedUrlCommand *)command{
    NSLog(@"相机---cool");
    self.callbackId = command.callbackId;
    self.name = [command.arguments objectAtIndex:0];
    if(command.arguments.count>1){
        NSInteger compression = [[command.arguments objectAtIndex:1] integerValue];
        self.compression = compression/100.0;
    }
    
    if(command.arguments.count>2){
        self.floatingAngle = [[command.arguments objectAtIndex:2] boolValue];
    }
    
    self.compression = self.compression<=0?0.8:self.compression;
    //判断权限
    //检查摄像头是否可用
    BOOL useCamera = [UIImagePickerController isCameraDeviceAvailable:UIImagePickerControllerCameraDeviceRear];
    NSLog(@"摄像头useCamera=%d",useCamera);
    if (!useCamera) {
        [self faileWithMessage:@"摄像头不可用"];
        return;
    }
    
    //打开相机
    UIImagePickerControllerSourceType sourceType;
    sourceType = UIImagePickerControllerSourceTypeCamera;
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = NO;
    picker.sourceType = sourceType;
    [self.viewController presentViewController:picker animated:YES completion:nil];
    //获取手机拍摄角度
    [self manager:YES];
    if(self.floatingAngle)
    self.window = [[CameraWindow alloc] initWithFrame:CGRectMake(0, [NSCameraUtil getStatusBarHeight], 210, 50)];
}
    
-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info{
    //关闭陀螺仪
    [self manager:NO];
    //移除罗盘悬浮窗
    [self.window removeFromSuperview];
    self.window = nil;
    
    //获取照片
    NSString *mediaType = [info objectForKey:UIImagePickerControllerMediaType];
    NSLog(@"已经关闭相机=%@",mediaType);
    // 判断获取类型：图片
    if ([@"public.image" isEqualToString:mediaType]){
        //获取原始照片
        __block  UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];
        //获取时间
        __block  NSString *date = [NSCameraUtil format:@"yyyy-MM-dd HH:mm" andOffset:0];
        NSLog(@"日期=%@",date);
        //获取经纬度
        __block  BOOL isOnece = YES;
        [MoLocationManager getMoLocationWithSuccess:^(double lat, double lng){
            isOnece = NO;
            //只打印一次经纬度
            NSLog(@"经纬度 lat lng (%f, %f)", lat, lng);
            NSString *location = [NSString stringWithFormat:@"(%f%f)",lat,lng];
            //加水印
            image = [NSCameraUtil imageWithLogoText:image andText:[NSString stringWithFormat:@"%@%@",date,location] andLeftOffset:14 andBottomOffset:68];
            image = [NSCameraUtil imageWithLogoText:image andText:self.name andLeftOffset:14 andBottomOffset:14];
            NSLog(@"水印图片image=%@", image);
            //压缩图片
            NSData *data = UIImageJPEGRepresentation(image, self.compression);
            UIImage *compressionImage = [UIImage imageWithData:data];
            
            //保存到本地
            NSTimeInterval nowtime = [[NSDate date] timeIntervalSince1970]*1000;
            long long millionTime = [[NSNumber numberWithDouble:nowtime] longLongValue];
            NSString *path = [NSCameraUtil getImageSavePath:[NSString stringWithFormat:@"%d_%lld.jpg",(int)self.angelX,millionTime]];
            [NSCameraUtil saveImage:compressionImage andPath:path];
            NSLog(@"水印图片地址path=%@", path);
            //转成base64
            NSString *base64 = [data base64EncodedStringWithOptions:0];
            NSLog(@"角度angle=%d",self.angle);
            //将结果回传给js
            [self successWithMessage:@[base64,[NSNumber numberWithInt:self.angle],[NSNumber numberWithInt:(int)self.angelX],[NSNumber numberWithInt:(int)self.angelY],[NSNumber numberWithInt:(int)self.angelZ],path]];
            if (!isOnece) {
                [MoLocationManager stop];
            }
        } Failure:^(NSError *error){
            isOnece = NO;
            [self faileWithMessage:@"定位失败!"];
            if (!isOnece) {
                [MoLocationManager stop];
            }
        }];
    }else{
        [self faileWithMessage:@"不支持视频类型"];
    }
    [picker dismissViewControllerAnimated:YES completion:nil];
}
    
-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    [self faileWithMessage:@"cancel"];
    //退出相机
    [picker dismissViewControllerAnimated:YES completion:nil];
}
    
-(void)successWithMessage:(NSArray *)messages{
    if(self.callbackId==nil)return;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:messages];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    [self manager:NO];
    
}
    
-(void)faileWithMessage:(NSString *)message{
    if(self.callbackId==nil)return;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    [self manager:NO];
}

-(void)manager:(BOOL)start{
    if(start){
        //初始化角度
        self.accelerometerValues = [NSSensorUtil initArray:3];//=new float[3];
        self.magneticFieldValues = [NSSensorUtil initArray:3];//=new float[3];
        self.values = [NSSensorUtil initArray:3];//=new float[3];
        self.rotate = [NSSensorUtil initArray:9];//=new float[9];
        
        self.count = 0;
        
        self.motionManager = [[CMMotionManager alloc] init];
        // 获取磁力计传感器的值
        // 1.判断磁力计是否可用
        if (!self.motionManager.isMagnetometerAvailable) {
            return;
        }
        // 2.设置采样间隔
        self.motionManager.magnetometerUpdateInterval = 0.5;
        [self.motionManager startMagnetometerUpdatesToQueue:[NSOperationQueue mainQueue] withHandler:^(CMMagnetometerData *magnetometerData, NSError *error) {
            if (error) return;
            CMMagneticField field = magnetometerData.magneticField;
            self.magneticFieldValues[0] = [NSNumber numberWithFloat:field.x];
            self.magneticFieldValues[1] = [NSNumber numberWithFloat:field.y];
            self.magneticFieldValues[2] = [NSNumber numberWithFloat:field.z];
        }];
        self.motionManager.deviceMotionUpdateInterval = 0.5;
        if([self.motionManager isDeviceMotionAvailable]) {
            [self.motionManager startDeviceMotionUpdatesToQueue:[NSOperationQueue mainQueue] withHandler:^(CMDeviceMotion * _Nullable motion,NSError * _Nullable error) {
                //获取这个然后使用这个角度进行view旋转，可以实现view保持水平的效果，设置一个图片可以测试
                //double rotation = atan2(motion.gravity.x, motion.gravity.y) - M_PI;
                //Gravity 获取手机的重力值在各个方向上的分量，根据这个就可以获得手机的空间位置，倾斜角度等
                
                //if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                self.accelerometerValues[0] = [NSNumber numberWithFloat:motion.userAcceleration.x];
                self.accelerometerValues[1] = [NSNumber numberWithFloat:motion.userAcceleration.y];
                self.accelerometerValues[2] = [NSNumber numberWithFloat:motion.userAcceleration.z];
                //if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
               
                self.rotate = [NSSensorUtil getRotationMatrix:self.rotate andValues:nil andGravity:self.accelerometerValues andFloat:self.magneticFieldValues];
                self.values = [NSSensorUtil getOrientation:self.rotate andValues:self.values];
                //经过SensorManager.getOrientation(rotate, values);得到的values值为弧度
                //转换为角度
                self.angelX = SK_RADIANS_TO_DEGREES([self.values[0] floatValue]) + 180;
                self.angelY = SK_RADIANS_TO_DEGREES([self.values[1] floatValue]);
                self.angelZ = SK_RADIANS_TO_DEGREES([self.values[2] floatValue]);
                //获取手机的倾斜角度(zTheta是手机与水平面的夹角， xyTheta是手机绕自身旋转的角度)：
                double zTheta = atan2(self.angelZ,sqrtf(self.angelX*self.angelX+self.angelY*self.angelY))/M_PI*180.0;
                //double xyTheta = atan2(gravityX,gravityY)/M_PI*180.0;
                self.angle = (int)zTheta+90;
                
                //更新悬浮窗
                [self.window setTips:@[[NSString stringWithFormat:@"x: %f °",round(self.angelX)],[NSString stringWithFormat:@"y: %f  °",round(self.angelY)],[NSString stringWithFormat:@"z: %f  °",round(self.angelZ)]]];
                }];
        }
    }else{
        if(self.motionManager != nil)
        [self.motionManager stopDeviceMotionUpdates];
        [self.motionManager stopMagnetometerUpdates];
        self.motionManager = nil;
    }
}

@end

