package net.darkslave.test.nio;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.nio.RequestAcceptor;
import net.darkslave.nio.RequestHandler;





class SimpleHttpHandler implements RequestHandler, RequestAcceptor {
    private static final Logger logger = LogManager.getLogger(TestHttpServer.class);
    private static final byte[] HEADERS_ENDS = "\r\n\r\n".getBytes();


    @Override
    public void handle(ByteChannel channel) throws IOException {
        byte[] array = new byte[16536];
        ByteBuffer buffer = ByteBuffer.wrap(array);

        int read = channel.read(buffer);

        System.out.println("read = " + read);
        System.out.println(new String(array, 0, read, "UTF-8"));

        buffer = ByteBuffer.wrap("Hello world".getBytes(StandardCharsets.UTF_8));
        channel.write(buffer);

    }


    @Override
    public boolean accept(InetSocketAddress address) {
        logger.debug("connect " + address);
        return true;
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

            offset += read;

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
            while (wi < length && source[si + wi] == what[wi])
                wi++;
            if (wi == length)
                return si;
        }

        return -1;
    }


}

