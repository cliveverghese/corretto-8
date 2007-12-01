/*
 * Copyright 2001-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.awt.image.ImagingOpException;
import sun.awt.SunHints;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.ToolkitImage;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.BlitBg;
import sun.java2d.loops.TransformHelper;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.ScaledBlit;
import sun.java2d.loops.SurfaceType;

public class DrawImage implements DrawImagePipe
{
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor)
    {
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        if (isSimpleTranslate(sg)) {
            return renderImageCopy(sg, img, bgColor,
                                   x + sg.transX, y + sg.transY,
                                   0, 0, imgw, imgh);
        }
        AffineTransform atfm = sg.transform;
        if ((x | y) != 0) {
            atfm = new AffineTransform(atfm);
            atfm.translate(x, y);
        }
        transformImage(sg, img, atfm, sg.interpolationType,
                       0, 0, imgw, imgh, bgColor);
        return true;
    }

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor)
    {
        if (isSimpleTranslate(sg)) {
            return renderImageCopy(sg, img, bgColor,
                                   dx + sg.transX, dy + sg.transY,
                                   sx, sy, w, h);
        }
        scaleImage(sg, img, dx, dy, (dx + w), (dy + h),
                   sx, sy, (sx + w), (sy + h), bgColor);
        return true;
    }

    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y,
                              int width, int height,
                              Color bgColor)
    {
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        // Only accelerate scale if:
        //          - w/h positive values
        //          - sg transform integer translate/identity only
        //          - no bgColor in operation
        if ((width > 0) && (height > 0) && isSimpleTranslate(sg)) {
            double dx1 = x + sg.transX;
            double dy1 = y + sg.transY;
            double dx2 = dx1 + width;
            double dy2 = dy1 + height;
            if (renderImageScale(sg, img, bgColor, sg.interpolationType,
                                 0, 0, imgw, imgh,
                                 dx1, dy1, dx2, dy2))
            {
                return true;
            }
        }

        AffineTransform atfm = sg.transform;
        if ((x | y) != 0 || width != imgw || height != imgh) {
            atfm = new AffineTransform(atfm);
            atfm.translate(x, y);
            atfm.scale(((double)width)/imgw, ((double)height)/imgh);
        }
        transformImage(sg, img, atfm, sg.interpolationType,
                       0, 0, imgw, imgh, bgColor);
        return true;
    }

    /*
     * This method is only called in those circumstances where the
     * operation has a non-null secondary transform specfied.  Its
     * role is to check for various optimizations based on the types
     * of both the secondary and SG2D transforms and to do some
     * quick calculations to avoid having to combine the transforms
     * and/or to call a more generalized method.
     */
    protected void transformImage(SunGraphics2D sg, Image img, int x, int y,
                                  AffineTransform extraAT, int interpType)
    {
        int txtype = extraAT.getType();
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        boolean checkfinalxform;

        if (sg.transformState <= sg.TRANSFORM_ANY_TRANSLATE &&
            (txtype == AffineTransform.TYPE_IDENTITY ||
             txtype == AffineTransform.TYPE_TRANSLATION))
        {
            // First optimization - both are some kind of translate

            // Combine the translations and check if interpolation is necessary.
            double tx = extraAT.getTranslateX();
            double ty = extraAT.getTranslateY();
            tx += sg.transform.getTranslateX();
            ty += sg.transform.getTranslateY();
            int itx = (int) Math.floor(tx + 0.5);
            int ity = (int) Math.floor(ty + 0.5);
            if (interpType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR ||
                (closeToInteger(itx, tx) && closeToInteger(ity, ty)))
            {
                renderImageCopy(sg, img, null, x+itx, y+ity, 0, 0, imgw, imgh);
                return;
            }
            checkfinalxform = false;
        } else if (sg.transformState <= sg.TRANSFORM_TRANSLATESCALE &&
                   ((txtype & (AffineTransform.TYPE_FLIP |
                               AffineTransform.TYPE_MASK_ROTATION |
                               AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0))
        {
            // Second optimization - both are some kind of translate or scale

            // Combine the scales and check if interpolation is necessary.

            // Transform source bounds by extraAT,
            // then translate the bounds again by x, y
            // then transform the bounds again by sg.transform
            double coords[] = new double[] {
                0, 0, imgw, imgh,
            };
            extraAT.transform(coords, 0, coords, 0, 2);
            coords[0] += x;
            coords[1] += y;
            coords[2] += x;
            coords[3] += y;
            sg.transform.transform(coords, 0, coords, 0, 2);

            if (tryCopyOrScale(sg, img, 0, 0, imgw, imgh,
                               null, interpType, coords))
            {
                return;
            }
            checkfinalxform = false;
        } else {
            checkfinalxform = true;
        }

        // Begin Transform
        AffineTransform tx = new AffineTransform(sg.transform);
        tx.translate(x, y);
        tx.concatenate(extraAT);

        // Do not try any more optimizations if either of the cases
        // above was tried as we have already verified that the
        // resulting transform will not simplify.
        if (checkfinalxform) {
            // In this case neither of the above simple transform
            // pairs was found so we will do some final tests on
            // the final rendering transform which may be the
            // simple product of two complex transforms.
            transformImage(sg, img, tx, interpType, 0, 0, imgw, imgh, null);
        } else {
            renderImageXform(sg, img, tx, interpType, 0, 0, imgw, imgh, null);
        }
    }

    /*
     * This method is called with a final rendering transform that
     * has combined all of the information about the Graphics2D
     * transform attribute with the transformations specified by
     * the arguments to the drawImage call.
     * Its role is to see if the combined transform ends up being
     * acceleratable by either a renderImageCopy or renderImageScale
     * once all of the math is done.
     *
     * Note: The transform supplied here has an origin that is
     * already adjusted to point to the device location where
     * the (sx1, sy1) location of the source image should be placed.
     */
    protected void transformImage(SunGraphics2D sg, Image img,
                                  AffineTransform tx, int interpType,
                                  int sx1, int sy1, int sx2, int sy2,
                                  Color bgColor)
    {
        // Transform 3 source corners by tx and analyze them
        // for simplified operations (Copy or Scale).  Using
        // 3 points lets us analyze any kind of transform,
        // even transforms that involve very tiny amounts of
        // rotation or skew to see if they degenerate to a
        // simple scale or copy operation within the allowable
        // error bounds.
        // Note that we use (0,0,w,h) instead of (sx1,sy1,sx2,sy2)
        // because the transform is already translated such that
        // the origin is where sx1, sy1 should go.
        double coords[] = new double[6];
        /* index:  0  1    2  3    4  5  */
        /* coord: (0, 0), (w, h), (0, h) */
        coords[2] = sx2 - sx1;
        coords[3] = coords[5] = sy2 - sy1;
        tx.transform(coords, 0, coords, 0, 3);
        // First test if the X coords of the transformed UL
        // and LL points match and that the Y coords of the
        // transformed LR and LL points also match.
        // If they do then it is a "rectilinear" transform and
        // tryCopyOrScale will make sure it is upright and
        // integer-based.
        if (Math.abs(coords[0] - coords[4]) < MAX_TX_ERROR &&
            Math.abs(coords[3] - coords[5]) < MAX_TX_ERROR &&
            tryCopyOrScale(sg, img, sx1, sy1, sx2, sy2,
                           bgColor, interpType, coords))
        {
            return;
        }

        renderImageXform(sg, img, tx, interpType, sx1, sy1, sx2, sy2, bgColor);
    }

    /*
     * Check the bounding coordinates of the transformed source
     * image to see if they fall on integer coordinates such
     * that they will cause no interpolation anomalies if we
     * use our simplified Blit or ScaledBlit operations instead
     * of a full transform operation.
     */
    protected boolean tryCopyOrScale(SunGraphics2D sg,
                                     Image img,
                                     int sx1, int sy1,
                                     int sx2, int sy2,
                                     Color bgColor, int interpType,
                                     double coords[])
    {
        double dx = coords[0];
        double dy = coords[1];
        double dw = coords[2] - dx;
        double dh = coords[3] - dy;
        // First check if width and height are very close to img w&h.
        if (closeToInteger(sx2-sx1, dw) && closeToInteger(sy2-sy1, dh)) {
            // Round location to nearest pixel and then test
            // if it will cause interpolation anomalies.
            int idx = (int) Math.floor(dx + 0.5);
            int idy = (int) Math.floor(dy + 0.5);
            if (interpType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR ||
                (closeToInteger(idx, dx) && closeToInteger(idy, dy)))
            {
                renderImageCopy(sg, img, bgColor,
                                idx, idy,
                                sx1, sy1, sx2-sx1, sy2-sy1);
                return true;
            }
        }
        // (For now) We can only use our ScaledBlits if the image
        // is upright (i.e. dw & dh both > 0)
        if (dw > 0 && dh > 0) {
            if (renderImageScale(sg, img, bgColor, interpType,
                                 sx1, sy1, sx2, sy2,
                                 coords[0], coords[1], coords[2], coords[3]))
            {
                return true;
            }
        }
        return false;
    }

    /*
     * Return a BufferedImage of the requested type with the indicated
     * subimage of the original image located at 0,0 in the new image.
     * If a bgColor is supplied, composite the original image over that
     * color with a SrcOver operation, otherwise make a SrcNoEa copy.
     */
    BufferedImage makeBufferedImage(Image img, Color bgColor, int type,
                                    int sx1, int sy1, int sx2, int sy2)
    {
        BufferedImage bimg = new BufferedImage(sx2-sx1, sy2-sy1, type);
        Graphics2D g2d = bimg.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        if (bgColor != null) {
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, sx2-sx1, sy2-sy1);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
        g2d.drawImage(img, -sx1, -sy1, null);
        g2d.dispose();
        return bimg;
    }

    protected void renderImageXform(SunGraphics2D sg, Image img,
                                    AffineTransform tx, int interpType,
                                    int sx1, int sy1, int sx2, int sy2,
                                    Color bgColor)
    {
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;
        SurfaceData srcData = dstData.getSourceSurfaceData(img,
                                                           sg.TRANSFORM_GENERIC,
                                                           sg.imageComp,
                                                           bgColor);

        if (srcData == null) {
            img = getBufferedImage(img);
            srcData = dstData.getSourceSurfaceData(img,
                                                   sg.TRANSFORM_GENERIC,
                                                   sg.imageComp,
                                                   bgColor);
            if (srcData == null) {
                // REMIND: Is this correct?  Can this happen?
                return;
            }
        }

        if (isBgOperation(srcData, bgColor)) {
            // We cannot perform bg operations during transform so make
            // an opaque temp image with the appropriate background
            // and work from there.
            img = makeBufferedImage(img, bgColor, BufferedImage.TYPE_INT_RGB,
                                    sx1, sy1, sx2, sy2);
            // Temp image has appropriate subimage at 0,0 now.
            sx2 -= sx1;
            sy2 -= sy1;
            sx1 = sy1 = 0;

            srcData = dstData.getSourceSurfaceData(img,
                                                   sg.TRANSFORM_GENERIC,
                                                   sg.imageComp,
                                                   bgColor);
        }

        SurfaceType srcType = srcData.getSurfaceType();
        TransformHelper helper = TransformHelper.getFromCache(srcType);

        if (helper == null) {
            /* We have no helper for this source image type.
             * But we know that we do have helpers for both RGB and ARGB,
             * so convert to one of those types depending on transparency.
             * ARGB_PRE might be a better choice if the source image has
             * alpha, but it may cause some recursion here since we only
             * tend to have converters that convert to ARGB.
             */
            int type = ((srcData.getTransparency() == Transparency.OPAQUE)
                        ? BufferedImage.TYPE_INT_RGB
                        : BufferedImage.TYPE_INT_ARGB);
            img = makeBufferedImage(img, null, type, sx1, sy1, sx2, sy2);
            // Temp image has appropriate subimage at 0,0 now.
            sx2 -= sx1;
            sy2 -= sy1;
            sx1 = sy1 = 0;

            srcData = dstData.getSourceSurfaceData(img,
                                                   sg.TRANSFORM_GENERIC,
                                                   sg.imageComp,
                                                   null);
            srcType = srcData.getSurfaceType();
            helper = TransformHelper.getFromCache(srcType);
            // assert(helper != null);
        }

        AffineTransform itx;
        try {
            itx = tx.createInverse();
        } catch (NoninvertibleTransformException e) {
            // Non-invertible transform means no output
            return;
        }

        /*
         * Find the maximum bounds on the destination that will be
         * affected by the transformed source.  First, transform all
         * four corners of the source and then min and max the resulting
         * destination coordinates of the transformed corners.
         * Note that tx already has the offset to sx1,sy1 accounted
         * for so we use the box (0, 0, sx2-sx1, sy2-sy1) as the
         * source coordinates.
         */
        double coords[] = new double[8];
        /* corner:  UL      UR      LL      LR   */
        /* index:  0  1    2  3    4  5    6  7  */
        /* coord: (0, 0), (w, 0), (0, h), (w, h) */
        coords[2] = coords[6] = sx2 - sx1;
        coords[5] = coords[7] = sy2 - sy1;
        tx.transform(coords, 0, coords, 0, 4);
        double ddx1, ddy1, ddx2, ddy2;
        ddx1 = ddx2 = coords[0];
        ddy1 = ddy2 = coords[1];
        for (int i = 2; i < coords.length; i += 2) {
            double d = coords[i];
            if (ddx1 > d) ddx1 = d;
            else if (ddx2 < d) ddx2 = d;
            d = coords[i+1];
            if (ddy1 > d) ddy1 = d;
            else if (ddy2 < d) ddy2 = d;
        }
        int dx1 = (int) Math.floor(ddx1);
        int dy1 = (int) Math.floor(ddy1);
        int dx2 = (int) Math.ceil(ddx2);
        int dy2 = (int) Math.ceil(ddy2);

        SurfaceType dstType = dstData.getSurfaceType();
        MaskBlit maskblit;
        Blit blit;
        if (sg.compositeState <= sg.COMP_ALPHA) {
            /* NOTE: We either have, or we can make,
             * a MaskBlit for any alpha composite type
             */
            maskblit = MaskBlit.getFromCache(SurfaceType.IntArgbPre,
                                             sg.imageComp,
                                             dstType);

            /* NOTE: We can only use the native TransformHelper
             * func to go directly to the dest if both the helper
             * and the MaskBlit are native.
             * All helpers are native at this point, but some MaskBlit
             * objects are implemented in Java, so we need to check.
             */
            if (maskblit.getNativePrim() != 0) {
                // We can render directly.
                helper.Transform(maskblit, srcData, dstData,
                                 sg.composite, clip,
                                 itx, interpType,
                                 sx1, sy1, sx2, sy2,
                                 dx1, dy1, dx2, dy2,
                                 null, 0, 0);
                return;
            }
            blit = null;
        } else {
            /* NOTE: We either have, or we can make,
             * a Blit for any composite type, even Custom
             */
            maskblit = null;
            blit = Blit.getFromCache(SurfaceType.IntArgbPre,
                                     sg.imageComp,
                                     dstType);
        }

        // We need to transform to a temp image and then copy
        // just the pieces that are valid data to the dest.
        BufferedImage tmpimg = new BufferedImage(dx2-dx1, dy2-dy1,
                                                 BufferedImage.TYPE_INT_ARGB);
        SurfaceData tmpData = SurfaceData.getPrimarySurfaceData(tmpimg);
        SurfaceType tmpType = tmpData.getSurfaceType();
        MaskBlit tmpmaskblit =
            MaskBlit.getFromCache(SurfaceType.IntArgbPre,
                                  CompositeType.SrcNoEa,
                                  tmpType);
        /*
         * The helper function fills a temporary edges buffer
         * for us with the bounding coordinates of each scanline
         * in the following format:
         *
         * edges[0, 1] = [top y, bottom y)
         * edges[2, 3] = [left x, right x) of top row
         * ...
         * edges[h*2, h*2+1] = [left x, right x) of bottom row
         *
         * all coordinates in the edges array will be relative to dx1, dy1
         *
         * edges thus has to be h*2+2 in length
         */
        int edges[] = new int[(dy2-dy1)*2+2];
        helper.Transform(tmpmaskblit, srcData, tmpData,
                         AlphaComposite.Src, null,
                         itx, interpType,
                         sx1, sy1, sx2, sy2,
                         0, 0, dx2-dx1, dy2-dy1,
                         edges, dx1, dy1);

        /*
         * Now copy the results, scanline by scanline, into the dest.
         * The edges array helps us minimize the work.
         */
        int index = 2;
        for (int y = edges[0]; y < edges[1]; y++) {
            int relx1 = edges[index++];
            int relx2 = edges[index++];
            if (relx1 >= relx2) {
                continue;
            }
            if (maskblit != null) {
                maskblit.MaskBlit(tmpData, dstData,
                                  sg.composite, clip,
                                  relx1, y,
                                  dx1+relx1, dy1+y,
                                  relx2 - relx1, 1,
                                  null, 0, 0);
            } else {
                blit.Blit(tmpData, dstData,
                          sg.composite, clip,
                          relx1, y,
                          dx1+relx1, dy1+y,
                          relx2 - relx1, 1);
            }
        }
    }

    // Render an image using only integer translation
    // (no scale or transform or sub-pixel interpolated translations).
    protected boolean renderImageCopy(SunGraphics2D sg, Image img,
                                      Color bgColor,
                                      int dx, int dy,
                                      int sx, int sy,
                                      int w, int h)
    {
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;

        int attempts = 0;
        // Loop up to twice through; this gives us a chance to
        // revalidate the surfaceData objects in case of an exception
        // and try it once more
        while (true) {
            SurfaceData srcData =
                dstData.getSourceSurfaceData(img,
                                             sg.TRANSFORM_ISIDENT,
                                             sg.imageComp,
                                             bgColor);
            if (srcData == null) {
                return false;
            }

            try {
                SurfaceType srcType = srcData.getSurfaceType();
                SurfaceType dstType = dstData.getSurfaceType();
                blitSurfaceData(sg, clip,
                                srcData, dstData, srcType, dstType,
                                sx, sy, dx, dy, w, h, bgColor);
                return true;
            } catch (NullPointerException e) {
                if (!(SurfaceData.isNull(dstData) ||
                      SurfaceData.isNull(srcData)))
                {
                    // Something else caused the exception, throw it...
                    throw e;
                }
                return false;
                // NOP if we have been disposed
            } catch (InvalidPipeException e) {
                // Always catch the exception; try this a couple of times
                // and fail silently if the system is not yet ready to
                // revalidate the source or dest surfaceData objects.
                ++attempts;
                clip = sg.getCompClip();   // ensures sg.surfaceData is valid
                dstData = sg.surfaceData;
                if (SurfaceData.isNull(dstData) ||
                    SurfaceData.isNull(srcData) || (attempts > 1))
                {
                    return false;
                }
            }
        }
    }

    // Render an image using only integer scaling (no transform).
    protected boolean renderImageScale(SunGraphics2D sg, Image img,
                                       Color bgColor, int interpType,
                                       int sx1, int sy1,
                                       int sx2, int sy2,
                                       double dx1, double dy1,
                                       double dx2, double dy2)
    {
        // Currently only NEAREST_NEIGHBOR interpolation is implemented
        // for ScaledBlit operations.
        if (interpType != AffineTransformOp.TYPE_NEAREST_NEIGHBOR) {
            return false;
        }

        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;

        int attempts = 0;
        // Loop up to twice through; this gives us a chance to
        // revalidate the surfaceData objects in case of an exception
        // and try it once more
        while (true) {
            SurfaceData srcData =
                dstData.getSourceSurfaceData(img,
                                             sg.TRANSFORM_TRANSLATESCALE,
                                             sg.imageComp,
                                             bgColor);

            if (srcData == null || isBgOperation(srcData, bgColor)) {
                return false;
            }

            try {
                SurfaceType srcType = srcData.getSurfaceType();
                SurfaceType dstType = dstData.getSurfaceType();
                return scaleSurfaceData(sg, clip,
                                        srcData, dstData, srcType, dstType,
                                        sx1, sy1, sx2, sy2,
                                        dx1, dy1, dx2, dy2);
            } catch (NullPointerException e) {
                if (!SurfaceData.isNull(dstData)) {
                    // Something else caused the exception, throw it...
                    throw e;
                }
                return false;
                // NOP if we have been disposed
            } catch (InvalidPipeException e) {
                // Always catch the exception; try this a couple of times
                // and fail silently if the system is not yet ready to
                // revalidate the source or dest surfaceData objects.
                ++attempts;
                clip = sg.getCompClip();  // ensures sg.surfaceData is valid
                dstData = sg.surfaceData;
                if (SurfaceData.isNull(dstData) ||
                    SurfaceData.isNull(srcData) || (attempts > 1))
                {
                    return false;
                }
            }
        }
    }

    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor)
    {
        int srcW, srcH, dstW, dstH;
        int srcX, srcY, dstX, dstY;
        boolean srcWidthFlip = false;
        boolean srcHeightFlip = false;
        boolean dstWidthFlip = false;
        boolean dstHeightFlip = false;

        if (sx2 > sx1) {
            srcW = sx2 - sx1;
            srcX = sx1;
        } else {
            srcWidthFlip = true;
            srcW = sx1 - sx2;
            srcX = sx2;
        }
        if (sy2 > sy1) {
            srcH = sy2-sy1;
            srcY = sy1;
        } else {
            srcHeightFlip = true;
            srcH = sy1-sy2;
            srcY = sy2;
        }
        if (dx2 > dx1) {
            dstW = dx2 - dx1;
            dstX = dx1;
        } else {
            dstW = dx1 - dx2;
            dstWidthFlip = true;
            dstX = dx2;
        }
        if (dy2 > dy1) {
            dstH = dy2 - dy1;
            dstY = dy1;
        } else {
            dstH = dy1 - dy2;
            dstHeightFlip = true;
            dstY = dy2;
        }
        if (srcW <= 0 || srcH <= 0) {
            return true;
        }
        // Only accelerate scale if it does not involve a flip or transform
        if ((srcWidthFlip == dstWidthFlip) &&
            (srcHeightFlip == dstHeightFlip) &&
            isSimpleTranslate(sg))
        {
            double ddx1 = dstX + sg.transX;
            double ddy1 = dstY + sg.transY;
            double ddx2 = ddx1 + dstW;
            double ddy2 = ddy1 + dstH;
            if (renderImageScale(sg, img, bgColor, sg.interpolationType,
                                 srcX, srcY, srcX+srcW, srcY+srcH,
                                 ddx1, ddy1, ddx2, ddy2))
            {
                return true;
            }
        }

        AffineTransform atfm = new AffineTransform(sg.transform);
        atfm.translate(dx1, dy1);
        double m00 = (double)(dx2-dx1)/(sx2-sx1);
        double m11 = (double)(dy2-dy1)/(sy2-sy1);
        atfm.scale(m00, m11);
        atfm.translate(srcX-sx1, srcY-sy1);

        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        srcW += srcX;
        srcH += srcY;
        // Make sure we are not out of bounds
        if (srcW > imgW) {
            srcW = imgW;
        }
        if (srcH > imgH) {
            srcH = imgH;
        }
        if (srcX < 0) {
            atfm.translate(-srcX, 0);
            srcX = 0;
        }
        if (srcY < 0) {
            atfm.translate(0, -srcY);
            srcY = 0;
        }
        if (srcX >= srcW || srcY >= srcH) {
            return true;
        }
        // Note: src[WH] are currently the right and bottom coordinates.
        // The following two lines would adjust src[WH] back to being
        // dimensions.
        //     srcW -= srcX;
        //     srcH -= srcY;
        // Since transformImage needs right and bottom coords we will
        // omit this adjustment.

        transformImage(sg, img, atfm, sg.interpolationType,
                       srcX, srcY, srcW, srcH, bgColor);
        return true;
    }

    /**
     ** Utilities
     ** The following methods are used by the public methods above
     ** for performing various operations
     **/

    /*
     * This constant represents a tradeoff between the
     * need to make sure that image transformations are
     * "very close" to integer device coordinates before
     * we decide to use an integer scale or copy operation
     * as a substitute and the fact that roundoff errors
     * in AffineTransforms are frequently introduced by
     * performing multiple sequential operations on them.
     *
     * The evaluation of bug 4990624 details the potential
     * for this error cutoff to result in display anomalies
     * in different types of image operations and how this
     * value represents a good compromise here.
     */
    private static final double MAX_TX_ERROR = .0001;

    public static boolean closeToInteger(int i, double d) {
        return (Math.abs(d-i) < MAX_TX_ERROR);
    }

    public static boolean isSimpleTranslate(SunGraphics2D sg) {
        int ts = sg.transformState;
        if (ts <= sg.TRANSFORM_INT_TRANSLATE) {
            // Integer translates are always "simple"
            return true;
        }
        if (ts >= sg.TRANSFORM_TRANSLATESCALE) {
            // Scales and beyond are always "not simple"
            return false;
        }
        // non-integer translates are only simple when not interpolating
        if (sg.interpolationType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR) {
            return true;
        }
        return false;
    }

    protected static boolean isBgOperation(SurfaceData srcData, Color bgColor) {
        // If we cannot get the srcData, then cannot assume anything about
        // the image
        return ((srcData == null) ||
                ((bgColor != null) &&
                 (srcData.getTransparency() != Transparency.OPAQUE)));
    }

    protected BufferedImage getBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        }
        // Must be VolatileImage; get BufferedImage representation
        return ((VolatileImage)img).getSnapshot();
    }

    /*
     * Return the color model to be used with this BufferedImage and
     * transform.
     */
    private ColorModel getTransformColorModel(SunGraphics2D sg,
                                              BufferedImage bImg,
                                              AffineTransform tx) {
        ColorModel cm = bImg.getColorModel();
        ColorModel dstCM = cm;

        if (tx.isIdentity()) {
            return dstCM;
        }
        int type = tx.getType();
        boolean needTrans =
            ((type&(tx.TYPE_MASK_ROTATION|tx.TYPE_GENERAL_TRANSFORM)) != 0);
        if (! needTrans && type != tx.TYPE_TRANSLATION && type != tx.TYPE_IDENTITY)
        {
            double[] mtx = new double[4];
            tx.getMatrix(mtx);
            // Check out the matrix.  A non-integral scale will force ARGB
            // since the edge conditions cannot be guaranteed.
            needTrans = (mtx[0] != (int)mtx[0] || mtx[3] != (int)mtx[3]);
        }

        if (sg.renderHint != SunHints.INTVAL_RENDER_QUALITY) {
            if (cm instanceof IndexColorModel) {
                Raster raster = bImg.getRaster();
                IndexColorModel icm = (IndexColorModel) cm;
                // Just need to make sure that we have a transparent pixel
                if (needTrans && cm.getTransparency() == cm.OPAQUE) {
                    // Fix 4221407
                    if (raster instanceof sun.awt.image.BytePackedRaster) {
                        dstCM = ColorModel.getRGBdefault();
                    }
                    else {
                        double[] matrix = new double[6];
                        tx.getMatrix(matrix);
                        if (matrix[1] == 0. && matrix[2] ==0.
                            && matrix[4] == 0. && matrix[5] == 0.) {
                            // Only scaling so do not need to create
                        }
                        else {
                            int mapSize = icm.getMapSize();
                            if (mapSize < 256) {
                                int[] cmap = new int[mapSize+1];
                                icm.getRGBs(cmap);
                                cmap[mapSize] = 0x0000;
                                dstCM = new
                                    IndexColorModel(icm.getPixelSize(),
                                                    mapSize+1,
                                                    cmap, 0, true, mapSize,
                                                    DataBuffer.TYPE_BYTE);
                            }
                            else {
                                dstCM = ColorModel.getRGBdefault();
                            }
                        }  /* if (matrix[0] < 1.f ...) */
                    }   /* raster instanceof sun.awt.image.BytePackedRaster */
                } /* if (cm.getTransparency() == cm.OPAQUE) */
            } /* if (cm instanceof IndexColorModel) */
            else if (needTrans && cm.getTransparency() == cm.OPAQUE) {
                // Need a bitmask transparency
                // REMIND: for now, use full transparency since no loops
                // for bitmask
                dstCM = ColorModel.getRGBdefault();
            }
        } /* if (sg.renderHint == RENDER_QUALITY) */
        else {

            if (cm instanceof IndexColorModel ||
                (needTrans && cm.getTransparency() == cm.OPAQUE))
            {
                // Need a bitmask transparency
                // REMIND: for now, use full transparency since no loops
                // for bitmask
                dstCM = ColorModel.getRGBdefault();
            }
        }

        return dstCM;
    }

    protected void blitSurfaceData(SunGraphics2D sg,
                                   Region clipRegion,
                                   SurfaceData srcData,
                                   SurfaceData dstData,
                                   SurfaceType srcType,
                                   SurfaceType dstType,
                                   int sx, int sy, int dx, int dy,
                                   int w, int h,
                                   Color bgColor)
    {
        if (w <= 0 || h <= 0) {
            /*
             * Fix for bugid 4783274 - BlitBg throws an exception for
             * a particular set of anomalous parameters.
             * REMIND: The native loops do proper clipping and would
             * detect this situation themselves, but the Java loops
             * all seem to trust their parameters a little too well
             * to the point where they will try to process a negative
             * area of pixels and throw exceptions.  The real fix is
             * to modify the Java loops to do proper clipping so that
             * they can deal with negative dimensions as well as
             * improperly large dimensions, but that fix is too risky
             * to integrate for Mantis at this point.  In the meantime
             * eliminating the negative or zero dimensions here is
             * "correct" and saves them from some nasty exceptional
             * conditions, one of which is the test case of 4783274.
             */
            return;
        }
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) &&
            (srcData.getTransparency() == Transparency.OPAQUE ||
             (bgColor != null &&
              bgColor.getTransparency() == Transparency.OPAQUE)))
        {
            comp = CompositeType.SrcNoEa;
        }
        if (!isBgOperation(srcData, bgColor)) {
            Blit blit = Blit.getFromCache(srcType, comp, dstType);
            blit.Blit(srcData, dstData, sg.composite, clipRegion,
                      sx, sy, dx, dy, w, h);
        } else {
            BlitBg blit = BlitBg.getFromCache(srcType, comp, dstType);
            blit.BlitBg(srcData, dstData, sg.composite, clipRegion,
                        bgColor, sx, sy, dx, dy, w, h);
        }
    }

    protected boolean scaleSurfaceData(SunGraphics2D sg,
                                       Region clipRegion,
                                       SurfaceData srcData,
                                       SurfaceData dstData,
                                       SurfaceType srcType,
                                       SurfaceType dstType,
                                       int sx1, int sy1,
                                       int sx2, int sy2,
                                       double dx1, double dy1,
                                       double dx2, double dy2)
    {
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) &&
            (srcData.getTransparency() == Transparency.OPAQUE))
        {
            comp = CompositeType.SrcNoEa;
        }

        ScaledBlit blit = ScaledBlit.getFromCache(srcType, comp, dstType);
        if (blit != null) {
            blit.Scale(srcData, dstData, sg.composite, clipRegion,
                       sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
            return true;
        }
        return false;
    }

    protected static boolean imageReady(ToolkitImage sunimg,
                                        ImageObserver observer)
    {
        if (sunimg.hasError()) {
            if (observer != null) {
                observer.imageUpdate(sunimg,
                                     ImageObserver.ERROR|ImageObserver.ABORT,
                                     -1, -1, -1, -1);
            }
            return false;
        }
        return true;
    }

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor,
                             ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return copyImage(sg, img, x, y, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, x, y, bgColor, observer);
        }
    }

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor,
                             ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return copyImage(sg, img, dx, dy, sx, sy, w, h, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg,
                                     dx, dy, (dx + w), (dy + h),
                                     sx, sy, (sx + w), (sy + h),
                                     bgColor, observer);
        }
    }

    public boolean scaleImage(SunGraphics2D sg, Image img,
                                int x, int y,
                                int width, int height,
                                Color bgColor,
                                ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return scaleImage(sg, img, x, y, width, height, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, x, y, width, height, bgColor,
                                     observer);
        }
    }

    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor,
                              ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return scaleImage(sg, img, dx1, dy1, dx2, dy2,
                              sx1, sy1, sx2, sy2, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, dx1, dy1, dx2, dy2,
                                     sx1, sy1, sx2, sy2, bgColor, observer);
        }
    }

    public boolean transformImage(SunGraphics2D sg, Image img,
                                  AffineTransform atfm,
                                  ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            transformImage(sg, img, 0, 0, atfm, sg.interpolationType);
            return true;
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, atfm, observer);
        }
    }

    public void transformImage(SunGraphics2D sg, BufferedImage img,
                               BufferedImageOp op, int x, int y)
    {
        if (op != null) {
            if (op instanceof AffineTransformOp) {
                AffineTransformOp atop = (AffineTransformOp) op;
                transformImage(sg, img, x, y,
                               atop.getTransform(),
                               atop.getInterpolationType());
                return;
            } else {
                img = op.filter(img, null);
            }
        }
        copyImage(sg, img, x, y, null);
    }
}
