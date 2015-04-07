package net.darkslave.nio;


import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;





public class ChannelInputStream extends InputStream {
    private final ReadableByteChannel channel;


    public ChannelInputStream(ReadableByteChannel channel) {
        this.channel = channel;
    }


    @Override
    public int read(byte[] target, int offset, int length) throws IOException {
        //
        return 0;
    }


    @Override
    public int read(byte[] target) throws IOException {
        return read(target, 0, target.length);
    }


    private final byte[] buffer1 = new byte[1];

    @Override
    public int read() throws IOException {
        while (true) {
            int read = read(buffer1, 0, 1);

            if (read == 1)
                return buffer1[0] & 255;

            if (read < 0)
                return read;
        }
    }


    private static final int SKIP_BUFFER_SIZE = 1024;
    private byte[] buffer0;

    @Override
    public long skip(long skip) throws IOException {
        if (skip <= 0)
            return 0;

        if (buffer0 == null)
            buffer0 = new byte[SKIP_BUFFER_SIZE];

        long origin = skip;

        while (skip > 0) {
            int need = SKIP_BUFFER_SIZE;

            if (need > skip)
                need = (int) skip;

            int read = read(buffer0, 0, need);

            if (read < 0)
                break;

            skip-= read;
        }

        return origin - skip;
    }


    @Override
    public int available() throws IOException {
        return -1;
    }


    @Override
    public void close() throws IOException {
        channel.close();
    }


    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }


    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean markSupported() {
        return false;
    }

}
