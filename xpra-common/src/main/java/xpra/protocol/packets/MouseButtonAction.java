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

package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MouseButtonAction extends WindowPacket {

    private int button;
    private boolean pressed;
    private final List<Integer> pos = new ArrayList<>();
    private final List<Integer> modifiers = new ArrayList<>();

    public MouseButtonAction(int windowId, int button, boolean pressed, int x, int y) {
        super("button-action");
        this.windowId = windowId;
        this.button = button;
        this.pressed = pressed;
        pos.add(x);
        pos.add(y);
    }

    @Override
    public void serialize(Collection<Object> elems) {
        super.serialize(elems);
        elems.add(button);
        elems.add(pressed);
        elems.add(pos);
        elems.add(modifiers);
    }

}
