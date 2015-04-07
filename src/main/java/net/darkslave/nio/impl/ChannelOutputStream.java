package net.darkslave.nio.impl;


import java.io.IOException;
import java.io.OutputStream;





public class ChannelOutputStream extends OutputStream {
    private final ChannelAction delegate;


    ChannelOutputStream(ChannelAction connection) {
        this.delegate = connection;
    }


    @Override
    public void write(byte[] source, int offset, int length) throws IOException {
        delegate.write(source, offset, length);
    }


    @Override
    public void write(byte[] source) throws IOException {
        write(source, 0, source.length);
    }


    @Override
    public void write(int source) throws IOException {
        byte[] temp = new byte[] {
            (byte) source
        };
        write(temp, 0, 1);
    }


    @Override
    public void flush() throws IOException {
        // do nothing
    }


    @Override
    public void close() throws IOException {
        // do nothing
    }

}
