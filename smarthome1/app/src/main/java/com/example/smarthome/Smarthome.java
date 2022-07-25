package com.example.smarthome;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import cn.kingvcn.kv_wsn.GateWay;
import cn.kingvcn.kv_wsn.base.Command;
import cn.kingvcn.kv_wsn.listener.OnSensorChangeListener;
import cn.kingvcn.kv_wsn.sensor.SensorBase;
import cn.kingvcn.kv_wsn.sensor.SensorCollectorATH;
import cn.kingvcn.kv_wsn.sensor.SensorCollectorCO2;
import cn.kingvcn.kv_wsn.sensor.SensorCollectorLightIntensity;
import cn.kingvcn.kv_wsn.sensor.SensorCollectorSTH;
import cn.kingvcn.kv_wsn.sensor.SensorCurtain;
import cn.kingvcn.kv_wsn.sensor.SensorIT;
import cn.kingvcn.kv_wsn.sensor.SensorRelay;
import cn.kingvcn.kv_wsn.sensor.SensorSmoke;

public class Smarthome extends AppCompatActivity {

    //空气温度显示view
    private TextView mTvAirTemperature;
    //空气湿度显示view
    private TextView mTvAirHumidity;
    //光照度显示view
    private TextView mTvLight;
    //二氧化碳显示view
    private SensorCurtain mSensorCurtain;

    //烟雾和人体
    private TextView mTvSmoke;
    private TextView mTvIt;


    //控制风水的开关
    private Switch mSwFan;
    //控制灯光的开关
    private Switch mSwLight;
    //控制窗户的开关
    private Switch mSwWindow;
    //控制锁的开关
    private Switch mSwLock;

    //控制是否实时获取的开关
    private Switch mSwAutoPing;

    //逻辑设置的图片
    private ImageView mImgLogicSetting;
    //控制逻辑的开关
    private Switch mSwLogic;

    //获取网关全局单例对象
    private GateWay mGateWay = GateWayUtil.getInstance();

    //接收到的继电器对象
    private SensorRelay mSensorRelay;

    //实时获取数据线程对象
    private Thread mThread;

    //空气温度阈值最小值
    private int mTemperatureMin;
    //空气温度阈值最大值
    private int mTemperatureMax;
    //空气湿度阈值最小值
    private int mHumidityMin;
    //空气湿度阈值最大值
    private int mHumidityMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smarthome);
        initView();
        initEvent();
        ping();
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        //获取空气温度阈值
        int[] temperature = RecordUtil.getAirTemperature(this);
        //把获取到的空气温度阈值最小值赋值给全局变量
        mTemperatureMin = temperature[0];
        //把获取到的空气湿度阈值最大值赋值给全局变量
        mTemperatureMax = temperature[1];
    }

    /**
     * 初始化View
     */
    private void initView(){
        //绑定空气温度view
        mTvAirTemperature = findViewById(R.id.tv_air_temperature);
        //绑定空气湿度view
        mTvAirHumidity = findViewById(R.id.tv_air_humidity);
        //绑定光照度view
        mTvLight = findViewById(R.id.tv_light);
        //绑定土壤温度view

        //绑定二氧化碳view
        //mTvCo2 = findViewById(R.id.tv_co2);
        mTvSmoke = findViewById(R.id.tv_smoke);
        mTvIt = findViewById(R.id.tv_shake);
        //绑定控制风扇状态的开关
        mSwFan = findViewById(R.id.sw_fan);
        //绑定控制灯泡状态的开关
        mSwLight = findViewById(R.id.sw_light);
        //绑定控制窗户的开关
        mSwWindow = findViewById(R.id.sw_window);
        //绑定控制电磁式的开关
        mSwLock = findViewById(R.id.sw_lock);

        //绑定实时获取开关
        mSwAutoPing = findViewById(R.id.sw_auto_ping);

        //绑定设置逻辑的图片
        mImgLogicSetting = findViewById(R.id.img_logic_setting);
        //绑定逻辑开关
        mSwLogic = findViewById(R.id.sw_logic);
//        open = findViewById(R.id.open);
//        stop = findViewById(R.id.stop1);
//        close = findViewById(R.id.close);

    }

    /**
     * 初始化事件
     */
    private void initEvent(){
        //注册逻辑设置的点击事件
        mSwLogic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogicSetting();
            }
        });

        //注册逻辑开关状态事件
        mSwLogic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (mTemperatureMax <= mTemperatureMin){
                        mSwLogic.setChecked(false);
                        Toast.makeText(Smarthome.this, "空气温度阈值有问题",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //如果开始智能逻辑需要实时获取数据，所以主动打开实时获取数据开关
                    if (!mSwAutoPing.isChecked()){
                        mSwAutoPing.setChecked(true);
                    }
                    //把实时获取数据开关设置为不可用，防止用户主动关闭实时数据，导致智能逻辑不可用
                    mSwAutoPing.setEnabled(false);
                }else {
                    //关闭智能逻辑开始时需要把实时获取数据开关开放使用
                    mSwAutoPing.setEnabled(true);
                }
            }
        });

        //注册监听风扇开关的事件
        mSwFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //调用控制继电器的方法
                controlRelay(0, b);
            }
        });
        //注册监听控制灯泡开关的事件
        mSwLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //调用控制继电器的方法
                controlRelay(1, b);
            }
        });
        //注册监听控制窗户开关的事件
        mSwWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //调用控制继电器的方法
                controlRelay(2, b);
            }
        });
        //注册监听控制电磁式开关的事件
        mSwLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //调用控制继电器的方法
                controlRelay(3, b);
            }
        });
//
//        open.setOnClickListener(new View.OnClickListener() {
//            @Override
//	            public void onClick(View v) {
//               	                controlCurtain((byte) 1);
//               	            }
//      });
//        	        //注册电动窗帘暂停事件
//        stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//	            public void onClick(View v) {
//                controlCurtain((byte) 0);
//                	            }
//	                });
//        	        //注册电动窗帘关闭事件
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//	            public void onClick(View v) {
//                controlCurtain((byte) 2);
//               	            }
//	        });
//


        //注册实时获取数据开关监听事件
        mSwAutoPing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    startAutoPing();
                }else {
                    stopAutoPing();
                }
            }
        });

        //注册监听传感器数据的事件
        mGateWay.addSensorChangeListener(new OnSensorChangeListener() {
            @Override
            public void onSensorChanged(SensorBase sensorBase) {
                //这里是子线程 所以说在更新UI的时候要把更新代码块交给主线程（UI线程）去执行
                if (sensorBase instanceof SensorCollectorATH){
                    //采集器空气温湿度
                    SensorCollectorATH sensorCollectorATH = (SensorCollectorATH) sensorBase;
                    //把更新UI的代码块交给主线程去执行
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //更新空气温度数据
                            mTvAirTemperature.setText("空气温度:" +
                                    sensorCollectorATH.getAirTemperature() + "℃");
                            //更新空气湿度数据
                            mTvAirHumidity.setText("空气湿度:" +
                                    sensorCollectorATH.getAirHumidity() + "%");
                        }
                    });
                    //判断智能逻辑是否是打开的状态
                    if (mSwLogic.isChecked()){
                        if (sensorCollectorATH.getAirTemperature() >= mTemperatureMax){
                            //如果空气温度高于阈值最大值 打开风扇
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSwFan.setChecked(true);
                                }
                            });
                        }else if (sensorCollectorATH.getAirTemperature() <= mTemperatureMin){
                            //如果空气温度低于阈值最小值 关闭风扇
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSwFan.setChecked(false);
                                }
                            });
                        }

                        if (sensorCollectorATH.getAirHumidity() >= mHumidityMax){
                            //如果空气湿度高于阈值最大值 打开窗户
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSwWindow.setChecked(true);
                                }
                            });
                        }else if (sensorCollectorATH.getAirHumidity() <= mHumidityMin){
                            //如果空气湿度低于阈值最小值 关闭窗户
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSwWindow.setChecked(false);
                                }
                            });
                        }
                    }

                }else if (sensorBase instanceof SensorSmoke) {
                    SensorSmoke sensorSmoke = (SensorSmoke) sensorBase;
                    final String content = "烟雾:" + (sensorSmoke.getSmokeStatus() ? "有烟雾" : "无烟雾");
                    //更新UI应该交给主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvSmoke.setText(content);
                        }
                    });
                    /*              */
                }else if (sensorBase instanceof SensorIT) {
                    SensorIT sensorIT = (SensorIT) sensorBase;
                    final String content = "热感:" + (sensorIT.getITStatus() ? "有人" : "无人");
                    //更新UI应该交给主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvIt.setText(content);
                        }
                    });

                }else if (sensorBase instanceof SensorCollectorLightIntensity){
                    //采集器光照度
                    SensorCollectorLightIntensity sensorCollectorLightIntensity =
                            (SensorCollectorLightIntensity) sensorBase;
                    //把更新UI的代码块交给主线程去执行
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //更新光照度数据
                            mTvLight.setText("光照:" +
                                    sensorCollectorLightIntensity.getLightIntensity() + "lux");
                        }
                    });
                }else if (sensorBase instanceof SensorCollectorCO2){
                    //采集器Co2
                    SensorCollectorCO2 sensorCollectorCO2 = (SensorCollectorCO2) sensorBase;
                    //把更新UI的代码块交给主线程去执行
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //更新Co2数据
                            //  mTvCo2.setText("Co2:" + sensorCollectorCO2.getCO2() + "ppm");
                        }
                    });
                }else if (sensorBase instanceof SensorRelay){
                    //继电器
                    mSensorRelay = (SensorRelay) sensorBase;
                }
            }
        });
    }

    /**
     * 控制继电器
     * @param index 通道号下标
     * @param channelStatus 通道状态
     */
    private void controlRelay(int index, boolean channelStatus){
        //判断是否有继电器设备
        if (mSensorRelay == null){
            Toast.makeText(this, "暂无继电器", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取继电器状态数组
        boolean[] status = mSensorRelay.getRelayStatus();
        //判断通道状态是否和需要控制的不一样
        if (status[index] != channelStatus){
            //改变通道状态
            status[index] = channelStatus;
            //生成控制继电器的命令
            byte[] command = Command.makeRelayCommand(mSensorRelay, status);
            //发送命令
            mGateWay.sendData(command);
        }
    }

    /**
     * 发送Ping指令
     */
    private void ping(){
        mGateWay.getAllDevice();
    }

    /**
     * 开始自动获取数据
     * 开启线程
     */
    private void startAutoPing(){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    mGateWay.getAllDevice();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        mThread.start();
    }

    /**
     * 结束自动获取数据
     * 结束线程
     */
    private void stopAutoPing(){
        if (mThread != null && !mThread.isInterrupted()){
            mThread.interrupt();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoPing();
    }


    /**
     * 展示逻辑设置界面
     */
    private void showLogicSetting(){
        //初始化提示框Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //导入自定义的布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_logic_setting, null);
        //导入自定义布局中的温度阈值输入框
        EditText editTemperatureMin = view.findViewById(R.id.edit_temperature_min);
        EditText editTemperatureMax = view.findViewById(R.id.edit_temperature_max);
        //回显空气温度阈值
        if (mTemperatureMin != -1){
            editTemperatureMin.setText(String.valueOf(mTemperatureMin));
        }
        if (mTemperatureMax != -1){
            editTemperatureMax.setText(String.valueOf(mTemperatureMax));
        }
        //导入自定义布局中的湿度阈值输入框
        EditText editHumidityMin = view.findViewById(R.id.edit_humidity_min);
        EditText editHumidityMax = view.findViewById(R.id.edit_humidity_max);
        //回显空气湿度阈值
        editHumidityMin.setText(String.valueOf(mHumidityMin));
        editHumidityMax.setText(String.valueOf(mHumidityMax));
        //把自定义布局设置到对话框内容
        builder.setView(view)
                //添加对话框"确定"按钮
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //获取温度阈值最小值
                        String temperatureMin = editTemperatureMin.getText().toString();
                        //转换成整形并赋值给全局变量
                        mTemperatureMin = Integer.parseInt(temperatureMin);
                        //获取温度阈值最大值
                        String temperatureMax = editTemperatureMax.getText().toString();
                        //转换成整形并赋值给全局变量
                        mTemperatureMax = Integer.parseInt(temperatureMax);

                        //储存空气温度阈值
                        RecordUtil.saveAirTemperature(Smarthome.this,
                                mTemperatureMin, mTemperatureMax);

                        //获取湿度阈值最小值
                        String humidityMin = editHumidityMin.getText().toString();
                        //转换成整形并赋值给全局变量
                        mHumidityMin = Integer.parseInt(humidityMin);
                        //获取湿度阈值最大值
                        String humidityMax = editHumidityMax.getText().toString();
                        //转换成整形并赋值给全局变量
                        mHumidityMax = Integer.parseInt(humidityMax);
                    }
                })
                //添加对话框"取消"按钮
                .setNegativeButton("取消", null)
                //把对话框显示出来
                .show();
    }
}