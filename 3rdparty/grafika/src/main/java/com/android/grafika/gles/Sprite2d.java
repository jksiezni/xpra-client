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

import android.opengl.Matrix;

/**
 * Base class for a 2d object.  Includes position, scale, rotation, and flat-shaded color.
 */
public class Sprite2d {
    private static final String TAG = GlUtil.TAG;

    private Drawable2d mDrawable;
    private float mColor[];
    private int mTextureId;
    private float mAngle;
    private float mScaleX, mScaleY;
    private float mPosX, mPosY;

    private float[] mModelViewMatrix;
    private boolean mMatrixReady;

    private float[] mScratchMatrix = new float[16];

    public Sprite2d(Drawable2d drawable) {
        mDrawable = drawable;
        mColor = new float[4];
        mColor[3] = 1.0f;
        mTextureId = -1;

        mModelViewMatrix = new float[16];
        mMatrixReady = false;
    }

    /**
     * Re-computes mModelViewMatrix, based on the current values for rotation, scale, and
     * translation.
     */
    private void recomputeMatrix() {
        float[] modelView = mModelViewMatrix;

        Matrix.setIdentityM(modelView, 0);
        Matrix.translateM(modelView, 0, mPosX, mPosY, 0.0f);
        if (mAngle != 0.0f) {
            Matrix.rotateM(modelView, 0, mAngle, 0.0f, 0.0f, 1.0f);
        }
        Matrix.scaleM(modelView, 0, mScaleX, mScaleY, 1.0f);
        mMatrixReady = true;
    }

    /**
     * Returns the sprite scale along the X axis.
     */
    public float getScaleX() {
        return mScaleX;
    }

    /**
     * Returns the sprite scale along the Y axis.
     */
    public float getScaleY() {
        return mScaleY;
    }

    /**
     * Sets the sprite scale (size).
     */
    public void setScale(float scaleX, float scaleY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mMatrixReady = false;
    }

    /**
     * Gets the sprite rotation angle, in degrees.
     */
    public float getRotation() {
        return mAngle;
    }

    /**
     * Sets the sprite rotation angle, in degrees.  Sprite will rotate counter-clockwise.
     */
    public void setRotation(float angle) {
        // Normalize.  We're not expecting it to be way off, so just iterate.
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        mAngle = angle;
        mMatrixReady = false;
    }

    /**
     * Returns the position on the X axis.
     */
    public float getPositionX() {
        return mPosX;
    }

    /**
     * Returns the position on the Y axis.
     */
    public float getPositionY() {
        return mPosY;
    }

    /**
     * Sets the sprite position.
     */
    public void setPosition(float posX, float posY) {
        mPosX = posX;
        mPosY = posY;
        mMatrixReady = false;
    }

    /**
     * Returns the model-view matrix.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public float[] getModelViewMatrix() {
        if (!mMatrixReady) {
            recomputeMatrix();
        }
        return mModelViewMatrix;
    }

    /**
     * Sets color to use for flat-shaded rendering.  Has no effect on textured rendering.
     */
    public void setColor(float red, float green, float blue) {
        mColor[0] = red;
        mColor[1] = green;
        mColor[2] = blue;
    }

    /**
     * Sets texture to use for textured rendering.  Has no effect on flat-shaded rendering.
     */
    public void setTexture(int textureId) {
        mTextureId = textureId;
    }

    /**
     * Returns the color.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public float[] getColor() {
        return mColor;
    }

    /**
     * Draws the rectangle with the supplied program and projection matrix.
     */
    public void draw(FlatShadedProgram program, float[] projectionMatrix) {
        // Compute model/view/projection matrix.
        Matrix.multiplyMM(mScratchMatrix, 0, projectionMatrix, 0, getModelViewMatrix(), 0);

        program.draw(mScratchMatrix, mColor, mDrawable.getVertexArray(), 0,
                mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
                mDrawable.getVertexStride());
    }

    /**
     * Draws the rectangle with the supplied program and projection matrix.
     */
    public void draw(Texture2dProgram program, float[] projectionMatrix) {
        // Compute model/view/projection matrix.
        Matrix.multiplyMM(mScratchMatrix, 0, projectionMatrix, 0, getModelViewMatrix(), 0);

        program.draw(mScratchMatrix, mDrawable.getVertexArray(), 0,
                mDrawable.getVertexCount(), mDrawable.getCoordsPerVertex(),
                mDrawable.getVertexStride(), GlUtil.IDENTITY_MATRIX, mDrawable.getTexCoordArray(),
                mTextureId, mDrawable.getTexCoordStride());
    }

    @Override
    public String toString() {
        return "[Sprite2d pos=" + mPosX + "," + mPosY +
                " scale=" + mScaleX + "," + mScaleY + " angle=" + mAngle +
                " color={" + mColor[0] + "," + mColor[1] + "," + mColor[2] +
                "} drawable=" + mDrawable + "]";
    }
}
