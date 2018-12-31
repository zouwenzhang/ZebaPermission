package com.zeba.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ZebaPermissionInject {
    private Map<Object,Map<String,ZebaPermissionHolder>> map=new HashMap<Object,Map<String,ZebaPermissionHolder>>();
    private int requestCode=100;
    private Map<Integer,ZebaPermissionHolder> requestCodeMap=new HashMap<Integer,ZebaPermissionHolder>();
    private static volatile ZebaPermissionInject myPermissionInject;
    public static ZebaPermissionInject get(){
        if(myPermissionInject ==null){
            synchronized (ZebaPermissionInject.class){
                if(myPermissionInject ==null){
                    myPermissionInject =new ZebaPermissionInject();
                }
            }
        }
        return myPermissionInject;
    }
    public Map<Object,Map<String,ZebaPermissionHolder>> getMap(){
        return map;
    }
    public Map<Integer,ZebaPermissionHolder> getRequestCodeMap(){
        return requestCodeMap;
    }
    public int createRequestCode(){
        return ++requestCode;
    }
    public static void register(Activity activity){
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        Map<String,ZebaPermissionHolder> map=get().getMap().get(activity);
        if(map==null){
            map=new HashMap<String,ZebaPermissionHolder>();
            get().getMap().put(activity,map);
        }
        Class cls=activity.getClass();
        Method[] methods= cls.getDeclaredMethods();
        for(int i=0;i<methods.length;i++){
            Method method=methods[i];
            if(method.isAnnotationPresent(ReqPermission.class)){
                ReqPermission reqPermission=method.getAnnotation(ReqPermission.class);
                String[] permissions= reqPermission.value();
                if(permissions.length!=0){
                    ZebaPermissionHolder holder=new ZebaPermissionHolder(method,permissions,activity,activity);
                    map.put(method.getName(),holder);
                }
            }
        }
    }

    public static void unregister(Object obj){
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        Map<String,ZebaPermissionHolder> map=get().getMap().get(obj);
        if(map==null){
            return;
        }
        map.clear();
        get().getMap().remove(obj);
    }

    private static ZebaPermissionHolder getHolder(Object obj){
        StackTraceElement[] ss= Thread.currentThread().getStackTrace();
        if(ss.length<5){
            return null;
        }
        String methodName=ss[4].getMethodName();
        if(obj.getClass().getName().equals(ss[4].getClassName())){
            Map<String,ZebaPermissionHolder> map=get().getMap().get(obj);
            if(map==null){
                return null;
            }
            return map.get(methodName);
        }
        return null;
    }

    public static boolean isGranted(Object... obj){
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        ZebaPermissionHolder holder=getHolder(obj[0]);
        if(holder!=null){
            if(holder.getActivity()!=null){
                String[] strings=holder.getPermissions();
                boolean r=true;
                try{
                    for(String s:strings){
                        int rr=ActivityCompat.checkSelfPermission(holder.getActivity(),s);
                        if(rr!= PackageManager.PERMISSION_GRANTED){
                            r=false;
                            break;
                        }
                    }
                    if(!r){
                        holder.setParms(obj);
                        request(holder);
                    }
                    return r;
                }catch (Exception e){
                    return false;
                }
            }
        }
        return false;
    }

    private static void request(ZebaPermissionHolder holder){
        if(holder==null){
            return;
        }
        if(holder.getActivity()==null){
            return;
        }
        int code=get().createRequestCode();
        get().getRequestCodeMap().put(code,holder);
        ActivityCompat.requestPermissions(holder.getActivity(),holder.getPermissions(),code);
    }

    public void onResult(int requestCode,String[] permissions,int[] results){
        ZebaPermissionHolder holder=getRequestCodeMap().get(requestCode);
        getRequestCodeMap().remove(requestCode);
        if(holder==null){
            return;
        }
        boolean rs=true;
        for(int r:results){
            if(r!=PackageManager.PERMISSION_GRANTED){
                rs=false;
                break;
            }
        }
        if(rs){
            Object obj=holder.getObjcet();
            Method method=holder.getMethod();
            if(obj!=null){
                try{
                    if(method.getParameterTypes().length==0){
                        method.invoke(obj);
                    }else if(holder.getParms()!=null){
                        method.invoke(obj,holder.getParms());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            if(holder.getActivity()!=null){
                ZebaPermissionUtil.showOpenAppSettingDialog(holder.getActivity());
            }
        }
    }

}
