package com.zeba.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;

public class ZebaPermission {
    private static volatile ZebaPermission myPermission;
    public static ZebaPermission get(){
        if(myPermission ==null){
            synchronized (ZebaPermission.class){
                if(myPermission ==null){
                    myPermission =new ZebaPermission();
                }
            }
        }
        return myPermission;
    }

    /**请求权限使用*/
    public static void request(Activity activity, String[] permissions, GrantedListener grantedListener, DeniedListener deniedListener){
        get().requestPermission(activity,permissions,grantedListener,deniedListener);
    }

    /**activity中的回调*/
    public static void onRequestPermissionsResult(Activity activity,int requestCode,String[] permissions,int[] results){
        get().onResult(activity,requestCode,permissions,results);
    }

    private Map<Integer,DeniedListener> deniedListenerMap;
    private Map<Integer,GrantedListener> grantedListenerMap;
    private int requestCode=101;

    private ZebaPermission(){
        deniedListenerMap=new HashMap<>();
        grantedListenerMap=new HashMap<>();
    }

    public int createRequestCode(){
        requestCode++;
        return requestCode;
    }

    private void requestPermission(Activity activity, String[] permissions, GrantedListener grantedListener, DeniedListener deniedListener){
        boolean rs=true;
        for(String r:permissions){
            if(ActivityCompat.checkSelfPermission(activity,r)!=PackageManager.PERMISSION_GRANTED){
                rs=false;
                break;
            }
        }
        if(rs){
            grantedListener.granted();
            return;
        }
        int requestCode=createRequestCode();
        deniedListenerMap.put(requestCode,deniedListener);
        grantedListenerMap.put(requestCode,grantedListener);
        ActivityCompat.requestPermissions(activity,permissions,requestCode);
    }

    private void onResult(Activity activity,int requestCode,String[] permissions,int[] results){
        if(deniedListenerMap.get(requestCode)==null&&grantedListenerMap.get(requestCode)==null){
            return;
        }
        boolean rs=true;
        for(int r:results){
            if(r!= PackageManager.PERMISSION_GRANTED){
                rs=false;
                break;
            }
        }
        if(!rs){
            boolean isShow= deniedListenerMap.get(requestCode).denied();
            if(isShow){
                ZebaPermissionUtil.showOpenAppSettingDialog(activity);
            }
        }else{
            grantedListenerMap.get(requestCode).granted();
        }
        deniedListenerMap.remove(requestCode);
        grantedListenerMap.remove(requestCode);
    }
}
