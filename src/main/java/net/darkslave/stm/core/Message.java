package net.darkslave.stm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;





public class Message {
    private final String param;
    private final double value;


    public Message(String param, double value) {
        this.param = param;
        this.value = value;
    }


    public String getParam() {
        return param;
    }


    public double getValue() {
        return value;
    }


    private static short MARKER = 0x7733;



    public static Message readFrom(InputStream stream) throws IOException {
        try {
            DataInput dis = new DataInputStream(stream);

            short marker = dis.readShort();
            if (marker != MARKER)
                throw new IOException("Invalid Message packet");

            String param = dis.readUTF();
            double value = dis.readDouble();

            return new Message(param, value);

        } catch (EOFException e) {
            return null;
        }
    }


    public static void writeTo(Message source, OutputStream stream) throws IOException {
        DataOutput dos = new DataOutputStream(stream);

        dos.writeShort(MARKER);

        dos.writeUTF(source.getParam());
        dos.writeDouble(source.getValue());
    }



    public static Message read(byte[] source, int offset, int length) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(source, offset, length);
        return readFrom(stream);
    }



    public static byte[] write(Message source) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        writeTo(source, stream);
        return stream.toByteArray();
    }


}
