package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Department;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.utils.db.DataSourceService;

public class DepartmentFactory  implements TMSEntityFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(DepartmentFactory.class);
    
    public final List<Department> departments = CollectionUtils.newArrayList();

    synchronized public Map<Long, Department> buildDepartments(final DataSourceService ps)
    {
        departments.clear();
        final TMSQuerier querier = new TMSQuerier(ps);
        try 
        {
            querier.getQueryResults(TMSQuery.NGA_DEPARTMENT.getConstantValue(), this);
        } 
        catch (final SQLException err) 
        {
            LOG.error(String.format("Caught an exception loading Departments with query: %s", TMSQuery.NGA_DEPARTMENT.getConstantValue()), err);
        }
        Map<Long, Department> dMap = CollectionUtils.newHashMap();
        for (Department d: departments)
        {
            dMap.put(d.getID(), d);
        }
        return dMap;
    }
    
    @Override
    public void processResult(final ResultSet rs) throws SQLException 
    {
        final TMSDepartment tmp = new TMSDepartment();
        try
        {
            
            departments.add((TMSDepartment)tmp.factory(rs));
        }
        catch (final Exception err)
        {
            LOG.error("Caught exception while adding a department", err);
        }
    }

}
