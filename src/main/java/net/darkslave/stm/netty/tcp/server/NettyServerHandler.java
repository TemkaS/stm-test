package net.darkslave.stm.netty.tcp.server;



import net.darkslave.stm.core.Message;
import net.darkslave.stm.core.MessageAcceptor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;





public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final MessageAcceptor handler;


    public NettyServerHandler(MessageAcceptor handler) {
        this.handler = handler;
    }


    @Override
    public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
        ByteBuf buffer = (ByteBuf) object;

        try {
            int length = buffer.readableBytes();

            byte[] temp = new byte[length];
            buffer.readBytes(temp);

            Message messg = Message.read(temp, 0, length);
            handler.accept(messg);

        } finally {
            buffer.release();
        }
    }

}
