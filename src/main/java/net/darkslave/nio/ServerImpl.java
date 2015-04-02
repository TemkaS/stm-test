package net.darkslave.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;




public class ServerImpl implements Runnable, Server {
    private final Selector selector;
    private final ServerSocketChannel channel;

    private final InetSocketAddress address;
    private final ExecutorService   workThreadPool;
    private final ExceptionHandler  exceptionHandler;
    private final RequestAcceptor   requestAcceptor;
    private final RequestHandler    requestHandler;
    private final int selectorDelay;

    private volatile boolean started = false;


    public ServerImpl(Bootstrap config) throws IOException {
        address = config.getAddress();
        channel = ServerSocketChannel.open();

        channel.configureBlocking(false);
        channel.bind(address, config.getPendingCount());

        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw Resources.close(e, channel);
        }

        channel.register(selector, channel.validOps());

        workThreadPool   = config.getWorkThreadPool();
        exceptionHandler = config.getExceptionHandler();
        requestAcceptor  = config.getRequestAcceptor();
        requestHandler   = config.getRequestHandler();
        selectorDelay    = config.getSelectorDelay();
    }


    @Override
    public void run() {
        try {
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

        } catch (IOException | InterruptedException  e) {
            // логируем ошибки
            exceptionHandler.handle(e);

        } finally {
            // закрываем ресурсы
            IOException closed = Resources.close(channel, selector);

            // логируем ошибки
            if (closed != null)
                exceptionHandler.handle(closed);

            started = false;
        }
    }


    @SuppressWarnings("resource")
    private void accept(SelectionKey serverKey) {
        SocketChannel chan = null;
        try {
            // принимаем соединение
            chan = ((ServerSocketChannel) serverKey.channel()).accept();
            chan.configureBlocking(false);

            // регистрируем в селекторе
            chan.register(serverKey.selector(), SelectionKey.OP_READ);

        } catch (IOException e) {
            // если канал успели открыть, закрываем
            if (chan != null)
                e = Resources.close(e, chan);
            // логируем ошибки
            exceptionHandler.handle(e);
        }
    }


    @SuppressWarnings("resource")
    private void connect(SelectionKey clientKey) {
        SocketChannel chan = (SocketChannel) clientKey.channel();
        try {
            // завершаем соединение
            chan.finishConnect();

            // проверяем адрес клиента
            InetSocketAddress address = (InetSocketAddress) chan.getRemoteAddress();

            if (!requestAcceptor.accept(address)) {
                clientKey.cancel();
                chan.close();
                return;
            }

            // создаем новую коннекцию запроса
            Connection conn = new Connection();
            clientKey.attach(conn);

            workThreadPool.execute(() -> {
                try {
                    // запускаем в отдельном потоке обработку запроса
                    requestHandler.handle(conn.getInputStream(), conn.getOutputStream());
                } catch (IOException e) {
                    // закрываем коннекцию и логируем ошибки
                    exceptionHandler.handle(Resources.close(e, conn));
                }
            });

        } catch (IOException e) {
            // закрываем канал и логируем ошибки
            exceptionHandler.handle(Resources.close(e, chan));
        }
    }


    @SuppressWarnings("resource")
    private void read(SelectionKey clientKey) {
        SocketChannel chan = (SocketChannel) clientKey.channel();
        try {
            Connection conn = (Connection) clientKey.attachment();
            chan.read((ByteBuffer) null);

        } catch (IOException e) {
            // закрываем канал и логируем ошибки
            exceptionHandler.handle(Resources.close(e, chan));
        }
    }


    @SuppressWarnings("resource")
    private void write(SelectionKey clientKey) {
        SocketChannel chan = (SocketChannel) clientKey.channel();
        try {
            Connection conn = (Connection) clientKey.attachment();
            chan.write((ByteBuffer) null);

        } catch (IOException e) {
            // закрываем канал и логируем ошибки
            exceptionHandler.handle(Resources.close(e, chan));
        }
    }


    @Override
    public InetSocketAddress address() {
        return address;
    }


    @Override
    public void stop() {
        started = false;
    }


    @Override
    public boolean started() {
        return started;
    }

}
