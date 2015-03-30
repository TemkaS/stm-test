package net.darkslave.stm;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.prop.PropertyFilePresenter;
import net.darkslave.stm.core.Client;
import net.darkslave.stm.core.ClientConfig;
import net.darkslave.stm.proto.Message;




public class TestClient {
    private static final Logger logger = LogManager.getLogger(TestClient.class);


    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Config file is not defined");
            return;
        }

        try {
            ClientConfig config = ClientConfig.create(new PropertyFilePresenter(Paths.get(args[0]), StandardCharsets.UTF_8));
            AtomicInteger latch = new AtomicInteger(config.getThreadCount());
            AtomicInteger count = new AtomicInteger(0);

            for (int i = 0; i < config.getThreadCount(); i++) {
                String name = "client-" + i;
                Thread thread = new Thread(() -> {
                    try (
                        Client client = config.getClientFactory().create(config);
                    ) {
                        client.init();

                        int index = config.getMessages();

                        while (!Thread.interrupted() && --index >= 0) {
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


            int  countPrev = 0;
            long checkPrev = System.currentTimeMillis();

            while (true) {
                int  threadAlive = latch.get();

                int  countNow   = count.get();
                int  countDelta = countNow - countPrev;

                long checkNow   = System.currentTimeMillis();
                long checkDelta = checkNow - checkPrev;

                logger.info(String.format(
                        "total: %d msg, thrwpt: %.2f msg/sec",
                        countNow,
                        checkDelta > 0 ? 1000.0 * countDelta / checkDelta : 0
                    ));

                countPrev = countNow;
                checkPrev = checkNow;

                if (threadAlive > 0) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }

            logger.info("done");

        } catch (Exception e) {
            logger.catching(e);
        }

    }


}
