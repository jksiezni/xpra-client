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

package com.android.grafika.gles;

import android.opengl.GLES10;
import android.opengl.GLU;

/**
 *
 */
public class GLException extends RuntimeException {

    private final int mError;

    public GLException(final String msg) {
        super(msg);
        mError = GLES10.GL_NO_ERROR;
    }

    public GLException(final String msg, final int error) {
        super(getErrorString(msg, error));
        mError = error;
    }

    private static String getErrorString(String msg, int error) {
        String errorString = GLU.gluErrorString(error);
        if (errorString == null) {
            errorString = "Unknown error 0x" + Integer.toHexString(error);
        }
        return msg + " : glError " + errorString;
    }

    public int getError() {
        return mError;
    }

}

