package net.darkslave.nio;



@FunctionalInterface
public interface ExceptionHandler {

    public void handle(Exception e);

}
