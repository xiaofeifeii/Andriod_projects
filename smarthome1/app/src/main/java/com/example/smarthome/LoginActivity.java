package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.kingvcn.kv_wsn.GateWay;
import cn.kingvcn.kv_wsn.listener.OnSocketListener;
import cn.kingvcn.kv_wsn.listener.StatusCode;

public class LoginActivity extends AppCompatActivity {

    //定义控件类型变量
    private EditText mEditIP;
    private EditText mEditPort;
    private Button mBtbLogin;

    //获取全局Gateway网关对象，单例
    private GateWay mGateWay = GateWayUtil.getInstance();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        init();
    }

    private void initView() {
        //绑定控件
        mEditIP = findViewById(R.id.edit_ip);
        mEditPort = findViewById(R.id.edit_port);
        mBtbLogin = findViewById(R.id.btn_login);

        //注册登录按钮的事件
        mBtbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkGateway();
            }
        });
    }


    private void init() {
        //获取储存的ip地址
        String ip = RecordUtil.getIp(this);
        //获取储存的port
        int port = RecordUtil.getPort(this);
        //如果IP不为null，则显示获取到的IP地址
        if (ip != null) {
            mEditIP.setText(ip);
        }
        //如果Port不为null，则显示获取到的Port地址
        if (port != -1) {
            mEditPort.setText(String.valueOf(port));
        }

    }

    private void linkGateway() {
        //获取输入的IP
        String ip = mEditIP.getText().toString();
        //判断ip的长度是否为0
        if (ip.length() == 0) {
            //为0则提示用户没有输入IP并不执行登录代码
            Toast.makeText(LoginActivity.this,
                    "未输入IP地址，请输入", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取端口号
        String portStr = mEditPort.getText().toString();
        //判断Port的长度是否为0
        if (portStr.length() == 0) {
            //提示未输入端口号，且不执行下面代码
            Toast.makeText(LoginActivity.this,
                    "请输入端口号", Toast.LENGTH_SHORT).show();
            return;
        }

        //把String类型转化为整数类型
        int port = Integer.parseInt(portStr);
        mGateWay.connectGateWay(ip,
                port,
                new OnSocketListener() {
                    @Override
                    public void onCommandResponse(final int i) {
                        //这里是子线程
                        //连接网关结果触发代码区域
                        //在主线程更新UI，提示连接网关成功或不成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (i == StatusCode.SUCCESS) {
                                    RecordUtil.saveIpAndPort(LoginActivity.this, ip, port);

                                    Toast.makeText(LoginActivity.this,
                                            "网关连接成功", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, Smarthome.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "网关连接失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
    }
}

