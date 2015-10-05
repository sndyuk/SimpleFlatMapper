package org.sfm.jdbc;

import org.junit.Test;
import org.sfm.beans.DbObject;
import org.sfm.map.Mapper;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PreparedStatementMapperBuilderTest {

    @Test
    public void testMapDbObjectToStatement() throws Exception {
        Mapper<DbObject, PreparedStatement> mapper =
                JdbcMapperFactory.newInstance().buildFrom(DbObject.class)
                        .addColumn("id")
                        .addColumn("name")
                        .addColumn("email")
                        .addColumn("creation_time")
                        .mapper();

        PreparedStatement ps = mock(PreparedStatement.class);

        DbObject dbObject = new DbObject();
        dbObject.setId(123);
        dbObject.setName("name");
        dbObject.setEmail("email");
        dbObject.setCreationTime(new Date());

        mapper.mapTo(dbObject, ps, null);

        verify(ps).setLong(1, 123);
        verify(ps).setString(2, "name");
        verify(ps).setString(3, "email");
        verify(ps).setTimestamp(4, new Timestamp(dbObject.getCreationTime().getTime()));
    }

}