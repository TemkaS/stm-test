package net.darkslave.stm.core;

import java.io.Closeable;
import java.io.IOException;




public interface Client extends Closeable {


    void start() throws IOException;


    void setHandler(MessageProducer handler);


    boolean started();

}
