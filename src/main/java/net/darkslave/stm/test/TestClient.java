package net.darkslave.stm.test;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.core.Message;




public class TestClient {
    private static final Logger logger = LogManager.getLogger(TestClient.class);


    public static void main(String[] args) {
        try {
            __main(args);
        } catch (Exception e) {
            logger.catching(e);
        }
    }


    private static void __main(String[] args) throws Exception {
        if (args.length == 0)
            throw new IllegalArgumentException("Config file is not defined");

        ClientConfig config = ClientConfig.create(args[0]);

        AtomicLong latch = new AtomicLong(config.getThreadCount());
        AtomicLong count = new AtomicLong(0);

        for (int i = 0; i < config.getThreadCount(); i++) {
            final String name = "client-" + i;
            Thread thread = new Thread(() -> {
                try (Client client = config.getClientFactory().create(config)) {
                    client.init();

                    int index = config.getMessages();

                    while (--index >= 0 && !Thread.interrupted()) {
                        client.send(new Message(name, index));
                        count.incrementAndGet();
                    }

                } catch (Exception e) {
                    logger.catching(e);

                } finally {
                    latch.decrementAndGet();
                }
            });
            thread.setDaemon(true);
            thread.start();
        }


        long countPrev = 0;
        long checkPrev = System.currentTimeMillis();

        while (true) {
            long threadAlive = latch.get();

            long countNow   = count.get();
            long countDelta = countNow - countPrev;

            long checkNow   = System.currentTimeMillis();
            long checkDelta = checkNow - checkPrev;

            logger.info(String.format(
                    "total: %d msg, thrwpt: %.2f msg/sec",
                    countNow,
                    checkDelta > 0 ? 1000.0 * countDelta / checkDelta : 0
                    ));

            countPrev = countNow;
            checkPrev = checkNow;

            if (threadAlive == 0)
                break;

            Thread.sleep(1000);
        }

        logger.info("done");
    }

}
