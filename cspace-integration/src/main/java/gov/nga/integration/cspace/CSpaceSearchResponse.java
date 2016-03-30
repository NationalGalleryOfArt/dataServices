package gov.nga.integration.cspace;

public class CSpaceSearchResponse {

    private final long id;
    private final String content;

    public CSpaceSearchResponse(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}