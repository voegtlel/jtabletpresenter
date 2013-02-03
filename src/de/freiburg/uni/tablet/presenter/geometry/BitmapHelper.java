package de.freiburg.uni.tablet.presenter.geometry;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class BitmapHelper {

	public static Bitmap reduceImage(Bitmap image, Rect newRect) {
		int width = image.getWidth();
		int height = image.getHeight();
		newRect.set(0, 0, width, height);
		final int[] pixelData = new int[image.getWidth() * image.getHeight()];
		image.getPixels(pixelData, 0, width, 0, 0, width, height);
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
				newRect.left++;
			} else {
				break;
			}
		}
		if (newRect.left >= newRect.right) {
			return null;
		}
		for (int iX = newRect.right - 1; iX >= newRect.left; iX--) {
			boolean allEmpty = true;
			for (int iY = 0; iY < height; iY++) { 
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.right--;
			} else {
				break;
			}
		}
		for (int iY = 0; iY < height; iY++) {
			boolean allEmpty = true;
			for (int iX = newRect.left; iX < newRect.right; iX++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.top++;
			} else {
				break;
			}
		}
		for (int iY = newRect.bottom - 1; iY >= newRect.top; iY--) {
			boolean allEmpty = true;
			for (int iX = newRect.left; iX < newRect.right; iX++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				newRect.bottom--;
			} else {
				break;
			}
		}
		if (newRect.left == 0 && newRect.top == 0 && newRect.right == width && newRect.bottom == height) {
			System.out.println("no reduction");
			return image;
		}
		Bitmap result = Bitmap.createBitmap(newRect.right - newRect.left, newRect.bottom - newRect.top, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(result);
		c.drawBitmap(image, newRect, new Rect(0, 0, newRect.right - newRect.left, newRect.bottom - newRect.top), null);
		System.out.println("Reduced image from " + image.getWidth() + "x" + image.getHeight() + " to " + newRect);
		return result;
	}
}
