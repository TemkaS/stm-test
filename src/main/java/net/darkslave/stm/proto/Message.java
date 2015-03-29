package net.darkslave.stm.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;




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


    public static byte[] encode(Message source) throws IOException {
        return encode(source.getParam(), source.getValue());
    }


    public static byte[] encode(String param, double value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        DataOutput dos = new DataOutputStream(out);

        dos.writeShort(MARKER);
        dos.writeUTF(param);
        dos.writeDouble(value);

        return out.toByteArray();
    }


    public static Message decode(byte[] source) throws IOException {
        ByteArrayInputStream inp = new ByteArrayInputStream(source);
        DataInput dis = new DataInputStream(inp);

        short marker = dis.readShort();
        if (marker != MARKER)
            throw new IOException("Invalid Message packet");

        String param = dis.readUTF();
        double value = dis.readDouble();

        return new Message(param, value);
    }




}
