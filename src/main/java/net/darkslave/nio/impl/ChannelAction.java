package net.darkslave.nio.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;




class ChannelAction {
    private final SelectionKey  channelKey;
    private final SocketChannel channel;
    private int   interest;

    private final Lock      lock;
    private final Condition done;


    public ChannelAction(SelectionKey key) {
        lock = new ReentrantLock();
        done = lock.newCondition();

        channelKey = key;
        channel  = (SocketChannel) key.channel();
        interest = 0;
    }


    public int read(byte[] target, int offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(target, offset, length);

        await(SelectionKey.OP_READ);

        // читаем из канала только доступные данные
        return channel.read(buffer);
    }


    public void write(byte[] source, int offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(source, offset, length);

        while (buffer.remaining() > 0) {
            // записываем в канал все данные
            await(SelectionKey.OP_WRITE);

            channel.write(buffer);
        }

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
