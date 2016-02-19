package com.lamfire.groupchats;

import com.lamfire.hydra.*;
import com.lamfire.json.JSON;
import com.lamfire.jspp.*;
import com.lamfire.logger.Logger;
import com.lamfire.utils.Lists;
import com.lamfire.utils.Maps;
import com.lamfire.utils.ThreadFactory;
import com.lamfire.utils.Threads;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 16-2-16
 * Time: 上午10:00
 * To change this template use File | Settings | File Templates.
 */
public class GroupServer implements MessageReceivedListener,SessionCreatedListener,SessionClosedListener {
    private static final Logger LOGGER = Logger.getLogger(GroupServer.class);
    private final Map<String,GroupChat> groupChats = Maps.newLinkedHashMap();
    private final ScheduledExecutorService service = Threads.newSingleThreadScheduledExecutor(new ThreadFactory("emptyGroupClean"));

    private final String host;
    private final int port;
    private Hydra hydra;

    public GroupServer(String host,int port){
        this.host = host;
        this.port = port;
    }

    public synchronized void startup(){
        if(hydra != null){
            return;
        }

        HydraBuilder builder = new HydraBuilder();
        builder.bind(host).port(port).messageReceivedListener(this).threads(8);
        hydra = builder.build();
        hydra.startup();
        service.scheduleWithFixedDelay(emptyGroupClean,3,3, TimeUnit.MINUTES);
        LOGGER.info("startup on - " + host +":" + port);
    }

    public synchronized void shutdown(){
        if(hydra == null){
            return;
        }
        hydra.shutdown();
        hydra = null;
        LOGGER.info("shutdown on - " + host +":" + port);
    }

    private void handleMessage(String groupId,Session session,MESSAGE message){
        GroupChat groupChat = groupChats.get(groupId);
        if(groupChat == null){
            LOGGER.error("group not found - " +groupId);
            LOGGER.error("send message failed - " +message);
            return;
        }
        groupChat.onReceivedMessage(session,message);
    }

    private void handlePresence(String groupId,Session session,PRESENCE presence){
        GroupChat groupChat = groupChats.get(groupId);
        if(groupChat == null){
            groupChat = new GroupChat(groupId);
            groupChats.put(groupId,groupChat);
        }

        try{
            //进入房间
            if(PRESENCE.TYPE_SUBSCRIBE.equalsIgnoreCase(presence.getType() ) ){
                JSON json = presence.getJSONObject("profile");

                GroupMember member = json.toJavaObject(GroupMember.class);
                if(!groupChat.exists(member.getId())){
                    groupChat.addGroupMember(session,member);
                    groupChat.onMemberEnterGroup(session,member);
                    groupChat.pushGroupMembers(session, member.getId());
                }else{
                    groupChat.pushMemberRepeatEnterGroupError(session,member.getId());
                }
                return;
            }
        }catch (Exception e){
            groupChat.pushDataFormatInvalidError(session);
        }
    }


    @Override
    public void onMessageReceived(Session session, Message message) {
        byte[] bytes = message.content();
        JSPP jspp = JSPPUtils.decode(bytes);
        if(JSPPUtils.getProtocolType(jspp) == ProtocolType.MESSAGE){
            MESSAGE msg = (MESSAGE) jspp;
            String to = msg.getTo();
            handleMessage(to,session,msg);
            return;
        }

        if(JSPPUtils.getProtocolType(jspp) == ProtocolType.PRESENCE){
            PRESENCE msg = (PRESENCE) jspp;
            String to = msg.getTo();
            handlePresence(to,session,msg);
            return;
        }
    }

    private Runnable emptyGroupClean = new Runnable() {
        @Override
        public void run() {
            List<String> emptyList = Lists.newArrayList();
            for(Map.Entry<String,GroupChat> e : groupChats.entrySet()){
                if(e.getValue().isEmptyMembers()){
                    emptyList.add(e.getKey());
                }
            }
            for(String id: emptyList ){
                groupChats.remove(id);
                LOGGER.info("[RemoveEmptyGroup] : " + id);
            }
        }
    };

    @Override
    public void onCreated(Session session) {
        System.out.println("[Created] - " + session);
    }

    @Override
    public void onClosed(Session session) {
        System.out.println("[Closed] - " + session);
    }
}
