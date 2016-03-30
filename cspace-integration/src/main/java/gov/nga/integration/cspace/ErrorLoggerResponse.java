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