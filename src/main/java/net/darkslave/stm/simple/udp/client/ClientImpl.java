package net.darkslave.stm.simple.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageProducer;





public class ClientImpl implements Client {
    private static final Logger logger = LogManager.getLogger(ClientImpl.class);

    private final ClientConfig config;
    private final AtomicInteger latch;
    private MessageProducer handler;

    private final ExecutorService bossThreadPool = Executors.newCachedThreadPool();


    public ClientImpl(ClientConfig config) {
        this.config = config;
        this.latch  = new AtomicInteger(config.getThreadsCount());
    }


    @Override
    public void setHandler(MessageProducer handler) {
        this.handler = handler;
    }


    @Override
    public void start() throws IOException {
        for (int i = 0; i < config.getThreadsCount(); i++) {
            Worker worker = new Worker();
            bossThreadPool.execute(worker);
        }
    }


    @Override
    public void close() throws IOException {
        bossThreadPool.shutdownNow();
    }


    @Override
    public boolean started() {
        return latch.get() != 0;
    }


    public class Worker implements Runnable {

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                int count = config.getMessageCount();

                while (--count >= 0 && !Thread.interrupted()) {
                    Message messg = handler.produce();

                    byte[] buffer = Message.write(messg);

                    DatagramPacket packet = new DatagramPacket(
                            buffer,
                            buffer.length,
                            config.getServerHost(),
                            config.getServerPort().get()
                            );

                    socket.send(packet);
                }

            } catch (Exception e) {
                logger.catching(e);

            } finally {
                latch.decrementAndGet();
            }
        }

    }

}
