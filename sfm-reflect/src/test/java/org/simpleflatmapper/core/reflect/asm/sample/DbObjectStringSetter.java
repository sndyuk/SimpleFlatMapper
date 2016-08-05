package org.simpleflatmapper.core.reflect.asm.sample;

import org.simpleflatmapper.core.reflect.Setter;
import org.simpleflatmapper.test.beans.DbObject;

public class DbObjectStringSetter implements Setter<DbObject, String> {

	@Override
	public void set(DbObject target, String value) throws Exception {
		target.setName(value);
	}

}