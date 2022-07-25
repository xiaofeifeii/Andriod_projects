package com.example.smarthome;

import android.content.Context;
import android.content.SharedPreferences;

public class RecordUtil {
    /**
     * 存储IP和Port
     *
     * @param context 上下文
     * @param ip      ip地址
     * @param port    port
     */


    public static void saveIpAndPort(Context context, String ip, int port) {
        //获取SharedPreferences操作对象
        SharedPreferences.Editor editor =
                context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit();
        editor.putString("ip", ip)
                .putInt("port", port)
                .apply();
    }

    /**
     * 获取ip
     *
     * @param context 上下文
     * @return ip
     */
    public static String getIp(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        return sharedPreferences.getString("ip", null);
    }

    /**
     * 获取port
     *
     * @param context 上下文
     * @return port
     */
    public static int getPort(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("port", -1);
    }

    /**
     * 储存空气温度阈值
     *
     * @param context 上下文
     * @param min     阈值最小值
     * @param max     阈值最大值
     */
    public static void saveAirTemperature(Context context, int min, int max) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putInt("temperatureMin", min)
                .putInt("temperatureMax", max)
                .apply();
    }

    /**
     * 获取空气温湿度阈值
     *
     * @param context 上下文
     * @return 空气温湿度阈值数组
     */
    public static int[] getAirTemperature(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int min = sharedPreferences.getInt("temperatureMin", -1);
        int max = sharedPreferences.getInt("temperatureMax", -1);
        return new int[]{min, max};


    }
}
