package gov.nga.entities.art;

/**
 * @author Vladimir Morozov
 * Specific exception for loads all TMS data.
 * 
 */
public class DataNotReadyException
    extends RuntimeException
{
    private static final long serialVersionUID = -4346125312783089824L;

    public DataNotReadyException()
    {
        super();
    }

    public DataNotReadyException(String message)
    {
        super(message);
    }

    public DataNotReadyException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataNotReadyException(Throwable cause)
    {
        super(cause);
    }
}
