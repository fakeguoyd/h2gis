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

package org.h2spatial.internal.function.spatial.convert;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.h2spatial.ValueGeometry;
import org.h2spatialapi.ScalarFunction;

import java.sql.SQLException;

/**
 * Convert a Well Known Text geometry string into a geometry instance.
 * TODO Read SRID in table constraint
 * @author Nicolas Fortin
 */
public class ST_GeomFromText implements ScalarFunction {
    private static WKTReader wktReader = new WKTReader();

    @Override
    public Object getProperty(String propertyName) {
        if(propertyName.equals(ScalarFunction.PROP_DETERMINISTIC)) {
            return true;
        }
        return null;
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGeometry";
    }

    /**
     * Convert well known text parameter into a Geometry
     * @param wkt Well known text
     * @return Geometry instance or null if parameter is null
     * @throws ParseException If wkt is invalid
     */
    public static ValueGeometry toGeometry(String wkt) throws SQLException {
        if(wkt == null) {
            return null;
        }
        try {
            return new ValueGeometry(wktReader.read(wkt));
        } catch (ParseException ex) {
            throw new SQLException("Cannot parse the WKT.",ex);
        }
    }

    /**
     * Convert well known text parameter into a Geometry
     * @param wkt Well known text
     * @param srid Geometry SRID
     * @return Geometry instance
     * @throws ParseException If wkt is invalid
     */
    public static ValueGeometry toGeometry(String wkt, int srid) throws SQLException {
        if(wkt == null) {
            return null;
        }
        try {
            WKTReader wktReaderSRID = new WKTReader(new GeometryFactory(new PrecisionModel(),srid));
            Geometry geometry = wktReaderSRID.read(wkt);
            return new ValueGeometry(geometry);
        } catch (ParseException ex) {
            throw new SQLException(ex);
        }
    }
}
