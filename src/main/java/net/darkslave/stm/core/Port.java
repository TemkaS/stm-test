package net.darkslave.stm.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import net.darkslave.util.StringParser;




public abstract class Port {


    public abstract int get();


    public abstract IntStream stream();



    public static Port parse(String source) {
        if (!source.contains("..")) {
            Integer port = StringParser.toInteger(source, null);

            if (port == null)
                throw new IllegalArgumentException("Port `" + source + "` is not valid");

            return new StaticPort(port);
        }

        String[] parts = source.split("\\s*[\\.]{2,}\\s*");
        if (parts.length != 2)
            throw new IllegalArgumentException("Port range `" + source + "` is not valid");

        Integer lower = StringParser.toInteger(parts[0], null);
        Integer upper = StringParser.toInteger(parts[1], null);

        if (lower == null || upper == null)
            throw new IllegalArgumentException("Port `" + source + "` is not valid");

        return new RangedPort(lower, upper);
    }



    private static class RangedPort extends Port {
        private final int lower;
        private final int delta;
        private final AtomicInteger counter;

        public RangedPort(int lower, int upper) {
            this.lower   = lower;
            this.delta   = upper - lower + 1;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public int get() {
            int d = counter.getAndIncrement() % delta;
            return d >= 0 ? lower + d : lower - d;
        }

        @Override
        public IntStream stream() {
            return IntStream.range(lower, lower + delta);
        }

    }



    private static class StaticPort extends Port {
        private final int port;

        public StaticPort(int port) {
            this.port = port;
        }

        @Override
        public int get() {
            return port;
        }

        @Override
        public IntStream stream() {
            return IntStream.of(port);
        }

    }


}
