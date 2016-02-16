package com.lamfire.test;

import com.lamfire.code.CRC32;
import com.lamfire.utils.Bytes;
import com.lamfire.utils.IOUtils;
import com.lamfire.utils.RandomUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 15-8-18
 * Time: 上午11:00
 * To change this template use File | Settings | File Templates.
 */
public class SocketClient {

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1",9999));
        OutputStream os = socket.getOutputStream();


        String message = "{\"presence\":{\"from\":\"member001\",\"to\":\"group001\",\"type\":\"subscribe\",\"profile\":{\"avatar\":\"http://www.lamfire.com/avatar.png\",\"gender\":1,\"id\":\"member001\",\"name\":\"lamfire\"}}}";


        byte[] content = message.getBytes();
        int len = content.length + 4 + 4 + 4;
        os.write(Bytes.toBytes(len));                        //all-length
        os.write(Bytes.toBytes(0));                          //id
        os.write(Bytes.toBytes(content.length));            //body-length
        os.write(Bytes.toBytes(0));                          //option
        os.write(content);                                   //body
        os.flush();

        InputStream in = socket.getInputStream();
        int packegLen = IOUtils.readInt(in);
        int id = IOUtils.readInt(in);
        int bodyLen = IOUtils.readInt(in);
        int option = IOUtils.readInt(in);

        byte[] bodyBytes =  IOUtils.readBytes(in,bodyLen);
        System.out.println(new String(bodyBytes));
    }
}
