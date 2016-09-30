package fr.toutatice.portail.cms.nuxeo.portlets.binaries;

/**
 * Bytes range java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class BytesRange {

    /** Bytes range start. */
    private final long start;
    /** Bytes range end. */
    private final long end;
    /** Bytes range length. */
    private final long length;
    /** Bytes range total. */
    private final long total;


    /**
     * Constructor.
     *
     * @param start bytes range start
     * @param end bytes range end
     * @param total bytes range total
     */
    public BytesRange(long start, long end, long total) {
        super();
        this.start = start;
        this.end = end;
        this.length = (end - start) + 1;
        this.total = total;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BytesRange [");
        builder.append(this.start);
        builder.append("-");
        builder.append(this.end);
        builder.append("/");
        builder.append(this.total);
        builder.append("]");
        return builder.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (end ^ (end >>> 32));
        result = prime * result + (int) (start ^ (start >>> 32));
        result = prime * result + (int) (total ^ (total >>> 32));
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BytesRange other = (BytesRange) obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        if (total != other.total) {
            return false;
        }
        return true;
    }


    /**
     * Getter for start.
     *
     * @return the start
     */
    public long getStart() {
        return this.start;
    }

    /**
     * Getter for end.
     *
     * @return the end
     */
    public long getEnd() {
        return this.end;
    }

    /**
     * Getter for length.
     *
     * @return the length
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Getter for total.
     *
     * @return the total
     */
    public long getTotal() {
        return this.total;
    }

}
