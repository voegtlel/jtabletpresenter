package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class BitmapHelper {

	public static BufferedImage reduceImage(BufferedImage image, Rectangle newRect) {
		int width = image.getWidth();
		int height = image.getHeight();
		newRect.setBounds(0, 0, width, height);
		int[] pixelData = image.getRGB(0, 0, width, height, null, 0, width);
		// Check left empty columns
		for (int iX = 0; iX < width; iX++) {
			boolean allEmpty = true;
			for (int iY = 0; iY < height; iY++) { 
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.x++;
				newRect.width--;
			} else {
				break;
			}
		}
		if (newRect.width <= 0) {
			return null;
		}
		for (int iX = newRect.width - 1; iX >= newRect.x; iX--) {
			boolean allEmpty = true;
			for (int iY = 0; iY < height; iY++) { 
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.width--;
			} else {
				break;
			}
		}
		for (int iY = 0; iY < height; iY++) {
			boolean allEmpty = true;
			for (int iX = newRect.x; iX < newRect.width; iX++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.y++;
				newRect.height--;
			} else {
				break;
			}
		}
		for (int iY = newRect.height - 1; iY >= newRect.y; iY--) {
			boolean allEmpty = true;
			for (int iX = newRect.x; iX < newRect.width; iX++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.height--;
			} else {
				break;
			}
		}
		if (newRect.x == 0 && newRect.y == 0 && newRect.width == width && newRect.height == height) {
			System.out.println("no reduction");
			return image;
		}
		BufferedImage result = new BufferedImage(newRect.width, newRect.height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = result.getGraphics();
		graphics.drawImage(image, 0, 0, newRect.width, newRect.height, newRect.x, newRect.y, newRect.x + newRect.width, newRect.y + newRect.height, null);
		graphics.dispose();
		System.out.println("Reduced image from " + image.getWidth() + "x" + image.getHeight() + " to " + newRect);
		return result;
	}
}
