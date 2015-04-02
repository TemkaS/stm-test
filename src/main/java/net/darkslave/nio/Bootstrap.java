package net.darkslave.nio;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;




/**
 *  Класс настроек и запуска сервера
 */
public class Bootstrap {
    public static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = (e) -> e.printStackTrace();
    public static final RequestAcceptor DEFAULT_REQUEST_ACCEPTOR   = (a) -> true;


    private InetSocketAddress address;

    private ExecutorService   bossThreadPool;
    private ExecutorService   workThreadPool;

    private ExceptionHandler  exceptionHandler;

    private RequestAcceptor   requestAcceptor;
    private RequestHandler    requestHandler;

    private int pendingCount  = 0;

    private int selectorDelay = 10;


    public Bootstrap() {
        this.bossThreadPool = Executors.newSingleThreadExecutor();
        this.workThreadPool = Executors.newWorkStealingPool();
        this.exceptionHandler = DEFAULT_EXCEPTION_HANDLER;
        this.requestAcceptor  = DEFAULT_REQUEST_ACCEPTOR;
    }


    InetSocketAddress getAddress() {
        return address;
    }


    /**
     * Установка адреса и порта
     */
    public Bootstrap setAddress(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        return this;
    }


    /**
     * Установка адреса и порта
     */
    public Bootstrap setAddress(InetAddress host, int port) {
        this.address = new InetSocketAddress(host, port);
        return this;
    }


    /**
     * Установка адреса и порта
     */
    public Bootstrap setAddress(InetSocketAddress value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.address = value;
        return this;
    }


    ExecutorService getBossThreadPool() {
        return bossThreadPool;
    }


    /**
     * Установка пула для потока селектора
     */
    public Bootstrap setBossThreadPool(ExecutorService value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.bossThreadPool = value;
        return this;
    }


    ExecutorService getWorkThreadPool() {
        return workThreadPool;
    }


    /**
     * Установка пула для потоков обработчиков пакетов
     */
    public Bootstrap setWorkThreadPool(ExecutorService value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.workThreadPool = value;
        return this;
    }


    ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }


    /**
     * Установка логирования ошибок
     */
    public Bootstrap setExceptionHandler(ExceptionHandler value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.exceptionHandler = value;
        return this;
    }


    RequestAcceptor getRequestAcceptor() {
        return requestAcceptor;
    }


    /**
     * Установка валидатора запросов
     */
    public Bootstrap setRequestAcceptor(RequestAcceptor value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.requestAcceptor = value;
        return this;
    }


    RequestHandler getRequestHandler() {
        return requestHandler;
    }


    /**
     * Установка обработчика запросов
     */
    public Bootstrap setRequestHandler(RequestHandler value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.requestHandler = value;
        return this;
    }


    int getPendingCount() {
        return pendingCount;
    }


    /**
     * Установка размера очереди ожидания
     */
    public Bootstrap setPendingCount(int value) {
        this.pendingCount = value;
        return this;
    }


    int getSelectorDelay() {
        return selectorDelay;
    }


    /**
     * Установка времени простоя селектора
     */
    public Bootstrap setSelectorDelay(int value) {
        this.selectorDelay = value;
        return this;
    }


    public Server start() throws IOException {
        if (address == null)
            throw new IllegalStateException("Address is not defined");

        if (requestHandler == null)
            throw new IllegalStateException("RequestHandler is not defined");

        // инстанцируем сервер
        ServerImpl thread = new ServerImpl(this);

        // и запускаем селектор в пуле
        bossThreadPool.execute(thread);

        return thread;
    }

}
