package gov.nga.integration.cspace.tms;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nga.common.entities.art.ArtDataCacher;
import gov.nga.entities.art.datamanager.helpers.QuerierImpl;
import gov.nga.imaging.dao.NetXImageDAO;

@Service
public class CSpaceArtDataQuerier extends QuerierImpl 
{
    @Autowired
    private ArtDataCacher cacher;
    
    @Autowired
    private NetXImageDAO netxdao;
    
    @PostConstruct
    public void postConstruct() {
    	this.setArtDataCacher(cacher);
    	this.setImageDAO(netxdao);
    }
}
