package gov.nga.entities.art;

public interface OperatingModeService {
	
	public enum OperatingMode {
		PUBLIC,
		PRIVATE
	}

	public OperatingMode getOperatingMode();
	
}
