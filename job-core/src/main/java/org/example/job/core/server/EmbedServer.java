package org.example.job.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.example.job.core.biz.ExecutorBiz;
import org.example.job.core.biz.impl.ExecutorBizImpl;
import org.example.job.core.thread.ExecutorRegistryThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class EmbedServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbedServer.class);
    private ExecutorBiz executorBiz;
    private Thread thread;

    public void start(String address, int port, String appname, String accessToken) {
        ThreadPoolExecutor bizThreadPool = new ThreadPoolExecutor(
                0,
                200,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                r -> new Thread(r, "rpc, EmbedServer bizThreadPool-" + r.hashCode()),
                (r, executor) -> {
                    throw new RuntimeException("job, EmbedServer bizThreadPool is EXHAUSTED!");
                });

        executorBiz = new ExecutorBizImpl();
        thread = new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))  // beat 3N, close if idle
                                        .addLast(new HttpServerCodec())//netty针对http编解码的处理类，但是这些只能处理像http get的请求
                                        .addLast(new HttpObjectAggregator(5 * 1024 * 1024))//HttpObjectAggregator这个netty的处理器就是为了解决这个问题而来的.它把HttpMessage和HttpContent聚合成为一个FullHttpRquest或者FullHttpRsponse
                                        .addLast(new EmbedHttpServerHandler(executorBiz, accessToken, bizThreadPool));
                            }
                        }).childOption(ChannelOption.SO_KEEPALIVE, true);
                // bind
                ChannelFuture future = bootstrap.bind(port).sync();
                logger.info(">>>>>>>>>>> job remoting server start success, nettype = {}, port = {}", EmbedServer.class, port);
                startRegistry(appname,address);
                // wait util stop
                future.channel().closeFuture().sync();

            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    logger.info(">>>>>>>>>>> job remoting server stop.");
                } else {
                    logger.error(">>>>>>>>>>> job remoting server error.", e);
                }
            } finally {
                // stop
                try {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    public void stop() throws Exception {
        // destroy server thread
        if (thread!=null && thread.isAlive()) {
            thread.interrupt();
        }

        // stop registry
        stopRegistry();
        logger.info(">>>>>>>>>>> xxl-job remoting server destroy success.");
    }

    private void startRegistry(final String appname,final String address) {
        ExecutorRegistryThread.getInstance().start(appname, address);
    }

    private void stopRegistry() {
        // stop registry
        ExecutorRegistryThread.getInstance().stop();
    }

    public class EmbedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        public EmbedHttpServerHandler(ExecutorBiz executorBiz, String accessToken, ThreadPoolExecutor bizThreadPool) {
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        }
    }
}
