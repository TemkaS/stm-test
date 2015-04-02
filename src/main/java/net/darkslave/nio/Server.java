package net.darkslave.nio;

import java.net.InetSocketAddress;




public interface Server {

    public InetSocketAddress address();

    public void stop();

    public boolean started();

}
