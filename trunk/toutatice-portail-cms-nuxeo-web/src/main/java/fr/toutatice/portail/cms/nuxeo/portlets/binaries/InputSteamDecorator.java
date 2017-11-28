package fr.toutatice.portail.cms.nuxeo.portlets.binaries;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream decorator.
 * 
 * @author Cédric Krommenhoek
 * @see InputStream
 */
public abstract class InputSteamDecorator extends InputStream {

    /** Decorated input stream. */
    private final InputStream inputStream;


    /**
     * Constructor.
     * 
     * @param inputStream decorated input stream
     */
    public InputSteamDecorator(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.inputStream.close();
        super.close();
    }


    /**
     * Getter for inputStream.
     * 
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }


}
