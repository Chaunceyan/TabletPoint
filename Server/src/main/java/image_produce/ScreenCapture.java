package image_produce;

import com.sun.corba.se.spi.activation.Server;
import util.ServerFactory;
import util.powerpoint.MSPowerPoint;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class ScreenCapture {
	// Get only one piece of screen.
	public static BufferedImage getScreenCapture() {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Rectangle rec2 = getScreenCaptureBound();
		System.out.println(rec2.x + "," + rec2.y + "," + rec2.width + "," + rec2.height );
		BufferedImage screenCapture = ServerFactory.getRobot().createScreenCapture(rec2);
		return screenCapture;
	}

	public static Rectangle getScreenCaptureBound() {
		int width = 0;
		int height = 0;
		int x = 0;
		int y = 0;
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
		float screenRatio = MSPowerPoint.getSlideShowWindowWidth()/MSPowerPoint.getSlideShowWindowHeigth();
		float slideViewRatio = MSPowerPoint.getSlideShowViewWidth()/MSPowerPoint.getSlideShowViewHeigth();
		int mX ,mY, mWidth, mHeight;
		if (screenRatio > slideViewRatio) {
		 	mX = Math.round ((1 - slideViewRatio / screenRatio) * width / 2 + x);
			mWidth = Math.round(slideViewRatio / screenRatio * width);
			return new Rectangle(mX, y, mWidth, height);
		} else {
			mY = Math.round((1 - screenRatio / slideViewRatio) * height / 2 + x);
			mHeight = Math.round(screenRatio / slideViewRatio * height);
			return new Rectangle(x, mY, width, mHeight);
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
