package net.darkslave.nio.impl;

import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class Test {
    private static final Logger logger = LogManager.getLogger(Test.class);


    public static void print(String messg, SelectionKey key) {
        print(messg, key != null ? key.channel() : null);
    }


    public static void print(String messg, Channel chan) {
        logger.info(addr(chan) + " " + messg);
    }


    private static String addr(Channel chan) {
        if (chan instanceof SocketChannel) {
            try {
                return ((SocketChannel) chan).getRemoteAddress().toString();
            } catch (Exception e) {}
        }
        return "???";
    }

}
