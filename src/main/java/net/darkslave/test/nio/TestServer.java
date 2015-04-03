package net.darkslave.test.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import net.darkslave.io.Streams;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.RequestAcceptor;
import net.darkslave.nio.RequestHandler;






public class TestServer {

    private static class Simple implements RequestHandler, RequestAcceptor {

        @Override
        public void handle(InputStream inp, OutputStream out) throws IOException {
            System.out.println("handle some");

            String reqt = Streams.readAll(inp, "UTF-8");

            System.out.println(reqt);
        }

        @Override
        public boolean accept(InetSocketAddress address) {
            System.out.println("connect to " + address);
            return true;
        }

    }


    public static void main(String[] args) throws IOException {
        Simple handler = new Simple();

        Bootstrap boot = new Bootstrap();
        boot.setAddress(9999);

        boot.setRequestAcceptor(handler);
        boot.setRequestHandler(handler);

        boot.create().start();

        System.out.println("server started");
    }

}
