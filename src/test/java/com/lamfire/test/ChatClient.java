package com.lamfire.test;

import com.lamfire.code.CRC32;
import com.lamfire.groupchats.GroupMember;
import com.lamfire.hydra.*;
import com.lamfire.jspp.JSPPUtils;
import com.lamfire.jspp.MESSAGE;
import com.lamfire.jspp.PRESENCE;
import com.lamfire.utils.RandomUtils;
import com.lamfire.utils.Threads;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 16-2-16
 * Time: 下午3:05
 * To change this template use File | Settings | File Templates.
 */
public class ChatClient implements MessageReceivedListener,SessionCreatedListener {
    static String groupId = "@TGS#3ZDIYQAEY";

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.startup();
    }

    public void startup(){
        SnakeBuilder builder = new SnakeBuilder();
        builder.host("127.0.0.1").port(9999).messageReceivedListener(this).heartbeatEnable(true).sessionCreatedListener(this).heartbeatInterval(5000);
        //builder.host("183.131.150.179").port(9999).messageReceivedListener(this).sessionCreatedListener(this).heartbeatEnable(true).heartbeatInterval(60000).autoConnectRetry(true);
        Snake snake = builder.build();
        snake.startup();
    }

    @Override
    public void onMessageReceived(Session session, Message message) {
        System.out.println("[RECEIVED] : "+message.header() +" -> " + (message.content()==null?"":new String(message.content())));
    }

    @Override
    public void onCreated(Session session) {
        System.out.println("[OPEN] session created - " + session);
        //join group
        GroupMember member = new GroupMember();
        member.setId("member001");
        member.setName("lamfire");
        member.setAvatar("http://www.lamfire.com/avatar.png");
        member.setGender(1);

        PRESENCE p = new PRESENCE();
        p.setType(PRESENCE.TYPE_SUBSCRIBE);
        p.setFrom("member001");
        p.setTo(groupId);
        p.put("profile",member);
        session.send(MessageFactory.message(0,0, JSPPUtils.encode(p)));


        //send message
        MESSAGE m = new MESSAGE();
        m.setFrom("member001");
        m.setTo(groupId);
        m.setBody("你好吗?");
        session.send(MessageFactory.message(0,0, JSPPUtils.encode(m)));
    }
}
