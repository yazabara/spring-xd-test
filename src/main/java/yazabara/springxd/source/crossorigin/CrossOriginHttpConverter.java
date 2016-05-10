package yazabara.springxd.source.crossorigin;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yaroslav Zabara
 */
public class CrossOriginHttpConverter implements MessageConverter {

    private final MessageBuilderFactory messageBuilderFactory;

    public CrossOriginHttpConverter() {
        this(new DefaultMessageBuilderFactory());
    }

    public CrossOriginHttpConverter(MessageBuilderFactory messageBuilderFactory) {
        this.messageBuilderFactory = messageBuilderFactory;
    }

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        throw new UnsupportedOperationException("This converter is for inbound messages only.");
    }

    @Override
    public Message<?> toMessage(Object payload, MessageHeaders header) {
        Assert.isInstanceOf(HttpRequest.class, payload);
        HttpRequest request = (HttpRequest) payload;
        ChannelBuffer content = request.getContent();
        Charset charsetToUse = null;
        boolean binary = false;
        if (content.readable()) {
            Map<String, String> messageHeaders = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : request.getHeaders()) {
                if (entry.getKey().equalsIgnoreCase("Content-Type")) {
                    MediaType contentType = MediaType.parseMediaType(entry.getValue());
                    charsetToUse = contentType.getCharSet();
                    messageHeaders.put(MessageHeaders.CONTENT_TYPE, entry.getValue());
                    binary = MediaType.APPLICATION_OCTET_STREAM.equals(contentType);
                } else if (!entry.getKey().toUpperCase().startsWith("ACCEPT")
                        && !entry.getKey().toUpperCase().equals("CONNECTION")) {
                    messageHeaders.put(entry.getKey(), entry.getValue());
                }
            }
            messageHeaders.put("requestPath", request.getUri());
            messageHeaders.put("requestMethod", request.getMethod().toString());
            addHeaders(messageHeaders, request);
            try {
                AbstractIntegrationMessageBuilder<?> builder;
                if (binary) {
                    builder = this.messageBuilderFactory.withPayload(toByteArray(content));
                } else {
                    // ISO-8859-1 is the default http charset when not set
                    charsetToUse = charsetToUse == null ? Charset.forName("ISO-8859-1") : charsetToUse;
                    builder = this.messageBuilderFactory.withPayload(content.toString(charsetToUse));
                }
                builder.copyHeaders(messageHeaders);
                return builder.build();
            } catch (Exception ex) {
                throw new MessageConversionException("Failed to convert netty event to a Message", ex);
            }
        } else {
            return null;
        }
    }

    private byte[] toByteArray(ChannelBuffer content) {
        if (content.hasArray()) {
            return content.array();
        } else {
            byte[] bytes = new byte[content.readableBytes()];
            content.getBytes(0, bytes);
            return bytes;
        }
    }

    /**
     * Add additional headers. Default implementation adds none.
     *
     * @param messageHeaders The headers that will be added to the message.
     * @param request        The HttpRequest
     */
    protected void addHeaders(Map<String, String> messageHeaders, HttpRequest request) {
    }

}
