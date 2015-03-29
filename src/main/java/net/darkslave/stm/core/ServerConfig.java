package net.darkslave.stm.core;

import java.net.InetAddress;
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


    public static ServerConfig create(PropertyPresenter prop) throws ConfigException {
        try {
            ServerConfig result = new ServerConfig();

            result.targetHost = InetAddress.getByName(prop.getString("target.host"));

            result.targetPort = Port.parse(prop.getString("target.port"));

            Class<?> factoryClazz = Class.forName(prop.getString("server.factory"));
            result.serverFactory = (ServerFactory) factoryClazz.newInstance();

            result.workingTime = prop.getLong("working.time", -1L);


            return result;

        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }


}
