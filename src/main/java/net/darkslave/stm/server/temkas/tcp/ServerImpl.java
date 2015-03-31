package net.darkslave.stm.server.temkas.tcp;

import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.Message;
import net.darkslave.stm.proto.MessageHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Created by TemkaS on 31.03.2015.
 */
public class ServerImpl implements Server {

    private static final Logger logger = LogManager.getLogger(net.darkslave.stm.server.simple.udp.ServerImpl.class);

    private final ServerConfig config;
    private MessageHandler handler;

    private final List<Worker> active;

    public ServerImpl(ServerConfig config) throws IOException {
        this.config = config;
        this.active = new LinkedList<>();
    }

    @Override
    public void start() throws IOException {
        for (Integer port : config.getTargetPort()) {
            Worker worker = new Worker(port, handler);

            Thread thread = new Thread(worker);
            thread.setDaemon(true);
            thread.start();

            active.add(worker);
        }
    }

    @Override
    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void close() throws IOException {
        for (Worker worker : active) {
            worker.close();
        }
    }

    private static class Worker implements Runnable, Closeable {

        private final MessageHandler handler;
        private volatile boolean active = true;
        private final Selector selector;


        public Worker(int port, MessageHandler handler) throws IOException {
            this.handler = handler;

            final ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.configureBlocking(false);
            selector = Selector.open();
            ssc.register(selector, ssc.validOps());
        }


        @Override
        public void run() {
            while (!Thread.interrupted() && active) {

                try {
                    while (selector.select() > -1) {
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (key.isValid()) {
                                try {
                                    if (key.isAcceptable()) {
                                        accept(key);
                                    } else if (key.isConnectable()) {
                                        connect(key);
                                    } else if (key.isReadable()) {
                                        read(key);
                                    } else if (key.isWritable()) {

                                    }
                                } catch (Exception e) {
                                    logger.error("not valid key or drop packet...", e);
                                    close(key);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("selector error...", e);
                }

            }

        }


        @Override
        public void close() throws IOException {
            selector.close();

            active = false;
        }

        private void close(SelectionKey key) throws IOException {
            SocketChannel sc = (SocketChannel) key.channel();
            key.cancel();
            if (sc.isConnected()) {
                sc.close();
            }
        }

        private void read(SelectionKey key) throws IOException {
            SocketChannel chan = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(512);
            long bytes = chan.read(buffer);
            if (bytes < 1) {
                close(key);
            } else {
                Message messg = Message.decode(buffer);
                handler.accept(messg);
            }
        }

        private void accept(SelectionKey key) throws IOException {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = ssc.accept();
            socketChannel.configureBlocking(false);
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            socketChannel.register(selector, SelectionKey.OP_READ);
        }

        private void connect(SelectionKey key) throws IOException {
            ((SocketChannel) key.channel()).finishConnect();
            key.interestOps(0);
        }
    }
}