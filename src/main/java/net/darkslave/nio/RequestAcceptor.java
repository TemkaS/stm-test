package net.darkslave.nio;

import java.net.InetSocketAddress;



/**
 * Валидатор клиентов, принимаемых сервером
 */
@FunctionalInterface
public interface RequestAcceptor {

    public boolean accept(InetSocketAddress address);

}
