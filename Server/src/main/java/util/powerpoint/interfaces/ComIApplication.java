package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.IConnectionPoint;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

@ComInterface(iid="{91493442-5A91-11CF-8700-00AA0060263B}")
public interface ComIApplication extends IUnknown, IConnectionPoint {

	@ComProperty
	String getVersion();
	
	@ComProperty
	boolean getVisible();

	@ComProperty
	void setVisible(boolean value);
	
	@ComProperty
	ComIPresentations getPresentations();

	@ComProperty
	ComIPresentation getPresentation();

	@ComProperty
	ComISlideShowWindows getSlideShowWindows();

	@ComProperty
	float getHeight();

	@ComProperty
	float getWidth();

	@ComMethod
	void Activate();
	@ComMethod
	void Quit();
}
