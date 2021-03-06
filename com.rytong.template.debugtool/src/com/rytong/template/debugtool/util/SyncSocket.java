package com.rytong.template.debugtool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.erlide.jinterface.ErlLogger;

import com.rytong.template.debugtool.util.SyncSocket.ClientThread;

public class SyncSocket {

    private static List<ClientThread> clients;
    private HashMap<ClientThread, String> download_store;
    private boolean status = false;

    public int port = 7003;
    public ServerSocket server = null;
    public ServerThread serverThread ;
    public Socket socket = null;
    public SyncSocket(){

    }

    public SyncSocket getSyncSocket(){
        return this;
    }

    public boolean getServerStatus(){
        return status;
    }


    public void CreateSocket(int port){
        try {
            this.port = port;
            server = new ServerSocket(port);
            clients = new ArrayList<ClientThread>();
            serverThread = new ServerThread(server);
            download_store = new HashMap<ClientThread, String>();
            serverThread.start();
            status = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            status = false;

        }

    }


    @SuppressWarnings("deprecation")
    public void CloseSocket(){
        try {

            for(int i = clients.size() -1; i >=0; i--){
                ErlLogger.debug("client:"+i);
                clients.get(i).getWriter().println("close");
                clients.get(i).getWriter().flush();
                //clients.get(i).stop1();
                clients.get(i).socket.shutdownInput();
                //clients.get(i).getReader().close();
                //clients.get(i).getWriter().close();
                //clients.get(i).writer.close();
                //clients.get(i).socket.close();
                //clients.get(i).stop1();
                //clients.remove(i);
            }
            if (serverThread != null){
                serverThread.w_flag = false;
                //serverThread.stop_server_socket();
                serverThread.stop();
            }

            ErlLogger.debug(" serverThread stop");
            if (server != null)
                server.close();

            status = false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            ErlLogger.debug("CloseSocket stack~");
            e.printStackTrace();
            status = true;
        } //关闭ServerSocket

    }

    public int sendServerMsg(String msg){
        //ErlLogger.debug("msg:"+msg);
        ErlLogger.debug("size:"+clients.size());
        int len = clients.size();
        if (len == 0){
            ErlLogger.debug("return 2");
            return 2;
        }
        for (int i = len-1; i >= 0; i--){
            msg = "s2bContent&$!"+msg+"$&end";
            clients.get(i).getWriter().print(msg);
            clients.get(i).getWriter().flush();
        }
        return 1;
    }

    public String getClientMsg(){
        //ErlLogger.debug("msg:"+msg);
        ErlLogger.debug("size:"+clients.size());
        int len = clients.size();
        if (len == 0){
            ErlLogger.debug("return 2");
            return null;
        }
        for (int i = len-1; i >= 0; i--){
            ClientThread client = clients.get(i);
            clients.get(i).getWriter().flush();
            String tmp_msg = download_store.get(client);
            //ErlLogger.debug("tmp_msg 2:"+tmp_msg);
            if (tmp_msg != null){
                //download_store.put(client, "");
                return tmp_msg;
            }
        }
        return "";
    }


    class ServerThread extends Thread{
        private ServerSocket serverSocket;
        public  boolean w_flag = true;

        public ServerThread(ServerSocket serverSocket){
            this.serverSocket = serverSocket;
        }

        public void run(){
            ErlLogger.debug("ServerThread   run~");
            while(w_flag){
                Socket socket;
                try {
                    ErlLogger.debug("before accepted");
                    socket = serverSocket.accept();
                    ErlLogger.debug("accepted");
                    ClientThread client = new ClientThread(socket);

                    client.start();
                    clients.add(client);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    ErlLogger.debug("accept except~ion");
                    e.printStackTrace();
                }
            }
        }

        public void stop_server_socket(){
            try {
                if (!serverSocket.isClosed())
                    serverSocket.close();
            } catch (IOException e) {
                ErlLogger.debug("socket server");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class ClientThread extends Thread{
        private volatile Thread blinker;
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        //private String cur_msg = "";
        StringBuilder tmp_sb = new StringBuilder();
        //private User user;

        public void start(){
            blinker = new Thread(this);
            blinker.start();
        }

        public void stop1(){
            ErlLogger.debug("client stop");
            Thread moribund = blinker;
            blinker = null;
            moribund.interrupt();

        }

        public ClientThread(Socket socket){
            try {
                this.socket = socket;
                InputStreamReader tmp_r = new InputStreamReader(socket.getInputStream());

                reader = new BufferedReader(tmp_r);

                writer = new PrintWriter(socket.getOutputStream());
                ErlLogger.debug("new client!");
                //String inf = reader.readLine();
                // ErlLogger.debug("inf:"+inf);
                //writer.println("connected");
                //writer.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @SuppressWarnings("deprecation")
        public void run(){

            Thread thisThread = Thread.currentThread();
            char tmp_chars[] = new char[1024];
            int len;
            while(blinker == thisThread){
                try {
                    //message = reader.readLine();
                    len = reader.read(tmp_chars);
                    ErlLogger.debug("len:~~~"+len);
                    //                    if (message.equals("close")){
                    //                        reader.close();
                    //                        writer.close();
                    //                        socket.close();
                    //
                    //                        for(int i=clients.size()-1; i>=0;i-- ){
                    //                            if (clients.get(i) == this){
                    //                                ClientThread temp = clients.get(i);
                    //                                clients.remove(i);
                    //                                temp.stop();
                    //                                return;
                    //                            }
                    //                        }
                    //                    } else {
                    //                        ErlLogger.debug("get:"+message);
                    //                        cur_msg = cur_msg.concat(message).concat("\r\n");
                    //                        download_store.put(this, cur_msg);
                    //                    }

                    //ErlLogger.debug("tmp_sb:"+tmp_sb.toString());
                    ErlLogger.debug("len:"+len);
                    if (len != -1) {
                        String tmp_str = new String(tmp_chars, 0, len);
                        String tmp_strs[] = tmp_str.split("#EditorMessage#");
                        //ErlLogger.debug("tmp_str:"+tmp_str);
                        ErlLogger.debug("tmp_strs:"+tmp_strs.length);
                        if (tmp_strs[0].equalsIgnoreCase("EditorMessageStart")){

                            if (tmp_strs.length >2 ){
                                ErlLogger.debug("EditorMessageStart:"+tmp_strs[2]);
                                if (tmp_strs[2].equalsIgnoreCase("EditorMessageEnd")){
                                    ErlLogger.debug("EditorMessageStart: 2");
                                    download_store.put(this, process_msg(tmp_sb.toString()));
                                    tmp_sb = new StringBuilder();
                                } else {
                                    ErlLogger.debug("EditorMessageStart: else");
                                    tmp_sb = tmp_sb.append(tmp_strs[1]);
                                }
                            } else if (tmp_strs.length == 2) {
                                ErlLogger.debug("EditorMessageStart: else");
                                tmp_sb = tmp_sb.append(tmp_strs[1]);
                            }
                        } else {
                            if (tmp_strs.length >1 ){
                                //ErlLogger.debug("tmp_strs:"+tmp_strs[1].length());
                                //ErlLogger.debug("tmp_strs:"+tmp_strs[1].trim().length());
                                ErlLogger.debug("result:" + tmp_strs[1].trim().equalsIgnoreCase("EditorMessageEnd"));

                                if (tmp_strs[1].trim().equalsIgnoreCase("EditorMessageEnd")){
                                    ErlLogger.debug("EditorMessageEnd:");
                                    tmp_sb = tmp_sb.append(tmp_strs[0]);
                                    download_store.put(this, process_msg(tmp_sb.toString()));
                                    tmp_sb = new StringBuilder();
                                } else {
                                    ErlLogger.debug("EditorMessageEnd: else");
                                    tmp_sb = tmp_sb.append(tmp_str);
                                }
                            }else {
                                //ErlLogger.debug("EditorMessageEnd: else");
                                tmp_sb = tmp_sb.append(tmp_str);
                            }
                        }
                    } else {
                        reader.close();
                        writer.close();
                        socket.close();

                        for(int i=clients.size()-1; i>=0;i-- ){
                            if (clients.get(i) == this){
                                ClientThread temp = clients.get(i);
                                clients.remove(i);
                                temp.stop1();
                                break;
                            }
                        }
                        break;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }


        private String process_msg(String msg){
            //ErlLogger.debug("msg:"+msg);
            String tmp_strs[] = msg.split("#EditorContent#");
            return tmp_strs[1];
        }

        public BufferedReader getReader(){
            return reader;
        }

        public PrintWriter getWriter(){
            return writer;
        }
    }

}


