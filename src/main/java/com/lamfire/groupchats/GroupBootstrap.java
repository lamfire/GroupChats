package com.lamfire.groupchats;

import com.lamfire.utils.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 16-2-16
 * Time: 下午3:55
 * To change this template use File | Settings | File Templates.
 */
public class GroupBootstrap {
    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 9999;
        if(args != null){
            for(String arg : args){
                if(StringUtils.contains(arg, "-p")){
                    port = Integer.valueOf(StringUtils.substringAfter(arg,"-p").trim());
                }

                if(StringUtils.contains(arg,"-h")){
                    host = (StringUtils.substringAfter(arg, "-h").trim());
                }
            }
        }

        GroupServer server = new GroupServer(host,port);
        server.startup();
    }
}
