package net.darkslave.nio;



@FunctionalInterface
public interface ErrorHandler {

    public void handle(Exception e);

}
