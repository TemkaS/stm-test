package net.darkslave.stm.simple.tcp.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageHandler;





public class ServerHandler implements Runnable {
    private final MessageHandler handler;
    private final Socket socket;


    public ServerHandler(MessageHandler handler, Socket socket) {
        this.handler = handler;
        this.socket  = socket;
    }


    @Override
    public void run() {
        try (
            Closeable __socket = socket;
            InputStream stream = socket.getInputStream();
        ) {

            while (!Thread.interrupted()) {
                Message messg = Message.readFrom(stream);

                if (messg == null)
                    break;

                handler.accept(messg);
            }

        } catch (IOException e) {
            ServerWorker.logger.catching(e);
        }
    }

}
