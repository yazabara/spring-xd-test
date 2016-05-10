package yazabara.springxd.source.crossorigin;

import org.springframework.xd.module.options.spi.ModuleOption;

/**
 * @author Yaroslav Zabara
 *         <p>
 *         Properties definition for CrossOriginSource module.
 */
public class CrossOriginSourceMetadata {
    private int port = 9000;

    public int getPort() {
        return port;
    }

    @ModuleOption("the port to listen to")
    public void setPort(int port) {
        this.port = port;
    }
}
