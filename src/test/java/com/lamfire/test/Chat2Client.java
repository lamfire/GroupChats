package com.lamfire.test;

import com.lamfire.groupchats.GroupMember;
import com.lamfire.hydra.*;
import com.lamfire.jspp.JSPPUtils;
import com.lamfire.jspp.MESSAGE;
import com.lamfire.jspp.PRESENCE;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 16-2-16
 * Time: 下午3:05
 * To change this template use File | Settings | File Templates.
 */
public class Chat2Client implements MessageReceivedListener {

    public static void main(String[] args) throws Exception {
        SnakeBuilder builder = new SnakeBuilder();
        builder.host("127.0.0.1").port(9999).messageReceivedListener(new Chat2Client()).heartbeatEnable(true).heartbeatInterval(5000);

        Snake snake = builder.build();
        snake.startup();

        ////////////

        GroupMember member = new GroupMember();
        member.setId("member009");
        member.setName("lamfire");
        member.setAvatar("http://www.lamfire.com/avatar.png");
        member.setGender(1);

        Session session = snake.getSession();
        PRESENCE p = new PRESENCE();
        p.setType(PRESENCE.TYPE_SUBSCRIBE);
        p.setFrom("member009");
        p.setTo("group001");
        p.put("profile",member);

        session.send(MessageFactory.message(0,0, JSPPUtils.encode(p)));

        MESSAGE m = new MESSAGE();
        m.setFrom("member009");
        m.setTo("group001");
        m.setBody("你好吗?");

        session.send(MessageFactory.message(0,0, JSPPUtils.encode(m)));

    }

    @Override
    public void onMessageReceived(Session session, Message message) {
        System.out.println("[RECEIVED] : "+message.header() +" -> " + (message.content()==null?"":new String(message.content())));
    }
}
