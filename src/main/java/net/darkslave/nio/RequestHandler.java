package net.darkslave.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 * Обработчик запросов
 */
@FunctionalInterface
public interface RequestHandler {

    public void handle(InputStream input, OutputStream output) throws IOException;

}
