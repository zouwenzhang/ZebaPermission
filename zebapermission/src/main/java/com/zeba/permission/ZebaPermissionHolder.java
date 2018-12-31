package com.zeba.permission;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class ZebaPermissionHolder {
    private Method method;
    private String[] permissions;
    private WeakReference<Activity> weakActivity;
    private WeakReference<Object> weakObj;
//    private int requestCode;
    private Object[] parms;

    public ZebaPermissionHolder(Method method, String[] permissions, Object obj, Activity activity){
        this.method=method;
        this.permissions=permissions;
        weakObj=new WeakReference<Object>(obj);
        weakActivity =new WeakReference<Activity>(activity);
        this.method.setAccessible(true);
    }

//    public int getRequestCode() {
//        return requestCode;
//    }
//
//    public void setRequestCode(int requestCode) {
//        this.requestCode = requestCode;
//    }

    public Activity getActivity(){
        if(weakActivity !=null){
            return weakActivity.get();
        }
        return null;
    }

    public Object getObjcet(){
        if(weakObj!=null){
            return weakObj.get();
        }
        return null;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public Object[] getParms() {
        return parms;
    }

    public void setParms(Object[] parms) {
        if(parms!=null&&parms.length>1){
            this.parms=new Object[parms.length-1];
            System.arraycopy(parms,1,this.parms,0,this.parms.length);
        }
    }
}
