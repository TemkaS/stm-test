package net.darkslave.nio.impl;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;





public class ChannelAdapter implements ByteChannel {
    private final SelectionKey channelKey;
    private final ByteChannel  channel;
    private int   interest;

    private final Lock      lock;
    private final Condition done;



    public ChannelAdapter(SelectionKey key) {
        channelKey = key;
        channel  = (ByteChannel) key.channel();
        interest = 0;

        lock = new ReentrantLock();
        done = lock.newCondition();
    }


    @Override
    public int read(ByteBuffer buffer) throws IOException {
        // ожидаем уведомления о возможности чтения
        await(SelectionKey.OP_READ);
        // и читаем
        return channel.read(buffer);
    }


    @Override
    public int write(ByteBuffer buffer) throws IOException {
        // ожидаем уведомления о возможности записи
        await(SelectionKey.OP_WRITE);
        // и пишем
        return channel.write(buffer);
    }


    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }


    @Override
    public void close() throws IOException {
        channelKey.cancel();
        channel.close();
    }


    public void signal(int option) throws IOException {
        lock();
        try {
            // проверяем флаг интереса
            if (interest == option) {
                // уведомляем ожидающий поток
                done.signalAll();
            } else {
                // сбрасываем флаг
                channelKey.interestOps(interest);
            }
        } finally {
            lock.unlock();
        }
    }


    private void lock() throws IOException {
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }


    private void await(int option) throws IOException {
        lock();
        try {
            // выставляем флаг интереса
            channelKey.interestOps(interest = option);

            // ожидаем уведомления
            done.await();

            // сбрасываем флаг интереса
            channelKey.interestOps(interest = 0);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();

        } finally {
            lock.unlock();
        }
    }

}
