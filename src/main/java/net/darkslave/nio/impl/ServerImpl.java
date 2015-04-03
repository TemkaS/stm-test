package net.darkslave.nio.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import net.darkslave.nio.Bootstrap;
import net.darkslave.nio.ErrorHandler;
import net.darkslave.nio.RequestAcceptor;
import net.darkslave.nio.RequestHandler;
import net.darkslave.nio.Server;



/**
 * Реализация TCP-сервера
 */
public class ServerImpl implements Runnable, Server {
    private final InetSocketAddress address;

    private final ExecutorService bossThreadPool;
    private final ExecutorService workThreadPool;

    private final RequestAcceptor requestAcceptor;
    private final RequestHandler  requestHandler;
    private final ErrorHandler    errorHandler;

    private final int pendingCount;
    private final int selectorDelay;

    private volatile boolean  started = false;



    public ServerImpl(Bootstrap config) throws IOException {
        if (config.getAddress() == null)
            throw new IllegalStateException("Address is not defined");

        if (config.getBossThreadPool() == null)
            throw new IllegalStateException("BossThreadPool is not defined");

        if (config.getWorkThreadPool() == null)
            throw new IllegalStateException("WorkThreadPool is not defined");

        if (config.getRequestHandler() == null)
            throw new IllegalStateException("RequestHandler is not defined");

        address  = config.getAddress();

        bossThreadPool = config.getBossThreadPool();
        workThreadPool = config.getWorkThreadPool();

        requestAcceptor = config.getRequestAcceptor();
        requestHandler  = config.getRequestHandler();

        errorHandler  = config.getErrorHandler();
        pendingCount  = config.getPendingCount();
        selectorDelay = config.getSelectorDelay();
    }



    @Override
    public void run() {
        try (
            ServerSocketChannel channel = ServerSocketChannel.open();
            Selector selector = Selector.open();
        ) {
            channel.configureBlocking(false);
            channel.bind(address, pendingCount);
            channel.register(selector, SelectionKey.OP_ACCEPT);

            started = true;

            while (started && !Thread.interrupted()) {
                // проверяем активные ключи
                int selected = selector.selectNow();

                // если нет ключей, подождем немного
                if (selected == 0) {
                    if (selectorDelay > 0)
                        Thread.sleep(selectorDelay);
                    continue;
                }

                // пробегаемся по активным ключам
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                for ( ; it.hasNext(); it.remove()) {
                    SelectionKey key = it.next();

                    if (!key.isValid())
                        continue;

                    try {
                        int option = key.readyOps();

                        if ((option & SelectionKey.OP_ACCEPT) != 0) {
                            accept(key);
                        } else
                        if ((option & SelectionKey.OP_READ) != 0) {
                            signal(key, SelectionKey.OP_READ);
                        } else
                        if ((option & SelectionKey.OP_WRITE) != 0) {
                            signal(key, SelectionKey.OP_WRITE);
                        }

                    } catch (CancelledKeyException e) {
                        // do nothing
                    }

                }

            }

        } catch (Exception e) {
            setLastError(e);

        } finally {
            started = false;
        }
    }


    /**
     * Создание нового соединения
     */
    private void accept(SelectionKey serverKey) {
        SelectionKey clientKey = null;
        try {
            // принимаем соединение
            SocketChannel channel = ((ServerSocketChannel) serverKey.channel()).accept();
            channel.configureBlocking(false);

            // регистрируем в селекторе
            clientKey = channel.register(serverKey.selector(), 0);

            // проверяем адрес клиента
            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();

            if (!requestAcceptor.accept(address)) {
                cancelKey(null, clientKey);
                return;
            }

            // создаем новую коннекцию запроса
            ChannelAction action = new ChannelAction(clientKey);
            clientKey.attach(action);

            // запускаем в отдельном потоке обработку запроса
            handle(clientKey, action);

        } catch (Exception e) {
            if (clientKey != null) {
                cancelKey(e, clientKey);
            } else {
                setLastError(e);
            }
        }
    }


    private void handle(SelectionKey clientKey, ChannelAction action) {
        InputStream  input  = new ChannelInputStream (action);
        OutputStream output = new ChannelOutputStream(action);

        workThreadPool.execute(() -> {
            Exception error = null;
            try {
                requestHandler.handle(input, output);
            } catch (Exception e) {
                error = e;
            } finally {
                cancelKey(error, clientKey);
            }
        });

    }


    /**
     * Сигнал о чтении / записи в канал
     */
    private void signal(SelectionKey clientKey, int option) {
        try {
            ChannelAction action = (ChannelAction) clientKey.attachment();
            action.signal(option);
        } catch (IOException e) {
            cancelKey(e, clientKey);
        }
    }


    /**
     *  Сброс ключа, закрытие канала и логирование ошибок
     */
    private void cancelKey(Exception result, SelectionKey clientKey) {
        clientKey.cancel();
        clientKey.attach(null);
        closeChannel(result, clientKey.channel());
    }


    /**
     *  Закрытие канала и логирование ошибок
     */
    private void closeChannel(Exception result, Channel channel) {
        try {
            channel.close();
        } catch (Exception e) {
            if (result != null) {
                result.addSuppressed(e);
            } else {
                result = e;
            }
        } finally {
            setLastError(result);
        }
    }


    /**
     * Логирование
     */
    private void setLastError(Exception value) {
        if (value != null) {
            errorHandler.handle(value);
        }
    }


    /**
     * Проверка флага запуска
     */
    @Override
    public boolean started() {
        return started;
    }


    /**
     * Запуск сервера
     */
    @Override
    public void start() {
        if (!started) {
            bossThreadPool.execute(this);
        }
    }


    /**
     * Остановка сервера
     */
    @Override
    public void stop() {
        started = false;
    }

}
