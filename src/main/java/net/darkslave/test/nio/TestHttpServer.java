package net.darkslave.test.nio;

import java.io.IOException;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.Server;
import net.darkslave.nio.impl.ServerImpl;






public class TestHttpServer {
    private static final Logger logger = LogManager.getLogger(TestHttpServer.class);


    public static void main(String[] args) throws IOException {
        SimpleHttpHandler handler = new SimpleHttpHandler();

        Bootstrap boot = new Bootstrap();
        boot.setBossThreadPool(Executors.newSingleThreadExecutor());
        boot.setWorkThreadPool(Executors.newWorkStealingPool());
        boot.setAddress(9999);

        boot.setRequestAcceptor(handler);
        boot.setRequestHandler(handler);

        Server server = new ServerImpl(boot);
        server.start();

        logger.debug("server started");
    }


}
