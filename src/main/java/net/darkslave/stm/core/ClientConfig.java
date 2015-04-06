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
    private InetAddress serverHost;
    private Port serverPort;
    private Port clientPort;

    private ClientFactory clientFactory;
    private int messgCount;


    public InetAddress getServerHost() {
        return serverHost;
    }


    public Port getServerPort() {
        return serverPort;
    }


    public ClientFactory getClientFactory() {
        return clientFactory;
    }


    public int getMessgCount() {
        return messgCount;
    }


    public Port getClientPort() {
        return clientPort;
    }


    public static ClientConfig create(String filename) throws ConfigException {
        try {
            PropertyPresenter prop = new PropertyFilePresenter(Paths.get(filename), StandardCharsets.UTF_8);
            ClientConfig result = new ClientConfig();

            result.serverHost = InetAddress.getByName(prop.getString("server.host"));
            result.serverPort = Port.parse(prop.getString("server.port"));
            result.clientPort = Port.parse(prop.getString("client.port"));

            result.messgCount = prop.getInteger("messg.count", 1);

            Class<?> clientClass = Class.forName("net.darkslave.stm." + prop.getString("client.impl") + ".client.ClientImpl");
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
