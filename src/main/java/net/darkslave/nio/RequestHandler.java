package net.darkslave.nio;

import java.io.IOException;
import java.nio.channels.ByteChannel;



/**
 * Обработчик запросов
 */
@FunctionalInterface
public interface RequestHandler {

    public void handle(ByteChannel channel) throws IOException;

}
