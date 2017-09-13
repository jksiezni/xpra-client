/*
 * Copyright (C) 2017 Jakub Ksiezniak
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

package xpra.protocol.packets;

import java.util.Collection;

/**
 * @author Jakub Księżniak
 *
 */
public class PingEcho extends xpra.protocol.IOPacket {

	public long timestamp;
	public long l1 = 1;
	public long l2 = 1;
	public long l3 = 1;
	public int serverLatency = -1;
	
	public PingEcho(Ping ping, long l1, long l2, long l3, int serverLatency) {
		super("ping_echo");
		timestamp = ping.getTimestamp();
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		elems.add(timestamp);
		elems.add(l1);
		elems.add(l2);
		elems.add(l3);
		elems.add(serverLatency);
	}

}
