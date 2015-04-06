package net.darkslave.stm.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import net.darkslave.prop.PropertyFilePresenter;
import net.darkslave.prop.PropertyPresenter;





public class ServerConfig {
    private InetAddress targetHost;
    private Port targetPort;

    private ServerFactory serverFactory;

    private long workingTime;


    public InetAddress getTargetHost() {
        return targetHost;
    }


    public Port getTargetPort() {
        return targetPort;
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

            result.targetHost = InetAddress.getByName(prop.getString("target.host"));
            result.targetPort = Port.parse(prop.getString("target.port"));
            result.workingTime = prop.getLong("working.time", -1L);

            Class<?> serverClass = Class.forName(prop.getString("server.impl"));
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
