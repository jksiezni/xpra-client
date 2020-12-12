/*
 * Copyright (C) 2020 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package xpra.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class Packet {

    protected void deserialize(Iterator<Object> it) {
    }
//		final String newType = asString(it.next());
//		if(!type.equals(newType)) {
//			throw new IllegalStateException("Trying to deserialize " + newType + " packet to "
//          + getClass().getCanonicalName() + "(" + type +")");
//		}

    protected static boolean asBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue() != 0;
        }
        return false;
    }

    protected static int asInt(Object obj) {
        return ((Number) obj).intValue();
    }

    protected static long asLong(Object obj) {
        return ((Number) obj).longValue();
    }

    protected static String asString(Object obj) {
        if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        } else {
            return (String) obj;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static List<String> asStringList(Object obj) {
        List list = (List) obj;
        List<String> result = new ArrayList<>();
        for (Object elem : list) {
            result.add(asString(elem));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, Object> asMap(Object obj) {
        return (Map<String, Object>) obj;
    }

    protected byte[] asByteArray(Object obj) {
        return (byte[]) obj;
    }

    protected <T> List<T> asList(Object obj) {
        return (List<T>) obj;
    }
}
