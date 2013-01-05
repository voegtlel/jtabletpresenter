package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.common.IPaint;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsEnvironment;
import de.intarsys.cwt.image.IImage;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public class PdfPageRedrawer implements IGraphicsContext {
	private Shape _clip = null;
	private Color _backgroundColor = Color.white;
	private Color _foregroundColor = Color.black;
	private RenderingHints _hints = new RenderingHints(new HashMap<Key, Object>());
	private AffineTransform _transform = new AffineTransform();

	public PdfPageRedrawer() {
	}

	@Override
	public void clip(Shape s) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void draw(Shape s) {
	}

	@Override
	public void drawImage(IImage img, float x, float y) {
	}

	@Override
	public void drawString(String str, float x, float y) {
	}

	@Override
	public void fill(Shape s) {
	}

	@Override
	public Color getBackgroundColor() {
		return _backgroundColor;
	}

	@Override
	public Shape getClip() {
		return _clip;
	}

	@Override
	public Color getForegroundColor() {
		return _foregroundColor;
	}

	@Override
	public IGraphicsEnvironment getGraphicsEnvironment() {
		return new IGraphicsEnvironment() {
		};
	}

	@Override
	public RenderingHints getRenderingHints() {
		return _hints;
	}

	@Override
	public AffineTransform getTransform() {
		return _transform;
	}

	@Override
	public void rotate(float theta) {
		_transform.rotate(theta);
	}

	@Override
	public void scale(float sx, float sy) {
		_transform.scale(sx, sy);
	}

	@Override
	public void setBackgroundColor(Color c) {
		_backgroundColor = c;
	}

	@Override
	public void setBackgroundPaint(IPaint p) {
	}

	@Override
	public void setClip(Shape s) {
		_clip = s;
	}

	@Override
	public void setFont(Font f) {
	}

	@Override
	public void setForegroundColor(Color c) {
		_foregroundColor = c;
	}

	@Override
	public void setForegroundPaint(IPaint p) {
	}

	@Override
	public void setRenderingHint(Key k, Object v) {
	}

	@Override
	public void setRenderingHints(Map m) {
	}

	@Override
	public void setStroke(Stroke s) {
		
	}

	@Override
	public void setTransform(AffineTransform t) {
		_transform.setTransform(t);
	}

	@Override
	public void transform(AffineTransform t) {
		_transform.concatenate(t);
	}

	@Override
	public void translate(float tx, float ty) {
		_transform.translate(tx, ty);
	}
	
	public static AffineTransform calculateTransform(PDPage page) {
		PdfPageRedrawer graphics = new PdfPageRedrawer();
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
	
	public static AffineTransform calculateTransform2(PDPage page) {
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
