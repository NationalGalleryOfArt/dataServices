package gov.nga.integration.cspace.tms;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nga.imaging.dao.NetXDAO;
import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;

@Service
public class CSpaceNetX extends NetXDAO {

	@Resource(name="nga.jdbc.tms") 
    private DataSourceService ds;
	
    @Autowired
    private ConfigService cs;
	
	@Override
	protected DataSourceService getImageryDS() {
		return ds;
	}

	@Override
	protected ConfigService getConfigService() {
		// TODO Auto-generated method stub
		return cs;
	}

}
