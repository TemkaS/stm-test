package net.darkslave.nio;


import java.io.IOException;
import java.io.InputStream;





public class ChannelInputStream extends InputStream {
    private static final int SKIP_BUFFER_SIZE = 4096;
    private final ChannelAction delegate;


    ChannelInputStream(ChannelAction connection) {
        this.delegate = connection;
    }


    @Override
    public int read(byte[] target, int offset, int length) throws IOException {
        return delegate.read(target, offset, length);
    }


    @Override
    public int read(byte[] target) throws IOException {
        return read(target, 0, target.length);
    }


    private final byte[] buffer1 = new byte[1];

    @Override
    public int read() throws IOException {
        int read = read(buffer1, 0, 1);
        if (read == 1)
            return buffer1[0];
        return -1;
    }


    @Override
    public long skip(long skip) throws IOException {
        if (skip <= 0)
            return 0;

        int size = SKIP_BUFFER_SIZE;

        if (size > skip)
            size = (int) skip;

        byte[] temp = new byte[size];
        long origin = skip;

        while (skip > 0) {
            int need = size;

            if (need > skip)
                need = (int) skip;

            int read = read(temp, 0, need);

            if (read < 0)
                break;

            skip -= read;
        }

        return origin - skip;
    }


    @Override
    public int available() throws IOException {
        return -1;
    }


    @Override
    public void close() throws IOException {
        // do nothing
    }


    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean markSupported() {
        return false;
    }

}
