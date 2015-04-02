package net.darkslave.nio;


import java.io.IOException;
import java.io.OutputStream;





public class ConnectionOutputStream extends OutputStream {
    private final Connection connection;


    ConnectionOutputStream(Connection connection) {
        this.connection = connection;
    }


    @Override
    public void write(byte[] source, int offset, int length) throws IOException {
        connection.write(source, offset, length);
    }


    @Override
    public void write(byte[] source) throws IOException {
        write(source, 0, source.length);
    }


    private final byte[] buffer1 = new byte[1];

    @Override
    public void write(int source) throws IOException {
        buffer1[0] = (byte) source;
        write(buffer1, 0, 1);
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
