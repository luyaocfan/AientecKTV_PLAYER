package com.ine.ktv.ineplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class IneSocketClient {
    public static final int Event_Original = 1;
    public static final int Event_MusicOnly = 2;
    public static final int Event_HelpMusic = 3;
    public static final int Event_Play = 4;
    public static final int Event_Pause = 5;
    public static final int Event_Cut = 6;
    public static final int Event_Replay = 7;
    public static final int Event_RefreshList = 8;
    public static final int Event_PlayListEnd = 9;
    public int SerialNo = 0;
    public interface EventListener {
        void OnEvent(IneSocketClient client, int eventType);
    }
    private class BasePackage {
        public int PackageLength;
        public int Command;
        public int SerialNo;
        public int Version;
        public byte Data[];
        public static final int HEADER_LEN = 8;
        public BasePackage() {
        }
        public BasePackage(BasePackage basePackage) {
            CopyHeaderFrom(basePackage);
        }

        public byte[] ToByteArray() {
            byte[] data = new byte[Data.length+8];
            BytesHelper.PutShort((short) PackageLength,data,0);
            BytesHelper.PutShort((short) Command,data,2);
            BytesHelper.PutShort((short) SerialNo,data,4);
            data[6] = 'a';
            data[7] = (byte) Version;
            System.arraycopy(Data,0,data,8,Data.length);
            return data;
        }
        public void CopyHeaderFrom(BasePackage basePackage){
            this.PackageLength = basePackage.PackageLength;
            this.Command = basePackage.Command;
            this.SerialNo = basePackage.SerialNo;
            this.Version = basePackage.Version;
            this.Data = basePackage.Data;
        }
        public void CopyFrom(BasePackage basePackage){
            CopyHeaderFrom(basePackage);
        }
        public boolean ParserByteArray(byte[] header) {
            if(header.length<8)
                return false;
            PackageLength = ((int)BytesHelper.GetShort(header,0))&0xffff;
            Command = ((int)BytesHelper.GetShort(header,2))&0xffff;
            SerialNo = ((int)BytesHelper.GetShort(header,4))&0xffff;
            Version = ((int)header[7])&0xff;
            return true;
        }
        public boolean FromInputStream(InputStream is) {
            try {
                if(PackageLength - HEADER_LEN > is.available())
                    return false;
                Data = new byte[PackageLength - HEADER_LEN];
                is.read(Data);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        public boolean FromByteArray(byte[] data) {
            if(PackageLength - HEADER_LEN > data.length)
                return false;
            Data = new byte[PackageLength - HEADER_LEN];
            System.arraycopy(data,0,Data,0, Data.length);
            return true;
        }
    }
    private class RegisterCMD extends BasePackage {
        public int clientType;
        public int userId;
        byte[] token = new byte[32];
        public static final int CMD = 0x700B;
        public static final int LEN = 46;
        public RegisterCMD() {
            super();
        }
        public RegisterCMD(BasePackage basePackage) {
            super(basePackage);
        }
        @Override
        public void CopyFrom(BasePackage basePackage) {
            if(basePackage.getClass()==getClass()) {
                RegisterCMD registerCMD = (RegisterCMD)basePackage;
                clientType = registerCMD.clientType;
                userId = registerCMD.userId;
                token = registerCMD.token;
            }
        }
        @Override
        public byte[] ToByteArray() {
            Data = new byte[LEN-BasePackage.HEADER_LEN];
            PackageLength = LEN;
            Command = CMD;
            BytesHelper.PutShort((short) clientType, Data,0);
            BytesHelper.PutInt(userId, Data,2);
            System.arraycopy(token,0,Data,6, token.length);
            return super.ToByteArray();
        }
        @Override
        public boolean FromInputStream(InputStream is) {
            if(super.FromInputStream(is)) {
                if(PackageLength == LEN && Command == CMD) {
                    clientType = ((int) BytesHelper.GetShort(Data, 0)) & 0xffff;
                    userId = BytesHelper.GetInt(Data, 2);
                    System.arraycopy(Data, 6, token, 0, token.length);
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean FromByteArray(byte[] data) {
            if(super.FromByteArray(data)) {
                if(PackageLength == LEN && Command == CMD) {
                    clientType = ((int) BytesHelper.GetShort(Data, 0)) & 0xffff;
                    userId = BytesHelper.GetInt(Data, 2);
                    System.arraycopy(Data, 6, token, 0, token.length);
                    return true;
                }
            }
            return false;
       }
    }
    private class ControlCMD extends BasePackage {
        public int funcType;
        public static final int CMD = 0x7008;
        public static final int LEN = 10;
        public ControlCMD() {
            super();
        }
        public ControlCMD(BasePackage basePackage) {
            super(basePackage);
        }
        @Override
        public void CopyFrom(BasePackage basePackage) {
            if(basePackage.getClass()==getClass()) {
                ControlCMD controlCMD = (ControlCMD)basePackage;
                funcType = controlCMD.funcType;
            }
        }
        @Override
        public byte[] ToByteArray() {
            Data = new byte[LEN-BasePackage.HEADER_LEN];
            PackageLength = LEN;
            Command = CMD;
            BytesHelper.PutShort((short) funcType, Data,0);
            return super.ToByteArray();
        }
        @Override
        public boolean FromInputStream(InputStream is) {
            if(super.FromInputStream(is)) {
                if (PackageLength == LEN) {
                    funcType = ((int) BytesHelper.GetShort(Data, 0)) & 0xffff;
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean FromByteArray(byte[] data) {
            if(super.FromByteArray(data) && Command == CMD) {
                if(PackageLength == LEN) {
                    funcType = ((int)BytesHelper.GetShort(Data,0))&0xffff;
                    return true;
                }
            }
            return false;
        }
    }
    private Context context;
    private String serverIP;
    private int serverPort;
    private Thread thread;
    private Socket clientSocket;//客戶端的socket
    private EventListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean bExit;
    //private JSONObject jsonWrite, jsonRead; //從java伺服器傳遞與接收資料的json
    public IneSocketClient(Context context, String serverIP, int serverPort, EventListener listener){
        this.context = context;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.listener=listener;
    }

    public void Open(){
        if(thread == null) {
            bExit = false;
            thread = new Thread(Connection);
            thread.start();
        }
    }

    public void Close(){
        try {
            bExit = true;
            clientSocket.close();
            thread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable Connection = () -> {
        //while(!bExit) {
            try {
                //輸入 Server 端的 IP
                InetAddress serverIp = InetAddress.getByName(serverIP);

                //建立連線
                clientSocket = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                OutputStream os = clientSocket.getOutputStream();
                //取得網路輸入串流
                InputStream is = clientSocket.getInputStream();
                RegisterCMD registerCMD = new RegisterCMD();
                registerCMD.clientType = 3;
                registerCMD.SerialNo = SerialNo++;
                byte[] data = registerCMD.ToByteArray();
                os.write(data);
                byte[] header = new byte[8];
                BasePackage basePackage = new BasePackage();
                ControlCMD controlCMD = new ControlCMD();
                //檢查是否已連線
                while (clientSocket.isConnected()) {
                    //宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    if (is.available() > 8) {
                        is.read(header);
                        basePackage.ParserByteArray(header);
                        SerialNo = basePackage.SerialNo++;
                        while (clientSocket.isConnected() && (is.available() < basePackage.PackageLength - BasePackage.HEADER_LEN))
                            Thread.sleep(10);
                        switch (basePackage.Command) {
                            case ControlCMD.CMD: {
                                controlCMD.CopyHeaderFrom(basePackage);
                                if (controlCMD.FromInputStream(is)) {
                                    final int event = controlCMD.funcType;
                                    mainHandler.post(() -> {
                                        listener.OnEvent(IneSocketClient.this, event);
                                    });
                                    if (event == Event_Cut) {
                                        controlCMD.SerialNo = SerialNo++;
                                        os.write(controlCMD.ToByteArray());
                                    }
                                }
                            }
                            break;
                            default:
                                is.skip(basePackage.PackageLength - BasePackage.HEADER_LEN);
                                break;
                        }

                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (Exception e) {
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("IneSocketClient", "Socket連線=" + e.toString());
                //reconnect?
            }
        try {
            if(!bExit)
                Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //}

    };

}
