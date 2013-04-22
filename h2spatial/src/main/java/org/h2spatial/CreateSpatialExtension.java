/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2spatial;

import org.h2spatial.internal.GeoSpatialFunctions;
import org.h2spatial.internal.ST_GeomFromText;
import org.h2spatialapi.Function;
import org.h2spatialapi.ScalarFunction;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class can be used to build a sql script for H2 spatial functions.
 * 
 * @author Erwan Bocher
 */
public class CreateSpatialExtension {
    /** H2 base type for geometry column {@link java.sql.ResultSetMetaData#getColumnTypeName(int)} */
    public static final String GEOMETRY_BASE_TYPE = "OTHER";

    /**
     * @return instance of all built-ins functions
     */
    public static Function[] getBuiltInsFunctions() {
        return new Function[] {new ST_GeomFromText()};
    }
    /**
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     * @param BundleSymbolicName OSGi Bundle symbolic name
     * @param BundleVersion OSGi Bundle version
     */
    public static void InitSpatialExtension(Connection connection,String BundleSymbolicName,String BundleVersion) throws SQLException {
        registerGeometryType(connection);
        addSpatialFunctions(connection,BundleSymbolicName+":"+BundleVersion+":");
        connection.commit();
    }

    /**
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     */
    public static void InitSpatialExtension(Connection connection) throws SQLException {
        registerGeometryType(connection);
        addSpatialFunctions(connection,"");
    }

    private static void registerGeometryType(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CREATE DOMAIN IF NOT EXISTS GEOMETRY AS "+GEOMETRY_BASE_TYPE+";");
    }
    private static String getStringProperty(Function function, String propertyKey) {
        Object value = function.getProperty(propertyKey);
        return value instanceof String ? (String)value : "";
    }
	/**
	 * Create java code to add function copy paste into
	 * GeoSpatialFunctionsAddRemove to upload it
	 * @param st SQL Statement
	 * @param function Function instance
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version
	 */
    public static void registerFunction(Statement st,Function function,String packagePrepend) throws SQLException {
        String functionClass = function.getClass().getName();
        String functionAlias = getStringProperty(function,Function.PROP_NAME);
        if(functionAlias.isEmpty()) {
            functionAlias = function.getClass().getSimpleName();
        }
        String functionName=null;
        if(function instanceof ScalarFunction) {
            ScalarFunction scalarFunction = (ScalarFunction)function;
            functionName = scalarFunction.getJavaStaticMethod();
        }
        if(functionName!=null) {
            st.execute("DROP ALIAS IF EXISTS " + functionAlias);
            // Create alias, H2 does not support prepare statement on create alias
            st.execute("CREATE ALIAS " + functionAlias + " FOR \"" + packagePrepend + functionClass + "." + functionName + "\"");
        }
    }

    /**
     * Register all built-ins function
     * @param connection JDBC Connection
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version
     * @throws SQLException
     */
	private static void addSpatialFunctions(Connection connection,String packagePrepend) throws SQLException {
        Statement st = connection.createStatement();
        for(Function function : getBuiltInsFunctions()) {
            registerFunction(st,function,packagePrepend);
        }
	}

	/*
	 * Remove spatial type and functions from the current connection.
	 */
	public static void disposeSpatialExtension(Connection connection) throws SQLException {
		for (Method method : GeoSpatialFunctions.class.getDeclaredMethods()) {
			String functionName = method.getName();
            connection.createStatement().execute("DROP ALIAS IF EXISTS " + functionName);
		}
	}
}