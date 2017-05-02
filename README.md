# PermissionsUtil
针对Android(安卓)6.0及6.0以上的手机动态申请权限的封装，帮助开发者1分钟搞定权限请求

使用方法：

1、在需要申请权限的Activity或者Fragment中 implements PermissionsUtil.IPermissionsCallback

2、在需要申请权限的地方调用：
  
    1、一次申请单个权限
```
        permissionsUtil = PermissionsUtil
                .with(this)
                .requestCode(0)
                .isDebug(true)
                .permissions(PermissionsUtil.Permission.Camera.CAMERA)
                .request();
```
                
    2、一次申请多个权限
```
        permissionsUtil=  PermissionsUtil
                .with(this)
                .requestCode(1)
                .isDebug(true)//开启log
                .permissions(PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE, 
                        PermissionsUtil.Permission.Location.ACCESS_FINE_LOCATION)
                .request();
```

3、在onRequestPermissionsResult中调用
```
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //需要调用onRequestPermissionsResult
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
```
    
4、在onActivityResult中调用
```
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //监听跳转到权限设置界面后再回到应用
        permissionsUtil.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
```
5、获得申请的权限
```
   @Override
    public void onPermissionsGranted(int requestCode, String... permission) {
        //权限获取回调
    }
```

6、用户拒绝了申请的权限
```
    @Override
    public void onPermissionsDenied(int requestCode, String... permission) {
        //权限被拒绝回调
    }
```
