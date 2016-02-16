package com.lamfire.groupchats;

import com.lamfire.hydra.*;
import com.lamfire.hydra.netty.NettySessionMgr;
import com.lamfire.json.JSON;
import com.lamfire.jspp.JSPP;
import com.lamfire.jspp.JSPPUtils;
import com.lamfire.jspp.MESSAGE;
import com.lamfire.jspp.PRESENCE;
import com.lamfire.logger.Logger;
import com.lamfire.utils.Lists;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 16-2-16
 * Time: 上午10:09
 * To change this template use File | Settings | File Templates.
 */
public class GroupChat implements SessionClosedListener{
    private static final Logger LOGGER = Logger.getLogger(GroupChat.class);
    private static final String MEMBER_KEY = "SESSION_MEMBER";
    private final SessionMgr sessionMgr = new NettySessionMgr();
    private String id;

    public GroupChat (String id){
        this.id = id;
        this.sessionMgr.addSessionClosedListener(this);
    }

    public void addGroupMember(Session session,GroupMember member){
        session.attr(MEMBER_KEY,member);
        sessionMgr.add(session);
    }

    public Collection<GroupMember> getGroupMembers(){
        List<GroupMember> members = Lists.newArrayList();
        Collection<Session> sessions = sessionMgr.all();
        for(Session s : sessions){
            GroupMember member = (GroupMember)s.attr(MEMBER_KEY);
            members.add(member);
        }
        return members;
    }

    public boolean exists(String memberId){
        Collection<Session> sessions = sessionMgr.all();
        for(Session s : sessions){
            GroupMember member = (GroupMember)s.attr(MEMBER_KEY);
            if(memberId.equalsIgnoreCase(member.getId())){
                return true;
            }
        }
        return false;
    }


    public void pushMemberRepeatEnterGroupError(Session session,String memberId){
        PRESENCE p = new PRESENCE();
        p.setFrom(id);
        p.setTo(memberId);
        p.setType(PRESENCE.TYPE_ERROR);
        p.put("body","Refuse to repeat join the group");

        Message sendMessage = MessageFactory.message(0,0, JSPPUtils.encode(p));
        session.send(sendMessage);
    }

    public void pushMembers(Session session,String memberId){
        PRESENCE p = new PRESENCE();
        p.setFrom(id);
        p.setTo(memberId);
        p.setType(PRESENCE.TYPE_AVAILABLE);
        p.put("members",getGroupMembers());

        Message sendMessage = MessageFactory.message(0,0, JSPPUtils.encode(p));
        session.send(sendMessage);
    }

    public void onReceivedMessage(Session session ,MESSAGE message){
        message.put("profile",session.attr(MEMBER_KEY));
        Collection<Session> sessions = sessionMgr.all();
        for(Session s : sessions){
            Message sendMessage = MessageFactory.message(0,0,JSPPUtils.encode(message));
            s.send(sendMessage);
        }
    }

    public void onMemberEnterGroup(Session session ,GroupMember member){
        PRESENCE p = new PRESENCE();
        p.setFrom(member.getId());
        p.setTo(id);
        p.setType(PRESENCE.TYPE_SUBSCRIBED);
        p.put("profile",member);
        Collection<Session> sessions = sessionMgr.all();
        for(Session s : sessions){
            Message sendMessage = MessageFactory.message(0,0,JSPPUtils.encode(p));
            s.send(sendMessage);
        }
        LOGGER.info("[" + member.getId() +"] enter group : " + id);
    }

    public void onMemberLeaveGroup(String memberId){
        PRESENCE p = new PRESENCE();
        p.setFrom(memberId);
        p.setTo(id);
        p.setType(PRESENCE.TYPE_UNSUBSCRIBED);
        Collection<Session> sessions = sessionMgr.all();
        for(Session s : sessions){
            Message sendMessage = MessageFactory.message(0,0,JSPPUtils.encode(p));
            s.send(sendMessage);
        }
        LOGGER.info("[" +memberId +"] leaved group : " + id);
    }

    public String getId() {
        return id;
    }

    @Override
    public void onClosed(Session session) {
        GroupMember member = (GroupMember)session.attr(MEMBER_KEY);
        onMemberLeaveGroup(member.getId());
    }

    public void pushDataFormatInvalidError(Session session) {
        PRESENCE p = new PRESENCE();
        p.setFrom(id);
        p.setType(PRESENCE.TYPE_ERROR);
        p.put("body","Your send data format invalid");

        Message sendMessage = MessageFactory.message(0,0, JSPPUtils.encode(p));
        session.send(sendMessage);
    }
}
