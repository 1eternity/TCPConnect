package com.example.administrator.tcpconnectdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartServer;
    private Button btnStartClient;
    private Button btnSendClient;
    private Button btnSendServer;
    private Button tvServer;
    private Button tvClient;

    //https://blog.csdn.net/qq_29634351/article/details/81458915
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    //收到客户端的消息
    public void MessageClient(String data) {
        tvClient.setText("客户端收到:" + data);
    }

    //收到服务器的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageServer(String data) {
        tvServer.setText("服务器收到:" + data);
    }

    private void initView() {
        btnStartServer = (Button) findViewById(R.id.btn_start_server);
        btnStartClient = (Button) findViewById(R.id.btn_start_client);
        btnSendClient = (Button) findViewById(R.id.btn_send_client);
        btnSendServer = (Button) findViewById(R.id.btn_send_server);
        tvServer = (Button) findViewById(R.id.tv_server);
        tvClient = (Button) findViewById(R.id.tv_client);
        btnStartServer.setOnClickListener(this);
        btnStartClient.setOnClickListener(this);
        btnSendClient.setOnClickListener(this);
        btnSendServer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_server:
                TcpServer.startServer();
                break;
            case R.id.btn_start_client:
//                TcpClient.startClient(getIPAddress(this), 8080);
                TcpClient.startClient("192.168.1.123", 8080);
                break;
            case R.id.btn_send_client:

                Map map = new HashMap();
                map.put("name", "小明");
                map.put("money", 1000000);

                String json = "{name:'小明', money:1000000}";
                byte[] bytes = json.getBytes(Charset.forName("GBK"));

                byte[] msg = new byte[4 + bytes.length];
                msg[0] = (byte)((bytes.length >> 24) & 0x0FF);
                msg[1] = (byte)((bytes.length >> 16) & 0x0FF);
                msg[2] = (byte)((bytes.length >> 8) & 0x0FF);
                msg[3] = (byte)((bytes.length >> 0) & 0x0FF);

                System.arraycopy(bytes, 0, msg, 4, bytes.length);

//                TcpServer.sendTcpMessage(msg);

                TcpServer.sendTcpMessage(bytes);

                break;
            case R.id.btn_send_server:
                TcpClient.sendTcpMessage(null);
                break;
        }
    }
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
