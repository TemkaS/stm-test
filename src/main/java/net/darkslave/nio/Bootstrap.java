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
    public static final ErrorHandler    DEFAULT_ERROR_HANDLER    = (e) -> e.printStackTrace();
    public static final RequestAcceptor DEFAULT_REQUEST_ACCEPTOR = (a) -> true;

    private InetSocketAddress address;

    private ExecutorService   bossThreadPool;
    private ExecutorService   workThreadPool;

    private RequestAcceptor   requestAcceptor;
    private RequestHandler    requestHandler;
    private ErrorHandler      errorHandler;

    private int pendingCount  = 1000;
    private int selectorDelay = 10;


    public Bootstrap() {
        this.bossThreadPool  = Executors.newSingleThreadExecutor();
        this.workThreadPool  = Executors.newWorkStealingPool();
        this.errorHandler    = DEFAULT_ERROR_HANDLER;
        this.requestAcceptor = DEFAULT_REQUEST_ACCEPTOR;
    }


    InetSocketAddress getAddress() {
        return address;
    }


    /**
     * Установка порта
     */
    public Bootstrap setAddress(int port) {
        this.address = new InetSocketAddress(port);
        return this;
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


    ErrorHandler getErrorHandler() {
        return errorHandler;
    }


    /**
     * Установка логирования ошибок
     */
    public Bootstrap setErrorHandler(ErrorHandler value) {
        if (value == null)
            throw new IllegalArgumentException("Parameter can't be null");
        this.errorHandler = value;
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


    public Server create() throws IOException {
        return new ServerImpl(this);
    }


}
