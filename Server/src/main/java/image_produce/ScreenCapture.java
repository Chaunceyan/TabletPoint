package image_produce;

import util.ServerFactory;
import util.powerpoint.MSPowerPoint;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ScreenCapture {
	public static int leftPadding = 0, topPadding = 0;
	private static float screenRatio;

	public static float getSlideViewRatio() {
		return slideViewRatio;
	}

	private static float slideViewRatio;

	// Get only one piece of screen.
	public static void init() {
		screenRatio = MSPowerPoint.getSlideShowWindowHeigth()/MSPowerPoint.getSlideShowWindowWidth();
		slideViewRatio = MSPowerPoint.getSlideShowViewHeigth()/MSPowerPoint.getSlideShowViewWidth();
	}

	public static BufferedImage getScreenCapture() {
		Rectangle rec2 = getScreenCaptureBound();
		System.out.println(rec2.x + "," + rec2.y + "," + rec2.width + "," + rec2.height );
		// Or change this
		BufferedImage screenCapture = ServerFactory.getRobot().createScreenCapture(rec2);
		return screenCapture;
	}

	public static Rectangle getScreenCaptureBound() {
		int width;
		int height;
		int x = 0;
		int y = 0;
		// Change this
        Rectangle b = getScreenBounds().get(0);
        width = b.width;
        height = b.height;
		return cropImage(x, y, width, height);
	}

	public static List<Rectangle> getScreenBounds() {
		List<Rectangle> result = new ArrayList<Rectangle>();
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			result.add(device.getDefaultConfiguration().getBounds());
		}
		return result;
	}

	private static Rectangle cropImage(int x, int y, int width, int height) {
		int mX ,mY, mWidth, mHeight;
		if (screenRatio > slideViewRatio) {
		 	mY = Math.round ((1 - slideViewRatio / screenRatio) * height / 2 + y);
			mHeight = Math.round(slideViewRatio / screenRatio * height);
			topPadding = mY;
			leftPadding = 0;
			System.out.println("mY: " + mY + "mHeight" + mHeight);
			return new Rectangle(x, mY, width, mHeight);
		} else {
			mX = Math.round((1 - screenRatio / slideViewRatio) * width / 2 + x);
			leftPadding = mX;
			topPadding = 0;
			mWidth = Math.round(screenRatio / slideViewRatio * width);
			return new Rectangle(mX, y, mWidth, height);
		}
	}

	// Did nothing rather than resize image to the specified size and returned.
	public static BufferedImage resizeImage(BufferedImage image, int width, int height, int imageType) {
		BufferedImage result = new BufferedImage(width, height, imageType);
		Graphics2D graphics2D = result.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.dispose();
		return result;
	}
}
