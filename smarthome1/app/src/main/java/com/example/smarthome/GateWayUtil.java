package com.example.smarthome;

import cn.kingvcn.kv_wsn.GateWay;

public class GateWayUtil {
    //网关操作对象静态单例
    private static GateWay sGateWay;

    //获取全局网关操作对象单例
    public static GateWay getInstance(){
        //如果是第一次获取，此时sGateWay为null,需要实例化
        if (sGateWay == null){
            sGateWay = new GateWay();
        }
        //返回网关操作对象单例
        return sGateWay;
    }
}
