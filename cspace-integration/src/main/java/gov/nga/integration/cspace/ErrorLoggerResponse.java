/*
    NGA ART DATA API: ErrorLoggerResponse is a container for errors that data consumers want to
    report back to the API via the ErrorLoggerController or that the application wants to report
    internally via its log files

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

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
package gov.nga.integration.cspace;

public class ErrorLoggerResponse {

	private final String severity;
    private final String origin;
    private final String summary;
    private final String details;

    public ErrorLoggerResponse(String severity, String origin, String summary, String details) {
        this.severity = severity;
        this.origin = origin;
        this.summary = summary;
        this.details = details;
    }

    public String getSeverity() {
		return severity;
	}

	public String getOrigin() {
		return origin;
	}

	public String getSummary() {
		return summary;
	}

	public String getDetails() {
		return details;
	}

}