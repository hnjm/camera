//create by 梁仲太 2018-6-28
module.exports = function (ctx) {
    //将相应代码写入activity
    replaceGradleFile(ctx);

    function replaceGradleFile(ctx) {
        const Q = ctx.requireCordovaModule('q');
        const path = ctx.requireCordovaModule('path');
        const fs = ctx.requireCordovaModule('fs');
        const pRoot = ctx.opts.projectRoot;

        const packageJsonPath = path.resolve(__dirname, '../package.json');
        const packageJson = require(packageJsonPath);
        const packageName = 'com/chinamobile/gdwy';
        const appGradle = path.join(pRoot, 'platforms/android/app/build.gradle');
        const mainActivity = path.join(pRoot, 'platforms/android/app/src/main/java/'+packageName+'/MainActivity.java');
        const manifestXml = path.join(pRoot, 'platforms/android/app/src/main/AndroidManifest.xml');
        console.log("--------------camera修改源码开始");
        if (fs.existsSync(manifestXml)) {
            console.log("--------------修改manifestXml");
            //修改manifestXml
            replace_string_in_file(fs,manifestXml,
                        'android:label=\"@string/app_name\"',
                        'android:label=\"@string/app_name\" android:name=\".MyApplication\"');
        }
        if (fs.existsSync(appGradle)) {
            // const data = fs.readFileSync(manifestXml, 'utf8');
            console.log("--------------修改MainActivity");
            //修改mainActivity
            replace_string_in_file(fs,mainActivity,
                        'package com.chinamobile.gdwy;',
                        'package com.chinamobile.gdwy;import android.content.Intent;import android.os.Build;import com.umeng.commonsdk.UMConfigure;import com.umeng.analytics.MobclickAgent;');

            replace_string_in_file(fs,mainActivity,
            'super.onCreate(savedInstanceState);',
            'UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE,"");MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);super.onCreate(savedInstanceState);if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {startForegroundService(new Intent(getApplicationContext(), DataService.class));} else {startService(new Intent(getApplicationContext(), DataService.class));}');
        }
        const appDelegate = path.join(pRoot, 'platforms/ios/网优助手/Classes/AppDelegate.m');
        if (fs.existsSync(appDelegate)) {
            // const data = fs.readFileSync(appDelegate, 'utf8');
            console.log("--------------修改ios");
             //修改AppDelegate
            replace_string_in_file(fs,appDelegate,
                        '#import \"MainViewController.h\"',
                        '#import \"MainViewController.h\" #import <UMCommon/UMCommon.h>');
            replace_string_in_file(fs,appDelegate,
                        'self.viewController = [[MainViewController alloc] init];',
                        'self.viewController = [[MainViewController alloc] init];[UMConfigure initWithAppkey:@\"5e709b30978eea0774044cb3\" channel:@\"gmcc\"];');
        }
    }

    //替换文件中的指定内容
    function replace_string_in_file(fs, filename, to_replace, replace_with) {
        const data = fs.readFileSync(filename, 'utf8');
        const result = data.replace(to_replace, replace_with);
        fs.writeFileSync(filename, result, 'utf8');
    }
    //写入文件
    function write_file(fs, source, target) {
        var readable = fs.createReadStream(source);
        var writable = fs.createWriteStream(target);
        readable.pipe(writable);
    }
}