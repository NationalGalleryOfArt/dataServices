package gov.nga.entities.common;

public abstract class FingerprintedEntity implements FingerprintedInterface { 

	private Long fingerprint = null;
	
	public FingerprintedEntity() {
	}
	
//	public FingerprintedEntity(ResultSet rs) throws SQLException {
//	}
	
	public FingerprintedEntity(Long fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public boolean sameFingerprint(FingerprintedEntity d) {
		return this.getFingerprint().equals(d.getFingerprint());
	}
	
	public Long getFingerprint() {
		return fingerprint;
	}

}