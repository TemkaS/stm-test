package net.darkslave.stm.core;

import java.io.Closeable;
import java.io.IOException;




public interface Server extends Closeable {


    void start() throws IOException;


    void setHandler(MessageAcceptor handler);


}
