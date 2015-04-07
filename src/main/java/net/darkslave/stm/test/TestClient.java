package net.darkslave.stm.test;

import java.util.concurrent.ThreadLocalRandom;
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
            if (args.length == 0)
                args = new String[] { "config.cfg" };

            __main(args);

        } catch (Exception e) {
            logger.catching(e);
        }
    }


    private static void __main(String[] args) throws Exception {
        ClientConfig config = ClientConfig.create(args[0]);

        try (Client client = config.getClientFactory().create(config)) {
            byte[] source = new byte[config.getPacketsMax()];

            AtomicLong count = new AtomicLong(0);
            AtomicLong total = new AtomicLong(0);

            client.setHandler(() -> {
                int size = ThreadLocalRandom.current().nextInt(config.getPacketsMin(), config.getPacketsMax() + 1);

                count.incrementAndGet();
                total.addAndGet(size);

                return new Message(source, size);
            });

            client.start();


            long countPrev = 0;
            long totalPrev = 0;
            long checkPrev = System.currentTimeMillis();

            while (true) {
                long countNow   = count.get();
                long countDelta = countNow - countPrev;

                long totalNow   = total.get();
                long totalDelta = totalNow - totalPrev;

                long checkNow   = System.currentTimeMillis();
                long checkDelta = checkNow - checkPrev;

                logger.info(String.format(
                        "total: %d msg, thrwpt: %.2f msg/sec  %.2f kb/sec",
                        countNow,
                        checkDelta > 0 ? 1000.0 * countDelta / checkDelta : 0,
                        checkDelta > 0 ? 1000.0 * totalDelta / checkDelta / 1024.0 : 0
                    ));

                countPrev = countNow;
                totalPrev = totalNow;
                checkPrev = checkNow;

                if (!client.started())
                    break;

                Thread.sleep(1000);
            }

            logger.info("done");
        }

    }

}
