package image_produce;

import com.sun.corba.se.spi.activation.Server;
import util.ServerFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.ArrayList;
import java.util.List;

public class ScreenCapture {
	// Get only one piece of screen.
	public static BufferedImage getScreenCapture() {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Rectangle rec = device.getDefaultConfiguration().getBounds();
		return ServerFactory.getRobot().createScreenCapture(rec);
	}

	public static List<Rectangle> getScreenBounds() {
		List<Rectangle> result = new ArrayList<Rectangle>();
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			result.add(device.getDefaultConfiguration().getBounds());
		}
		return result;
	}

	public static Rectangle getScreenCaptureBound() {
		int width = 0;
		int height = 0;
		int x = 0;
		int y = 0;
		for (Rectangle b : getScreenBounds()) {
			width += b.width;
			height = Math.max(height, b.height);
			x = Math.min(x, b.x);
			y = Math.min(y, b.y);
		}
		return new Rectangle(x, y, width, height);
	}

//	public static BufferedImage getScreenCapture() {
//		return getScreenCapture(getScreenCaptureBound());
//	}

	public static BufferedImage getScreenCapture(Rectangle bounds) {
		return ServerFactory.getRobot().createScreenCapture(bounds);
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

	public static BufferedImage toColorModel(BufferedImage src, int imageType) {
		BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), imageType);
		ColorConvertOp op = new ColorConvertOp(src.getColorModel().getColorSpace(), result.getColorModel().getColorSpace(), null);
		op.filter(src, result);
		return result;
	}
}
