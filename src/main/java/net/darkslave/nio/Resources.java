package net.darkslave.nio;

import java.io.Closeable;
import java.io.IOException;



/**
 *  Утилита управления ресурсами
 */
public class Resources {


    /**
     * Закрыть перечисленные ресурсы
     *
     * @param source - ресурсы
     * @return исключение, если при закрытии произошла ошибка
     */
    public static IOException close(Closeable ... source) {
        return close(null, source);
    }


    /**
     * Закрыть перечисленные ресурсы
     *
     * @param result - результирующее исключение
     * @param source - ресурсы
     * @return исключение, если при закрытии произошла ошибка
     */
    public static IOException close(IOException result, Closeable ... source) {

        for (Closeable item : source) {
            try {
                item.close();
            } catch (IOException e) {
                if (result != null) {
                    result.addSuppressed(e);
                } else {
                    result = e;
                }
            }
        }

        return result;
    }


    private Resources() {}
}
