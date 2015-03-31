package net.darkslave.stm.server.netty.tcp;



import net.darkslave.stm.proto.Message;
import net.darkslave.stm.proto.MessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;





public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final MessageHandler handler;


    public NettyServerHandler(MessageHandler handler) {
        this.handler = handler;
    }


    @Override
    public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
        ByteBuf buffer = (ByteBuf) object;

        try {
            int length = buffer.readableBytes();

            byte[] temp = new byte[length];
            buffer.readBytes(temp);

            Message messg = Message.decode(temp, length);
            handler.accept(messg);

        } finally {
            buffer.release();
        }
    }

}
