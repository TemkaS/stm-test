package net.darkslave.stm.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import net.darkslave.prop.PropertyFilePresenter;
import net.darkslave.prop.PropertyPresenter;





public class ServerConfig {
    private Port serverPort;

    private ServerFactory serverFactory;

    private long workingTime;



    public Port getServerPort() {
        return serverPort;
    }


    public ServerFactory getServerFactory() {
        return serverFactory;
    }


    public long getWorkingTime() {
        return workingTime;
    }


    public static ServerConfig create(String filename) throws ConfigException {
        try {
            PropertyPresenter prop = new PropertyFilePresenter(Paths.get(filename), StandardCharsets.UTF_8);
            ServerConfig result = new ServerConfig();

            result.serverPort = Port.parse(prop.getString("server.port"));
            result.workingTime = prop.getLong("working.time", -1L);

            Class<?> serverClass = Class.forName("net.darkslave.stm." + prop.getString("server.impl") + ".server.ServerImpl");
            Constructor<?> serverConst = serverClass.getConstructor(ServerConfig.class);

            result.serverFactory = (ServerFactory) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { ServerFactory.class },
                (Object proxy, Method method, Object[] args) -> {
                    if (method.getName().equals("create"))
                        return serverConst.newInstance(args[0]);
                    return null;
                }
            );

            return result;

        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }


}
