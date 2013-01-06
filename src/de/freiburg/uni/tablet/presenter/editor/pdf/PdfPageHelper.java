package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public class PdfPageHelper {
	public static AffineTransform calculateTransform(PDPage page) {
		Image img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2 = (Graphics2D) img.getGraphics();
		IGraphicsContext graphics = new CwtAwtGraphicsContext(graphics2);
		CSContent content = page.getContentStream();
		if (content != null) {
			CSPlatformRenderer renderer = new CSPlatformRenderer(null, graphics);
			renderer.process(content, page.getResources());
			System.out.println("translate after: " + graphics.getTransform().getTranslateX() + ", " + graphics.getTransform().getTranslateY());
			System.out.println("scale after: " + graphics.getTransform().getScaleX() + ", " + graphics.getTransform().getScaleY());
			return graphics.getTransform();
		}
		return new AffineTransform();
	}
}
