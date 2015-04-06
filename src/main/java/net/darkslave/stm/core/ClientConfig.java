package net.darkslave.stm.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import net.darkslave.prop.PropertyFilePresenter;
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


    public static ClientConfig create(String filename) throws ConfigException {
        try {
            PropertyPresenter prop = new PropertyFilePresenter(Paths.get(filename), StandardCharsets.UTF_8);
            ClientConfig result = new ClientConfig();

            result.targetHost = InetAddress.getByName(prop.getString("target.host"));
            result.targetPort = Port.parse(prop.getString("target.port"));
            result.threadCount = prop.getInteger("thread.count", 1);
            result.messages = prop.getInteger("messages", 1);

            Class<?> clientClass = Class.forName(prop.getString("client.impl"));
            Constructor<?> clientConst = clientClass.getConstructor(ClientConfig.class);

            result.clientFactory = (ClientFactory) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { ClientFactory.class },
                (Object proxy, Method method, Object[] args) -> {
                    if (method.getName().equals("create"))
                        return clientConst.newInstance(args[0]);
                    return null;
                }
            );

            return result;

        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }


}
