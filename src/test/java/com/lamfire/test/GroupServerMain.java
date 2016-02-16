package com.lamfire.test;

import com.lamfire.groupchats.GroupServer;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 16-2-16
 * Time: 下午3:02
 * To change this template use File | Settings | File Templates.
 */
public class GroupServerMain {

    public static void main(String[] args) {
        GroupServer server = new GroupServer("0.0.0.0",9999);
        server.startup();
    }
}
