/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.image;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import net.yatomiya.e4.services.image.*;
import net.yatomiya.e4.ui.util.*;

public class ImageUtils {
    private ImageUtils() {
    }

    public static Image[] allocateImages(ImageLoader loader) throws SWTException {
        return allocateImages(loader, -1, -1);
    }

    public static Image[] allocateImages(ImageLoader loader, int newWidth, int newHeight) throws SWTException {
        int width = loader.logicalScreenWidth;
        int height = loader.logicalScreenHeight;
        if (width == 0 || height == 0) {
            width = loader.data[0].width;
            height = loader.data[0].height;
        }

        Image[] images = new Image[loader.data.length];
        Image currentImage = new Image(null, width, height);

        for (int i = 0; i < images.length; i++) {
            ImageData data = loader.data[i];

            // implemented only DM_FILL_NONE
            switch (data.disposalMethod) {
            case SWT.DM_FILL_NONE:
            case SWT.DM_FILL_PREVIOUS:
            case SWT.DM_FILL_BACKGROUND:
            case SWT.DM_UNSPECIFIED:
                break;
            }
                
            Image frameImage = new Image(null, data);

            GC gc = new GC(currentImage);
            gc.drawImage(frameImage, data.x, data.y);
            gc.dispose();

            frameImage.dispose();

            images[i] = new Image(currentImage.getDevice(), currentImage, SWT.IMAGE_COPY);

            if (newWidth > 0 || newHeight > 0) {
                if (newWidth != width || newHeight != height) {
                    Image newImage = scaleImage(images[i], newWidth, newHeight);
                    images[i].dispose();
                    images[i] = newImage;
                }
            }
        }
        currentImage.dispose();

        return images;
    }

    public static void disposeImages(Image[] images) {
        if (images != null) {
            for (Image i : images) {
                if (!i.isDisposed())
                    i.dispose();
            }
        }
    }

    public static Image scaleImage(Image image, int newWidth, int newHeight) {
        Rectangle bounds = image.getBounds();
        Image newImage = new Image(image.getDevice(), newWidth, newHeight);
        GC gc = new GC(newImage);
        gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, newWidth, newHeight);
        gc.dispose();
        return newImage;
    }

    public static Point centeringLocation(Rectangle rect, Point size) {
        return new Point(rect.x + rect.width / 2 - size.x / 2,
                         rect.y + rect.height / 2 - size.y / 2);
    }

    public static Rectangle withMargin(Rectangle rect, int widthMargin, int heightMargin) {
        return new Rectangle(rect.x + widthMargin, rect.y + heightMargin,
                             rect.width - widthMargin - widthMargin, rect.height - heightMargin - heightMargin);
    }

    /**
     * fit size to max size with same aspect rate.
     */
    public static Point fitTo(int width, int height, int dstWidth, int dstHeight) {
        int newWidth, newHeight;

        if (width > height) {
            newWidth = dstWidth;
            newHeight = (int)(height * ((float)dstWidth / width));
        } else {
            newWidth = (int)(width * ((float)dstHeight / height));
            newHeight = dstHeight;
        }
        return new Point(newWidth, newHeight);
    }

    public static Rectangle fitToCenter(Rectangle srcRect, Rectangle dstRect) {
        Point size = fitTo(srcRect.width, srcRect.height, dstRect.width, dstRect.height);
        Point loc = centeringLocation(dstRect, size);
        return new Rectangle(loc.x, loc.y, size.x, size.y);
    }

    public static void drawText(GC gc, String text, Rectangle dst) {
        TextLayout layout = new TextLayout(null);
        layout.setText(text);
        layout.setWidth(dst.width);
        layout.draw(gc, dst.x, dst.y);
        layout.dispose();
    }

    public static void drawMosaic(GC gc, Image img, int factor, Rectangle src, Rectangle dst) {
        int mosWidth = dst.width / factor;
        int mosHeight = dst.height / factor;
        Image mosImg = new Image(null, mosWidth, mosHeight);
        GC mosGc = new GC(mosImg);
        mosGc.drawImage(img, src.x, src.y, src.width, src.height, 0, 0, mosWidth, mosHeight);
        gc.drawImage(mosImg, 0, 0, mosWidth, mosHeight, dst.x, dst.y, dst.width, dst.height);
        mosGc.dispose();
        mosImg.dispose();
    }

    public static void drawImageCacheInformation(GC gc, ImageEntry entry, UIResourceManager resMgr, Rectangle dst) {
        Color textColor = resMgr.getColor(255, 0, 0);
        String msg = "";
        if (entry.isUpdating()) {
            msg = "Loading...";
            textColor = resMgr.getColor(0, 0, 0);
        } else {
            ImageEvent event = entry.getLastUpdateErrorEvent();
            if (event != null) {
                if (event.getType() == ImageEvent.Type.ERROR) {
                    switch (event.getErrorType()) {
                    case IO_EXCEPTION:
                        if (event.getSource() == ImageEvent.Source.STORAGE)
                            msg = "I/O Error";
                        else if (event.getSource() == ImageEvent.Source.NETWORK)
                            msg = "Network error.";
                        break;
                    case HTTP_STATUS_CODE:
                        msg = String.format("Http error(%d)", event.getHttpStatusCode());
                        break;
                    case UNSUPPORTED_IMAGE_FORMAT:
                        msg = "Unsupported image format.";
                        break;
                    }
                }
            }
        }
        gc.setForeground(textColor);
        ImageUtils.drawText(gc, msg, ImageUtils.withMargin(dst, 1, 2));
    }
/*
    public static BufferedImage convertToBufferedImage(ImageData data) {
        ColorModel colorModel = null;
        PaletteData palette = data.palette;
        if (palette.isDirect) {
            colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
            BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    RGB rgb = palette.getRGB(pixel);
                    bufferedImage.setRGB(x, y,  rgb.red << 16 | rgb.green << 8 | rgb.blue);
                }
            }
            return bufferedImage;
        } else {
            RGB[] rgbs = palette.getRGBs();
            byte[] red = new byte[rgbs.length];
            byte[] green = new byte[rgbs.length];
            byte[] blue = new byte[rgbs.length];
            for (int i = 0; i < rgbs.length; i++) {
                RGB rgb = rgbs[i];
                red[i] = (byte)rgb.red;
                green[i] = (byte)rgb.green;
                blue[i] = (byte)rgb.blue;
            }
            if (data.transparentPixel != -1) {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
            } else {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
            }
            BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    pixelArray[0] = pixel;
                    raster.setPixel(x, y, pixelArray);
                }
            }
            return bufferedImage;
        }
    }

    public static ImageData convertToImageData(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                    if (colorModel.hasAlpha()) {
                        data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                    }
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }
*/
/*
    public static BufferedImage scale(BufferedImage img, int width, int height) {
        BufferedImage newImg = new BufferedImage(width, height, img.getType());
        Graphics2D g = (Graphics2D)newImg.getGraphics();
        g.drawImage(img, 0, 0, newImg.getWidth(), newImg.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
        return newImg;
    }


	public static void drawText(Graphics2D g2d, String text, int x, int y, float width) {
		if (text == null || text.length() == 0) {
			return;
		}
		float maxWrapWidth = width - 10;
		Point2D.Float pen = new Point2D.Float(x, y);
		Map<TextAttribute, Font> attributes = new HashMap<TextAttribute, Font>();
		attributes.put(TextAttribute.FONT, g2d.getFont());
		AttributedCharacterIterator textIterator = new AttributedString(text,
				attributes).getIterator();
		FontRenderContext frc = g2d.getFontRenderContext();
		LineBreakMeasurer measurer = new LineBreakMeasurer(textIterator, frc);
		int line = 1;
		while (true) {
			TextLayout layout = measurer.nextLayout(maxWrapWidth);
			if (layout == null)
				break;
			pen.y += layout.getAscent();
			float dx = 0;
			if (layout.isLeftToRight()) {
				dx = (width - layout.getAdvance());
			}
			layout.draw(g2d, pen.x + dx / 2, pen.y);
			pen.y += layout.getDescent() + layout.getLeading() - 1;
			line++;
		}
	}
*/

}
