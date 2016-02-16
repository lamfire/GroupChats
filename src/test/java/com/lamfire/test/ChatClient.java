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
public class ChatClient implements MessageReceivedListener {

    public static void main(String[] args) throws Exception {
        SnakeBuilder builder = new SnakeBuilder();
        builder.host("183.131.150.179").port(9999).messageReceivedListener(new ChatClient()).heartbeatEnable(true).heartbeatInterval(5000);

        Snake snake = builder.build();
        snake.startup();

        ////////////

        GroupMember member = new GroupMember();
        member.setId("member001");
        member.setName("lamfire");
        member.setAvatar("http://www.lamfire.com/avatar.png");
        member.setGender(1);

        Session session = snake.getSession();
        PRESENCE p = new PRESENCE();
        p.setType(PRESENCE.TYPE_SUBSCRIBE);
        p.setFrom("member001");
        p.setTo("group001");
        p.put("profile",member);

        session.send(MessageFactory.message(0,0, JSPPUtils.encode(p)));

        MESSAGE m = new MESSAGE();
        m.setFrom("member001");
        m.setTo("group001");
        m.setBody("你好吗?");

        session.send(MessageFactory.message(0,0, JSPPUtils.encode(m)));

    }

    @Override
    public void onMessageReceived(Session session, Message message) {
        System.out.println("[RECEIVED] : "+message.header() +" -> " + (message.content()==null?"":new String(message.content())));
    }
}
