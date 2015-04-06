package net.darkslave.stm.test;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;




public class TestServer {
    private static final Logger logger = LogManager.getLogger(TestServer.class);


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

        ServerConfig config = ServerConfig.create(args[0]);

        try (Server server = config.getServerFactory().create(config)) {
            AtomicLong count = new AtomicLong(0);
            AtomicLong total = new AtomicLong(0);

            server.setHandler((Message messg) -> {
                count.incrementAndGet();
                total.addAndGet(messg.getSize());
            });

            server.start();


            long countPrev = 0;
            long totalPrev = 0;
            long checkPrev = System.currentTimeMillis();
            long expires;

            if (config.getWorkingTime() > 0) {
                expires = System.currentTimeMillis() + config.getWorkingTime();
            } else {
                expires = Long.MAX_VALUE;
            }

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

                if (System.currentTimeMillis() >= expires)
                    break;

                Thread.sleep(1000);
            }

            logger.info("done");
        }

    }

}
