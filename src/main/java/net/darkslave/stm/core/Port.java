package net.darkslave.stm.core;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import net.darkslave.util.StringParser;




public abstract class Port implements Iterable<Integer> {


    public abstract int get();


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

        if (lower == null || upper == null || lower > upper)
            throw new IllegalArgumentException("Port `" + source + "` is not valid");

        return new RangedPort(lower, upper);
    }



    private static class RangedPort extends Port {
        private final int lower;
        private final int delta;
        private final AtomicInteger index;

        public RangedPort(int lower, int upper) {
            this.lower = lower;
            this.delta = upper - lower + 1;
            this.index = new AtomicInteger(0);
        }

        @Override
        public int get() {
            int d = index.getAndIncrement() % delta;
            return d >= 0 ? lower + d : lower - d;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new RangedIntIterator(lower, lower + delta);
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
        public Iterator<Integer> iterator() {
            return new SingleIntIterator(port);
        }

    }



    private static class SingleIntIterator implements Iterator<Integer> {
        private final int value;
        private boolean hasNext;

        public SingleIntIterator(int value) {
            this.value   = value;
            this.hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Integer next() {
            if (!hasNext)
                throw new NoSuchElementException();
            hasNext = false;
            return value;
        }

    }

    private static class RangedIntIterator implements Iterator<Integer> {
        private final int limit;
        private int index;

        public RangedIntIterator(int lower, int limit) {
            this.index = lower;
            this.limit = limit;
        }

        @Override
        public boolean hasNext() {
            return index < limit;
        }

        @Override
        public Integer next() {
            if (index >= limit)
                throw new NoSuchElementException();
            return index++;
        }

    }


}
