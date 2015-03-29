package net.darkslave.stm.core;

import java.io.Closeable;
import java.io.IOException;
import net.darkslave.stm.proto.MessageHandler;




public interface Server extends Closeable {


    void start() throws IOException;


    void setHandler(MessageHandler handler);


}
