package net.darkslave.stm.server.temkas.udp;

import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.Message;
import net.darkslave.stm.proto.MessageHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
        private final int port;
        private final MessageHandler handler;
        private volatile boolean active = true;


        public Worker(int port, MessageHandler handler) throws IOException {
            this.port    = port;
            this.handler = handler;
        }


        @Override
        public void run() {
            try (
                    DatagramChannel channel = DatagramChannel.open();
                    Selector selector = Selector.open()
            ) {
                InetSocketAddress isa = new InetSocketAddress(port);
                channel.socket().bind(isa);
                channel.configureBlocking(false);
                SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
                clientKey.attach(new Connection());

                while (!Thread.interrupted() && active) {

                    try {
                        selector.select();
                        Iterator selectedKeys = selector.selectedKeys().iterator();
                        while (selectedKeys.hasNext()) {
                            try {
                                SelectionKey key = (SelectionKey) selectedKeys.next();
                                selectedKeys.remove();

                                if (!key.isValid()) {
                                    continue;
                                }

                                if (key.isReadable()) {
                                    read(key);
                                    //тут тока Read подразумеваем поэтому не ставим операцию WRITE
                                    //и в условие Writable не попадем
                                    //key.interestOps(SelectionKey.OP_WRITE);
                                } else if (key.isWritable()) {
                                    write(key);
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            } catch (IOException e) {
                                logger.error("drop... " + (e.getMessage() != null ? e.getMessage() : ""));
                            }
                        }
                    } catch (IOException e) {
                        logger.error("drop... " + (e.getMessage() != null ? e.getMessage() : ""));
                    }
                }

            } catch (IOException e) {
                logger.error(e);
            }
        }


        @Override
        public void close() throws IOException {
            active = false;
        }

        private void read(SelectionKey key) throws IOException {
            DatagramChannel chan = (DatagramChannel)key.channel();
            Connection con = (Connection)key.attachment();
            con.sa = chan.receive(con.reqt);
            Message messg = Message.decode(con.reqt);
            handler.accept(messg);
        }

        private void write(SelectionKey key) throws IOException {
            DatagramChannel chan = (DatagramChannel)key.channel();
            Connection con = (Connection)key.attachment();
            chan.send(con.resp, con.sa);
        }
    }

    private static final int BUF_SZ = 4096;

    private static class Connection {
        private ByteBuffer reqt;
        private ByteBuffer resp;
        private SocketAddress sa;

        public Connection() {
            reqt = ByteBuffer.allocate(BUF_SZ);
        }
    }
}
