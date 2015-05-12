package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;
import java.util.Deque;

import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.cos.COSNumber;
import de.intarsys.pdf.pd.PDPage;

public class PdfPageHelper {
	/**
	 * Simulates the document transformation and returns the final transformation.
	 * @param page
	 * @return
	 */
	public static AffineTransform calculateTransform(final PDPage page) {
		CSContent content = page.getContentStream();
		Deque<AffineTransform> transformationStack = new ArrayDeque<AffineTransform>();
		transformationStack.push(new AffineTransform());
		if (content != null) {
			int len = content.size();
			for (int i = 0; i < len; i++) {
				CSOperation operation = content.getOperation(i);
				byte[] token = operation.getOperatorToken();
				if (token.length == 1 && token[0] == 'q') {
					transformationStack.push(transformationStack.peek());
				} else if (token.length == 1 && token[0] == 'Q') {
					transformationStack.pop();
				} else if (token.length == 2 && token[0] == 'c' && token[1] == 'm') {
					float f1 = ((COSNumber) operation.getOperand(0)).floatValue();
					float f2 = ((COSNumber) operation.getOperand(1)).floatValue();
					float f3 = ((COSNumber) operation.getOperand(2)).floatValue();
					float f4 = ((COSNumber) operation.getOperand(3)).floatValue();
					float f5 = ((COSNumber) operation.getOperand(4)).floatValue();
					float f6 = ((COSNumber) operation.getOperand(5)).floatValue();
					AffineTransform transform = new AffineTransform(f1, f2, f3, f4, f5, f6);
					AffineTransform peek = transformationStack.pop();
					transform.preConcatenate(peek);
					transformationStack.push(transform);
				}
			}
			System.out.println("translate after: " + transformationStack.peek().getTranslateX() + ", " + transformationStack.peek().getTranslateY());
			System.out.println("scale after: " + transformationStack.peek().getScaleX() + ", " + transformationStack.peek().getScaleY());
			System.out.println("transformation stack depth: " + transformationStack.size());
			return transformationStack.peek();
		}
		return new AffineTransform();
	}
}
