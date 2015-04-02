package net.darkslave.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;




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



    ServerImpl(Bootstrap config) throws IOException {
        if (config.getAddress() == null)
            throw new IllegalStateException("Address is not defined");

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
            channel.register(selector, channel.validOps());

            started = true;

            while (started && !Thread.interrupted()) {
                // проверяем активные ключи
                int selected = selector.selectNow();

                // еслиключей нет, ждем немного
                if (selected == 0) {
                    if (selectorDelay > 0)
                        Thread.sleep(selectorDelay);
                    continue;
                }

                // пробегаемся по активным ключам
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                for ( ; it.hasNext(); it.remove()) {
                    SelectionKey key = it.next();
                    if (!key.isValid()) {
                        // do nothing
                    } else
                    if (key.isAcceptable()) {
                        accept(key);
                    } else
                    if (key.isConnectable()) {
                        connect(key);
                    } else
                    if (key.isReadable()) {
                        read(key);
                    } else
                    if (key.isWritable()) {
                        write(key);
                    }
                }

            }

        } catch (Exception  e) {
            setLastError(e);

        } finally {
            started = false;
        }
    }


    /**
     * Создание нового соединения
     */
    private void accept(SelectionKey serverKey) {
        SocketChannel chan = null;
        try {
            // принимаем соединение
            chan = ((ServerSocketChannel) serverKey.channel()).accept();
            chan.configureBlocking(false);

            // регистрируем в селекторе
            chan.register(serverKey.selector(), SelectionKey.OP_READ);

        } catch (Exception e) {
            if (chan != null) {
                closeChannel(e, chan);
            } else {
                setLastError(e);
            }
        }
    }


    /**
     * Завершение подключения к клиенту
     */
    private void connect(SelectionKey clientKey) {
        try {
            SocketChannel chan = (SocketChannel) clientKey.channel();

            // завершаем соединение
            chan.finishConnect();

            // проверяем адрес клиента
            InetSocketAddress address = (InetSocketAddress) chan.getRemoteAddress();

            if (!requestAcceptor.accept(address)) {
                cancelKey(null, clientKey);
                return;
            }

            // создаем новую коннекцию запроса
            Connection conn = new Connection(clientKey);
            clientKey.attach(conn);

            // запускаем в отдельном потоке обработку запроса
            workThreadPool.execute(() -> {
                Exception error = null;
                try {
                    requestHandler.handle(conn.getInputStream(), conn.getOutputStream());
                } catch (Exception e) {
                    error = e;
                } finally {
                    cancelKey(error, clientKey);
                }
            });

        } catch (Exception e) {
            cancelKey(e, clientKey);
        }
    }


    private void read(SelectionKey clientKey) {
        try {
            SocketChannel chan = (SocketChannel) clientKey.channel();

            Connection conn = (Connection) clientKey.attachment();
            chan.read((ByteBuffer) null);

        } catch (Exception e) {
            cancelKey(e, clientKey);
        }
    }


    private void write(SelectionKey clientKey) {
        try {
            SocketChannel chan = (SocketChannel) clientKey.channel();

            Connection conn = (Connection) clientKey.attachment();
            chan.write((ByteBuffer) null);

        } catch (Exception e) {
            cancelKey(e, clientKey);
        }
    }


    /**
     *  Сброс ключа, закрытие канала и логирование ошибок
     */
    private void cancelKey(Exception result, SelectionKey clientKey) {
        clientKey.cancel();
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
