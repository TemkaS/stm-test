package net.darkslave.stm.core;

import java.net.InetAddress;
import net.darkslave.prop.PropertyPresenter;





public class ClientConfig {
    private InetAddress targetHost;
    private Port targetPort;

    private ClientFactory clientFactory;

    private int threadCount;
    private int messages;


    public InetAddress getTargetHost() {
        return targetHost;
    }


    public Port getTargetPort() {
        return targetPort;
    }


    public ClientFactory getClientFactory() {
        return clientFactory;
    }


    public int getThreadCount() {
        return threadCount;
    }


    public int getMessages() {
        return messages;
    }


    public static ClientConfig create(PropertyPresenter prop) throws ConfigException {
        try {
            ClientConfig result = new ClientConfig();

            result.targetHost = InetAddress.getByName(prop.getString("target.host"));

            result.targetPort = Port.parse(prop.getString("target.port"));

            Class<?> factoryClazz = Class.forName(prop.getString("client.factory"));
            result.clientFactory = (ClientFactory) factoryClazz.newInstance();

            result.threadCount = prop.getInteger("thread.count", 1);

            result.messages = prop.getInteger("messages", 1);


            return result;

        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }


}
