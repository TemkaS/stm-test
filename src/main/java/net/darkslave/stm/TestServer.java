package net.darkslave.stm;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.darkslave.prop.PropertyFilePresenter;
import net.darkslave.stm.core.Server;
import net.darkslave.stm.core.ServerConfig;
import net.darkslave.stm.proto.Message;




public class TestServer {
    private static final Logger logger = LogManager.getLogger(TestServer.class);


    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Config file is not defined");
            return;
        }

        try {
            ServerConfig config = ServerConfig.create(new PropertyFilePresenter(Paths.get(args[0]), StandardCharsets.UTF_8));
            AtomicInteger count = new AtomicInteger(0);

            Server server = config.getServerFactory().create(config);

            server.setHandler((Message messg) -> count.incrementAndGet());

            server.start();


            int  countPrev = 0;
            long checkPrev = System.currentTimeMillis();
            long expires;

            if (config.getWorkingTime() > 0) {
                expires = System.currentTimeMillis() + config.getWorkingTime();
            } else {
                expires = Long.MAX_VALUE;
            }

            while (true) {
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

                if (System.currentTimeMillis() < expires) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }


        } catch (Exception e) {
            logger.catching(e);
        }

    }


}
