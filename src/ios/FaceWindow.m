//
//  FaceWindow.m
//  HelloCordova
//
//  Created by 梁仲太 on 2018/7/13.
//

#import "FaceWindow.h"

@interface FaceWindow ()

@property(nonatomic,strong)UIImageView *iv;

@end

@implementation FaceWindow

-(id)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if(self){
        self.backgroundColor = [UIColor blackColor];
        self.alpha = 0.7;
        self.windowLevel        = UIWindowLevelAlert +1;
        
        [self makeKeyAndVisible];
    }
    
    self.iv = [[UIImageView alloc] init];
    self.iv.frame = CGRectMake(0,0, frame.size.width, frame.size.height);
    self.iv.contentMode = UIViewContentModeScaleAspectFit;
    [self addSubview:self.iv];
    return self;
}

-(UIImageView *)setFaceIV:(UIImage *)image{
    self.iv.image = image;
    return self.iv;
}

@end
