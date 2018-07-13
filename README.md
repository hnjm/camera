## Camera插件使用说明
* 版本:4.8.0

## 环境配置
* npm 4.4.1 +
* node 9.8.0 +


## 使用流程
#### 注意:
###### ios平台,Mac系统下如果以下的控制台命令遇到权限问题，可以在命令前加sudo

###### ios平台构建，需要在项目的info.list添加以下权限:
###### Privacy-Camera Usage Description String "请同意，使用相机"
###### Privacy-Photo Library Addtions Usage Description String "请同意，保存图片到相册"
###### Privacy-Photo Library Usage Description String "请同意，使用相册"
###### Localiztion native development region  String "请同意，开启定位服务"
###### Privacy-Location Always and When in Use Usage Description String "请同意,开启定位服务"
###### Privacy-Location When In Use Usage Description String "请同意，开启定位服务"

######安卓平台需要添加support支持包：com.android.support:support-v4:27.1.0及以下版本

###### 1.进入项目的根目录，添加相机插件::com.chinamobile.gdwy.camera
* 为项目添加Camera插件，执行:`cordova plugin add com.chinamobile.gdwy.camera`
* 如果要删除插件,执行:`cordova plugin add com.chinamobile.gdwy.camera`
* 为项目添加对应的platform平台,已添加过，此步忽略，执行:
* 安卓平台: `cordova platform add android`
* ios 平台̨:`cordova platform add ios`
* 将插件添加到对应平台,执行: `cordova build`

###### 2.在js文件中,通过以下js方法调用插件，获取照片的信息数据(Base64字符串格式)
*
```javascript
    camera: function(){
        //向native发出照相请求
        //success:成功的回调函数
        //error:失败的回调函数
        //CameraMy:插件名,固定值
        //coolMethod:插件方法，固定值
        //["xxxxx_5442415",50,0,1,0]:插件方法参数，具体对应以下：
        //参数1：左下角水印文本第一行，传1时，日期_经纬度 自动添加在第二行
        //参数2：压缩质量：0-100
        //参数3：0:不使用,  1:使用角度悬浮窗
        //参数4：0:不使用,  1:使用水印
        //参数5：0:相机,    1:打开相册
        //参数6: 0:相机照片不使用人脸检测,1:相机照片使用人脸检测
        //参数7：0:后置摄像头,1：前置摄像头
        cordova.exec(success,error,"CameraMy","coolMethod",["xxxxx_5442415",50,0,1,0,0,0]);
    }
    
    success: function(var result){
        //base64格式的照片数据,3.8.0开始，不再返回base64字符串
        var picBase64 = result[0];
        //照片的拍摄角度,手机垂直于地面为90度。
        var angle     = result[1];
        //angleX
        var angleX    = result[2];
        //angleY
        var angleY    = result[3];
        //angleZ
        var angleZ    = result[4];
        //照片的本地路径
        var path      = result[5];
        //coolMethod的动作类型
        var cameraType= result[6];
        //是否在进行人脸检测 0:没有在检测人脸; 1:正在检测人脸.
        var faceChecking = result[7];
    }

    error: function(var result){
        //照相的异常/拍照/选取相册失败的提示
        alert(result);
    }
```
###### 说明:
* 1.["xxxxx_5442415",50],元素1：用户名_id ，元素2：图片的压缩质量：(0-100),元素3：是否开启罗盘悬浮窗,0：不开启，1：开启,元素4：是否开启水印,0：不开启，1：开启;
* 2.success函数:result是一个数组,元素1：图片的base64的字符串，元素2：照相机的拍摄角度,元素3：x轴角度，元素4：y轴角度，元素5：轴角度，元素6：压缩后的照片保存的手机路径
* 3.具体x，y，z方向的判定，请参照该链接：[https://www.cnblogs.com/mengdd/archive/2013/05/19/3086781.html]
* 4.x:azimuth 方向角，android/ios端用（磁场+加速度）得到的数据范围是（-180～180）,也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西,实际返回为了转成0-360，会全部作+180处理。
    y:pitch   倾斜角   即由静止状态开始，前后翻转
    z:roll    旋转角  即由静止状态开始，左右翻转
* 6.coolMethod的动作类型：0，相机；1，打开相册
* 7.是否在检测人脸中: 0,否；1，是

## 问题反馈
  在使用中有任何问题，可以用以下联系方式.
  
  * 邮件:18520660170@139.com
  * 时间:2018-5-24 16:00:00
