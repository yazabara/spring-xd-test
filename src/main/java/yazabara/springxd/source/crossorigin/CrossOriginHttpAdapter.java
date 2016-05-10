package yazabara.springxd.source.crossorigin;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Yaroslav Zabara
 */
public class CrossOriginHttpAdapter extends MessageProducerSupport {

    private interface Constants {
        /**
         * Default max number of threads for the default {@link Executor}
         */
        int DEFAULT_CORE_POOL_SIZE = 16;

        /**
         * Default max total size of queued events per channel for the default {@link Executor} (in bytes)
         */
        long DEFAULT_MAX_CHANNEL_MEMORY_SIZE = 1048576;

        /**
         * Default max total size of queued events for the whole pool for the default {@link Executor} (in bytes)
         */
        long DEFAULT_MAX_TOTAL_MEMORY_SIZE = 1048576;

        /**
         * Default max content length
         */
        int DEFAULT_MAX_CONTENT_LENGTH = 1048576;
    }

    private final int port;

    private volatile ServerBootstrap bootstrap;

    private volatile MessageConverter messageConverter;

    private volatile ExecutionHandler executionHandler;

    private volatile Executor executor = new OrderedMemoryAwareThreadPoolExecutor(Constants.DEFAULT_CORE_POOL_SIZE, Constants.DEFAULT_MAX_CHANNEL_MEMORY_SIZE, Constants.DEFAULT_MAX_TOTAL_MEMORY_SIZE);

    public CrossOriginHttpAdapter(int port) {
        this.port = port;
    }

    @Override
    protected void doStart() {
        this.messageConverter = new CrossOriginHttpConverter(getMessageBuilderFactory());
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        executionHandler = new ExecutionHandler(executor);
        bootstrap.setPipelineFactory(new PipelineFactory());
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.bind(new InetSocketAddress(this.port));
    }

//    @Override
//    protected void doStop() {
//        if (bootstrap != null) {
//            bootstrap.shutdown();
//        }
//    }

    private class PipelineFactory implements ChannelPipelineFactory {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = new DefaultChannelPipeline();

            LoggingHandler loggingHandler = new LoggingHandler();
            if (loggingHandler.getLogger().isDebugEnabled()) {
                pipeline.addLast("logger", loggingHandler);
            }
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("aggregator", new HttpChunkAggregator(Constants.DEFAULT_MAX_CONTENT_LENGTH));
            pipeline.addLast("errorHandler", new SimpleChannelHandler() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
                    if (e.getCause() instanceof TooLongFrameException) {
                        HttpResponse err = new DefaultHttpResponse(HTTP_1_1, REQUEST_ENTITY_TOO_LARGE);
                        e.getChannel().write(err).addListener(ChannelFutureListener.CLOSE);
                    }
                }
            });
            pipeline.addLast("encoder", new HttpResponseEncoder());
            pipeline.addLast("compressor", new HttpContentCompressor() {

                /*
                 * Required because the content compressor rejects a reply when no request
                 * has yet been processed. Even through the exception is caused further down
                 * the pipleline, writes go through all handlers.
                 */
                @Override
                public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                    Object msg = e.getMessage();
                    if (msg instanceof HttpResponse && ((HttpResponse) e.getMessage()).getStatus().equals(REQUEST_ENTITY_TOO_LARGE)) {
                        ctx.sendDownstream(e);
                    } else {
                        super.writeRequested(ctx, e);
                    }
                }

            });
            pipeline.addLast("executionHandler", executionHandler);
            pipeline.addLast("handler", new Handler(messageConverter));
            return pipeline;
        }
    }


    private class Handler extends SimpleChannelUpstreamHandler {

        private final MessageConverter messageConverter;

        public Handler(MessageConverter messageConverter) {
            Assert.notNull(messageConverter, "'messageConverter' must not be null");
            this.messageConverter = messageConverter;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            Assert.isInstanceOf(HttpRequest.class, e.getMessage());
            HttpRequest request = (HttpRequest) e.getMessage();
            if (logger.isDebugEnabled()) {
                logger.debug("Received HTTP request:\n" + indent(e.getMessage().toString()));
            }
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            Message<?> message = null;
            try {
                message = this.messageConverter.toMessage(request, null);
            } catch (MessageConversionException ex) {
                logger.error("Failed to convert message", ex);
                response = new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            }
            if (message != null) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending message: " + message);
                    }
                    sendMessage(message);
                } catch (Exception ex) {
                    logger.error("Error sending message", ex);
                    response = new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
                }
            }
            writeResponse(request, response, e.getChannel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            logger.error("Unhandled exception, closing channel", e.getCause());
            e.getChannel().close();
        }

        private void writeResponse(HttpRequest request, HttpResponse response, Channel channel) {
            boolean keepAlive = HttpHeaders.isKeepAlive(request);
            if (keepAlive) {
                response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
                response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                response.setHeader("Access-Control-Allow-Origin", "*");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Sending HTTP response:\n" + indent(response.toString()));
            }
            ChannelFuture future = channel.write(response);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    /**
     * Indents the content of a multi-line string - used mainly for allowing the pretty display of Netty {@code toString()} output/
     */
    private static String indent(String s) {
        return "\t" + s.replace(StringUtil.NEWLINE, StringUtil.NEWLINE + "\t");
    }
}
