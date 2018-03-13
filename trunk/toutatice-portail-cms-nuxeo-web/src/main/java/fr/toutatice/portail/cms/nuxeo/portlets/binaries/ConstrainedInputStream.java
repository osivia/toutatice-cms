package fr.toutatice.portail.cms.nuxeo.portlets.binaries;

import java.io.IOException;
import java.io.InputStream;

/**
 * Constrained input stream.
 * 
 * @author Cédric Krommenhoek
 * @see InputSteamDecorator
 */
public class ConstrainedInputStream extends InputSteamDecorator {

    /** Constrained length. */
    private long length;


    /**
     * Constructor.
     * 
     * @param inputStream decorated input stream
     * @param length constrained length
     */
    public ConstrainedInputStream(InputStream inputStream, long length) {
        super(inputStream);
        this.length = length;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        int result;
        if (this.length-- > 0) {
            result = this.getInputStream().read();
        } else {
            result = -1;
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long skip(long n) throws IOException {
        this.length += n;
        return super.skip(n);
    }

}
