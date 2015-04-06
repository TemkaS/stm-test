package net.darkslave.stm.core;

import java.io.Closeable;
import java.io.IOException;




public interface Client extends Closeable {


    void init() throws IOException;


    void send(Message messg) throws IOException;


}
