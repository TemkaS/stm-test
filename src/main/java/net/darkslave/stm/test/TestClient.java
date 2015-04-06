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

        try (Client client = config.getClientFactory().create(config)) {
            AtomicLong count = new AtomicLong(0);

            client.setHandler(() -> {
                long index = count.incrementAndGet();
                return new Message("packet-" + index, index);
            });

            client.start();


            long countPrev = 0;
            long checkPrev = System.currentTimeMillis();

            while (true) {
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

                if (!client.started())
                    break;

                Thread.sleep(1000);
            }

            logger.info("done");
        }

    }

}
