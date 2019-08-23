package com.example.administrator.tcpconnectdemo;



import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private static ServerSocket serverSocket;
    private static Socket socket;

    public static void startServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (serverSocket == null) serverSocket = new ServerSocket(8080);
                    while(true) {
                        Log.i("tcp", "服务器等待连接中");
                        acceptClient();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    serverSocket = null;
                }

            }
        }).start();
    }

    public static void acceptClient(){
        try {
            socket = serverSocket.accept();
            Log.i("tcp", "客户端连接上来了");
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(buffer)) != -1) {
                try {
                    String data = new String(buffer, 0, len);
                    Log.i("tcp", "收到客户端的数据-----------------------------:" + data);
                    EventBus.getDefault().post(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }

    public static void sendTcpMessage(final byte[] msg){
        if (socket != null && socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("tcp", "run: 发送数据到客户端start");
                        socket.getOutputStream().write(msg);
                        socket.getOutputStream().flush();
                        Log.i("tcp", "run: 发送数据到客户端end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
