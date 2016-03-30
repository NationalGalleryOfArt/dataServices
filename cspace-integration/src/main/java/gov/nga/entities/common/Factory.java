package gov.nga.entities.common;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Factory<T> {
    public T factory(ResultSet rs) throws SQLException;
}


