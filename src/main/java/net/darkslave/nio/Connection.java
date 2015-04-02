package net.darkslave.nio;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;





public class Connection implements Closeable {

    Connection() {
    }


    public InputStream getInputStream() {
        return null;
    }


    public OutputStream getOutputStream() {
        return null;
    }


    public int read(byte[] buffer, int offset, int length) throws IOException {
        return 0;
    }


    public void write(byte[] buffer, int offset, int length) throws IOException {

    }


    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }


    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }


    @Override
    public void close() throws IOException {
    }


}
