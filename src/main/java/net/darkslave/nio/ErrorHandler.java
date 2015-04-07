package net.darkslave.nio;



/**
 * Обработчик ошибок
 */
@FunctionalInterface
public interface ErrorHandler {

    public void handle(Exception e);

}
