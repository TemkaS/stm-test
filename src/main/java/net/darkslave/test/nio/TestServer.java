package net.darkslave.test.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.RequestHandler;
import net.darkslave.nio.Server;






public class TestServer {

    private static class Simple implements RequestHandler {

        @Override
        public void handle(InputStream input, OutputStream output) throws IOException {
            // TODO Auto-generated method stub

        }

    }


    public static void main(String[] args) throws IOException {

        Bootstrap boot = new Bootstrap();
        boot.setAddress("localhost", 9999);

        boot.setRequestHandler(new Simple());

        Server serv = boot.start();



    }

}
