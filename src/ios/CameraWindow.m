//
//  CameraWindow.m
//  GoodJob
//
//  Created by 梁仲太 on 2018/6/1.
//

#import "CameraWindow.h"

@interface CameraWindow ()

@property(nonatomic,strong)UIButton *button;
@property(nonatomic,strong)NSMutableArray<UILabel *> *lables;

@end

@implementation CameraWindow

-(id)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if(self){
        self.backgroundColor = [UIColor blackColor];
        self.alpha = 0.7;
        self.windowLevel        = UIWindowLevelAlert +1;
        
        [self makeKeyAndVisible];
    }
    
    self.button = [UIButton buttonWithType:UIButtonTypeCustom];
    //self.button.backgroundColor = [UIColor grayColor];
    self.button.frame = CGRectMake(0,0, frame.size.width, frame.size.height);
    self.lables = [NSMutableArray array];
    for (int i=0; i<3; i++) {
        UILabel *lable = [[UILabel alloc] init];
        lable.frame = CGRectMake(i*70, 0, 70, 50);
        lable.textColor = [UIColor whiteColor];
        [self.lables addObject:lable];
        [self.button addSubview:lable];
    }
    
    self.button.layer.cornerRadius = frame.size.width/2;
    [self.button addTarget:self action:@selector(choose) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.button];
    
    // 放一个拖动手势，用来改变控件的位置
    UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(changePosition:)];
    [self.button addGestureRecognizer:pan];
    return self;
}

-(void)setTips:(NSArray<NSString *> *)tips{
    for (int i=0; i<3; i++) {
        [self.lables[i] setText:tips[i]];
    }
}

//按钮事件
-(void)choose{
    NSLog(@"点击了悬浮窗");
}

-(void)changePosition:(UIPanGestureRecognizer *)pan{
    
    CGPoint point = [pan translationInView:self];
    
    CGFloat width = [UIScreen mainScreen].bounds.size.width;
    CGFloat height = [UIScreen mainScreen].bounds.size.height;
    
    CGRect originalFrame = self.frame;
    if (originalFrame.origin.x >= 0 && originalFrame.origin.x+originalFrame.size.width <= width) {
        originalFrame.origin.x += point.x;
    }
    if (originalFrame.origin.y >= 0 && originalFrame.origin.y+originalFrame.size.height <= height) {
        originalFrame.origin.y += point.y;
    }
    self.frame = originalFrame;
    [pan setTranslation:CGPointZero inView:self];
    
    if (pan.state == UIGestureRecognizerStateBegan) {
        self.button.enabled = NO;
    }else if (pan.state == UIGestureRecognizerStateChanged){
        
    } else {
        
    CGRect frame = self.frame;
    //记录是否越界
    BOOL isOver = NO;
        
    if (frame.origin.x < 0) {
        frame.origin.x = 0;
        isOver = YES;
    } else if (frame.origin.x+frame.size.width > width) {
        frame.origin.x = width - frame.size.width;
        isOver = YES;
    }
        
    if (frame.origin.y < 0) {
        frame.origin.y = 0;
        isOver = YES;
    } else if (frame.origin.y+frame.size.height > height) {
        frame.origin.y = height - frame.size.height;
        isOver = YES;
    }
    if (isOver) {
        [UIView animateWithDuration:0.3 animations:^{
            self.frame = frame;
        }];
    }
    self.button.enabled = YES;
    }
}

@end
