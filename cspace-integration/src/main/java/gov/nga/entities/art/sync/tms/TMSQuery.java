package gov.nga.entities.art.sync.tms;

import gov.nga.common.utils.Constant;

public enum TMSQuery implements Constant<String> 
{
    VENUE("select exhibitionid, constituentid, venueopendate, venueclosedate, venueapproved, venuecode, fingerprint from data.exhibitions_venues order by constituentid, exhibitionid"),
    EXHIBITION("select exhibitionid, exhtitle, exhopendate, exhclosedate, exhstatus, fingerprint from data.exhibitions order by exhibitionid"),
    EXHIBITION_ARTOBJECT("select exhibitionid, objectid, loanid, title, catalognumber, dexid, medium, displaydate, creditline, dimensions, attribution, attributioninverted, photorestrictions, venuecount, venuecodes, keyobject, loanobjectstatus, fingerprint from data.objects_exhibitions order by exhibitionid"),
    EXHIBITION_LOAN("select loanid, loannumber, loanpurpose, loancategory, loantype, loanstatus, role, loanstart, loanend, constituentid, fingerprint from data.loans order by loanid"),
    EXHIBITION_CONSTITUENT("select fingerprint, exhibitionid, constituentid, displayorder, role, roletype from data.exhibitions_constituents order by exhibitionid, displayorder"),
    COMPONENT_LOCATION("select cratenumber, projectname, iscurrentlocation, entereddate, begindate, enddate, objectid, locationid from data.components_locations order by objectid, iscurrentlocation desc, begindate desc"),
    NGA_DEPARTMENT("select fingerprint, departmentid, department, mnemonic from data.departments");

    private final String queryString;
    
    TMSQuery(final String qs)
    {
        queryString = qs;
    }
    
    @Override
    public String getConstantValue() 
    {
        return queryString;
    }

}
