/*
    NGA Art Data API: DataNotReadyException is a Custom Exception thrown when
    the state of the data is not sufficient to handle an API call that is 
    received and rather than blocking I/O until the data is ready, it throws
    this exception instead so the caller can decide what to do rather than the APIs.

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet, Vladimir Morozov

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
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
