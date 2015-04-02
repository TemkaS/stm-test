package net.darkslave.nio;


import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;





public class Connection {
    private static final int STATE_READ  = 1;
    private static final int STATE_WRITE = 2;

    private final SelectionKey  clientKey;

    private final InputStream   inputStream;
    private final OutputStream  outputStream;

    private final Lock          actionLock;
    private final Condition     actionDone;
    private volatile ByteBuffer actionData;


    Connection(SelectionKey clientKey) {
        this.clientKey    = clientKey;

        this.inputStream  = new ConnectionInputStream(this);
        this.outputStream = new ConnectionOutputStream(this);

        this.actionLock   = new ReentrantLock();
        this.actionDone   = this.actionLock.newCondition();
    }


    public void actionLock() throws IOException {
        try {
            actionLock.lockInterruptibly();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }


    public void actionUnlock() {
        actionLock.unlock();
    }


    public void actionAwait() throws IOException {
        try {
            actionDone.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }


    public void actionSignal() {
        actionDone.signalAll();
    }


    public InputStream getInputStream() {
        return inputStream;
    }


    public OutputStream getOutputStream() {
        return outputStream;
    }


    public int read(byte[] target, int offset, int length) throws IOException {
        actionLock();
        try {
            actionData = ByteBuffer.wrap(target, offset, length);
            actionAwait();

            return 0;

        } finally {
            actionUnlock();
        }
    }


    public void write(byte[] source, int offset, int length) throws IOException {
        actionLock();
        try {
            actionData = ByteBuffer.wrap(source, offset, length);
            actionAwait();

        } finally {
            actionUnlock();
        }
    }

}
