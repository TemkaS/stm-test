package net.darkslave.test.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.RequestAcceptor;
import net.darkslave.nio.RequestHandler;






public class TestServer {

    private static class Simple implements RequestHandler, RequestAcceptor {
        private static final byte[] HEADERS_ENDS = "\r\n\r\n".getBytes();


        @Override
        public void handle(InputStream inp, OutputStream out) throws IOException {
            String reqt = readTill(inp, HEADERS_ENDS);
            System.out.println("request " + reqt);

            out.write(("Hello world " + new Date()).getBytes());
        }


        private static String readTill(InputStream input, byte[] what) throws IOException {
            int length = 16536;
            int offset = 0;

            byte[] temp = new byte[length];

            while (true) {
                int search = offset - what.length;

                if (search < 0)
                    search = 0;

                int read = input.read(temp, offset, length - offset);
                if (read < 0)
                    break;

                offset+= read;

               int found = indexOf(temp, search, offset, what);

               if (found >= 0) {
                   offset = found;
                   break;
               }

            }

            return new String(temp, 0, offset, StandardCharsets.UTF_8);
        }


        private static int indexOf(byte[] source, int from, int till, byte[] what) {
            int length = what.length;
            int latest = till - length;

            for (int si = from; si <= latest; si++) {
                int wi = 0;
                while (wi < length && source[si + wi] == what[wi]) wi++;
                if (wi == length)
                    return si;
            }

            return -1;
        }


        @Override
        public boolean accept(InetSocketAddress address) {
            System.out.println("connect " + address);
            return true;
        }

    }


    public static void main(String[] args) throws IOException {
        Simple handler = new Simple();

        Bootstrap boot = new Bootstrap();
        boot.setBossThreadPool(Executors.newSingleThreadExecutor());
        boot.setWorkThreadPool(Executors.newWorkStealingPool());
        boot.setAddress(9999);

        boot.setRequestAcceptor(handler);
        boot.setRequestHandler(handler);

        boot.create().start();

        System.out.println("server started");
    }

}
