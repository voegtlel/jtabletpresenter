/**
 *  Page.java
 *
Copyright (c) 2012, Innovatics Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and / or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.pdfjet;

import java.io.*;
import java.util.*;


/**
 *  Used to create PDF page objects.
 *
 *  Please note:
 *  <pre>
 *  The coordinate (0.0f, 0.0f) is the top left corner of the page.
 *  The size of the pages are represented in points.
 *  1 point is 1/72 inches.
 *  </pre>
 *
 */
public class Page {

    protected PDF pdf;
    protected ByteArrayOutputStream buf;
    protected float[] tm = new float[] {1f, 0f, 0f, 1f};
    protected int renderingMode = 0;
    protected float width;
    protected float height;
    protected List<Annotation> annots;
    protected List<Destination> destinations;

    private float[] pen = {0.0f, 0.0f, 0.0f};
    private float[] brush = {0.0f, 0.0f, 0.0f};
    private float pen_width = -1.0f;
    private int line_cap_style = 0;
    private int line_join_style = 0;
    private String linePattern = "[] 0";


    /**
     *  Creates page object and add it to the PDF document.
     *
     *  Please note:
     *  <pre>
     *  The coordinate (0.0, 0.0) is the top left corner of the page.
     *  The size of the pages are represented in points.
     *  1 point is 1/72 inches.
     *  </pre>
     *
     *  @param pdf the pdf object.
     *  @param pageSize the page size of this page.
     */
    public Page(PDF pdf, float[] pageSize) throws Exception {
        this.pdf = pdf;
        annots = new ArrayList<Annotation>();
        destinations = new ArrayList<Destination>();
        width = pageSize[0];
        height = pageSize[1];
        buf = new ByteArrayOutputStream(8192);

        int n = pdf.pages.size();
        if (n > 0) {
            Page page = pdf.pages.get(n - 1);
            pdf.addPageContent(page);
        }

        pdf.pages.add(this);
    }


    /**
     *  Adds destination to this page.
     *
     *  @param name The destination name.
     *  @param yPosition The vertical position of the destination on this page.
     */
    public void addDestination(String name, double yPosition) {
        destinations.add(new Destination(name, height - yPosition));
    }


    protected void setDestinationsPageObjNumber(int pageObjNumber) {
        for (Destination destination : destinations) {
            destination.setPageObjNumber(pageObjNumber);
            this.pdf.destinations.put(destination.name, destination);
        }
    }

    
    /**
     *  Returns the width of this page.
     *
     *  @return the width of the page.
     */
    public float getWidth() {
        return width;
    }


    /**
     *  Returns the height of this page.
     *
     *  @return the height of the page.
     */
    public float getHeight() {
        return height;
    }


    /**
     *  Draws a line on the page, using the current color, between the points (x1, y1) and (x2, y2).
     *
     *  @param x1 the first point's x coordinate.
     *  @param y1 the first point's y coordinate.
     *  @param x2 the second point's x coordinate.
     *  @param y2 the second point's y coordinate.
     */
    public void drawLine(
            double x1,
            double y1,
            double x2,
            double y2) throws IOException {
        drawLine((float) x1, (float) y1, (float) x2, (float) y2);
    }


    /**
     *  Draws a line on the page, using the current color, between the points (x1, y1) and (x2, y2).
     *
     *  @param x1 the first point's x coordinate.
     *  @param y1 the first point's y coordinate.
     *  @param x2 the second point's x coordinate.
     *  @param y2 the second point's y coordinate.
     */
    public void drawLine(
            float x1,
            float y1,
            float x2,
            float y2) throws IOException {
        moveTo(x1, y1);
        lineTo(x2, y2);
        strokePath();
    }


    /**
     *  Draws the text given by the specified string,
     *  using the specified main font and the current brush color.
     *  If the main font is missing some glyphs - the fallback font is used.
     *  The baseline of the leftmost character is at position (x, y) on the page.
     *
     *  @param font1 the main font.
     *  @param font2 the fallback font.
     *  @param str the string to be drawn.
     *  @param x the x coordinate.
     *  @param y the y coordinate.
     */
    public void drawString(
            Font font1,
            Font font2,
            String str,
            float x,
            float y) throws IOException {
        boolean usingFont1 = true;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            if (font1.charSet.contains(ch)) {
                if (!usingFont1) {
                    String s1 = buf.toString();
                    drawString(font2, s1, x, y);
                    x += font2.stringWidth(s1);
                    buf = new StringBuilder();
                    usingFont1 = true;
                }
            }
            else {
                if (usingFont1) {
                    String s1 = buf.toString();
                    drawString(font1, s1, x, y);
                    x += font1.stringWidth(s1);
                    buf = new StringBuilder();
                    usingFont1 = false;
                }
            }
            buf.append((char) ch);
        }

        if (usingFont1) {
            drawString(font1, buf.toString(), x, y);
        }
        else {
            drawString(font2, buf.toString(), x, y);
        }
    }


    /**
     * Returns the width of a string drawn using two fonts.
     * 
     * @param font1 the main font.
     * @param font2 the fallback font.
     * @param str the string.
     * @return the width.
     */
    public float stringWidth(Font font1, Font font2, String str) {
        float width = 0.0f;

        boolean usingFont1 = true;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            if (font1.charSet.contains(ch)) {
                if (!usingFont1) {
                    width += font2.stringWidth(buf.toString());
                    buf = new StringBuilder();
                    usingFont1 = true;
                }
            }
            else {
                if (usingFont1) {
                    width += font1.stringWidth(buf.toString());
                    buf = new StringBuilder();
                    usingFont1 = false;
                }
            }
            buf.append((char) ch);
        }

        if (usingFont1) {
            width += font1.stringWidth(buf.toString());
        }
        else {
            width += font2.stringWidth(buf.toString());
        }

        return width;
    }


    /**
     *  Draws the text given by the specified string,
     *  using the specified font and the current brush color.
     *  The baseline of the leftmost character is at position (x, y) on the page.
     *
     *  @param font the font to use.
     *  @param str the string to be drawn.
     *  @param x the x coordinate.
     *  @param y the y coordinate.
     */
    public void drawString(
            Font font,
            String str,
            double x,
            double y) throws IOException {
        drawString(font, str, (float) x, (float) y);
    }


    /**
     *  Draws the text given by the specified string,
     *  using the specified font and the current brush color.
     *  The baseline of the leftmost character is at position (x, y) on the page.
     *
     *  @param font the font to use.
     *  @param str the string to be drawn.
     *  @param x the x coordinate.
     *  @param y the y coordinate.
     */
    public void drawString(
            Font font,
            String str,
            float x,
            float y) throws IOException {

        if (str == null || str.equals("")) {
            return;
        }

        append("q\n");  // Save the graphics state
        append("BT\n");

        setTextFont(font);

        if (renderingMode != 0) {
            append(renderingMode);
            append(" Tr\n");
        }

        float skew = 0f;
        if (font.skew15 &&
                tm[0] == 1f &&
                tm[1] == 0f &&
                tm[2] == 0f &&
                tm[3] == 1f) {
            skew = 0.26f;
        }

        append(tm[0]);
        append(' ');
        append(tm[1]);
        append(' ');
        append(tm[2] + skew);
        append(' ');
        append(tm[3]);
        append(' ');
        append(x);
        append(' ');
        append(height - y);
        append(" cm\n");

        append("[ (");
        drawString(font, str);
        append(") ] TJ\n");
        append("ET\n");

        append("Q\n");  // Restore the graphics state
    }
    

    private void drawString(Font font, String str) throws IOException {
        for (int i = 0; i < str.length(); i++) {
            int c1 = str.charAt(i);
            if (font.isComposite) {
                drawTwoByteChar(c1, font);
            }
            else {
                drawOneByteChar(c1, font, str, i);
            }
        }
    }


    private void drawTwoByteChar(int c1, Font font) throws IOException {
        if (c1 < font.firstChar || c1 > font.lastChar) {
            append((byte) 0x0000);
            append((byte) 0x0020);
        }
        else {
            byte hi = (byte) (c1 >> 8);
            byte lo = (byte) (c1);
            if (hi == '(' || hi == ')' || hi == '\\') {
                append((byte) '\\');
            }
            append(hi);
    
            if (lo == '\r') {
                append("\\015");
            }
            else {
                if (lo == '(' || lo == ')' || lo == '\\') {
                    append((byte) '\\');
                }
                append(lo);
            }
        }
    }


    private void drawOneByteChar(int c1, Font font, String str, int i) throws IOException {
        if (c1 < font.firstChar || c1 > font.lastChar) {
            c1 = font.mapUnicodeChar(c1);
        }
        if (c1 == '(' || c1 == ')' || c1 == '\\') {
            append((byte) '\\');
        }
        append((byte) c1);

        if (font.isStandard && font.kernPairs && i < (str.length() - 1)) {
            c1 -= 32;
            int c2 = str.charAt(i + 1);
            if (c2 < font.firstChar || c2 > font.lastChar) {
                c2 = 32;
            }
            for (int j = 2; j < font.metrics[c1].length; j += 2) {
                if (font.metrics[c1][j] == c2) {
                    append(") ");
                    append(-font.metrics[c1][j + 1]);
                    append(" (");
                    break;
                }
            }
        }
    }


    /**
     * Sets the color for stroking operations.
     * The pen color is used when drawing lines and splines.
     *
     * @param r the red component is float value from 0.0 to 1.0.
     * @param g the green component is float value from 0.0 to 1.0.
     * @param b the blue component is float value from 0.0 to 1.0.
     */
    public void setPenColor(
            double r, double g, double b) throws IOException {
        setPenColor((float) r, (float) g, (float) b);
    }


    /**
     * Sets the color for stroking operations.
     * The pen color is used when drawing lines and splines.
     *
     * @param r the red component is float value from 0.0f to 1.0f.
     * @param g the green component is float value from 0.0f to 1.0f.
     * @param b the blue component is float value from 0.0f to 1.0f.
     */
    public void setPenColor(
            float r, float g, float b) throws IOException {
        if (pen[0] != r ||
                pen[1] != g ||
                pen[2] != b) {
            setColor(r, g, b);
            append(" RG\n");
            pen[0] = r;
            pen[1] = g;
            pen[2] = b;
        }
    }


    /**
     * Sets the color for brush operations.
     * This is the color used when drawing regular text and filling shapes.
     *
     * @param r the red component is float value from 0.0 to 1.0.
     * @param g the green component is float value from 0.0 to 1.0.
     * @param b the blue component is float value from 0.0 to 1.0.
     */
    public void setBrushColor(
            double r, double g, double b) throws IOException {
        setBrushColor((float) r, (float) g, (float) b);
    }


    /**
     * Sets the color for brush operations.
     * This is the color used when drawing regular text and filling shapes.
     *
     * @param r the red component is float value from 0.0f to 1.0f.
     * @param g the green component is float value from 0.0f to 1.0f.
     * @param b the blue component is float value from 0.0f to 1.0f.
     */
    public void setBrushColor(
            float r, float g, float b) throws IOException {
        if (brush[0] != r ||
                brush[1] != g ||
                brush[2] != b) {
            setColor(r, g, b);
            append(" rg\n");
            brush[0] = r;
            brush[1] = g;
            brush[2] = b;
        }
    }


    /**
     * Sets the color for brush operations.
     * 
     * @param color the color.
     * @throws IOException
     */
    public void setBrushColor(float[] color) throws IOException {
        setBrushColor(color[0], color[1], color[2]);
    }


    /**
     * Returns the brush color.
     * 
     * @return the brush color.
     */
    public float[] getBrushColor() {
        return brush;
    }


    private void setColor(
            float r, float g, float b) throws IOException {
        append(r);
        append(' ');
        append(g);
        append(' ');
        append(b);
    }


    /**
     * Sets the pen color.
     * 
     * @param color the color. See the Color class for predefined values or define your own using 0x00RRGGBB packed integers.
     * @throws IOException
     */
    public void setPenColor(int color) throws IOException {
        float r = ((color >> 16) & 0xff)/255.0f;
        float g = ((color >>  8) & 0xff)/255.0f;
        float b = ((color)       & 0xff)/255.0f;
        setPenColor(r, g, b);
    }


    /**
     * Sets the pen color.
     * @deprecated  As of v4.00 replaced by {@link #setPenColor(int)}
     * 
     * @param color
     * @throws IOException
     */
    @Deprecated
    public void setPenColor(int[] color) throws IOException {
        float r = ((color[0] >> 16) & 0xff)/255.0f;
        float g = ((color[1] >>  8) & 0xff)/255.0f;
        float b = ((color[2])       & 0xff)/255.0f;
        setPenColor(r, g, b);
    }


    /**
     * Sets the brush color.
     * 
     * @param color the color. See the Color class for predefined values or define your own using 0x00RRGGBB packed integers.
     * @throws IOException
     */
    public void setBrushColor(int color) throws IOException {
        float r = ((color >> 16) & 0xff)/255.0f;
        float g = ((color >>  8) & 0xff)/255.0f;
        float b = ((color)       & 0xff)/255.0f;
        setBrushColor(r, g, b);
    }


    /**
     * Sets the pen color.
     * @deprecated  As of v4.00 replaced by {@link #setBrushColor(int)}
     * 
     * @param color
     * @throws IOException
     */
    @Deprecated
    public void setBrushColor(int[] color) throws IOException {
        float r = ((color[0] >> 16) & 0xff)/255.0f;
        float g = ((color[1] >>  8) & 0xff)/255.0f;
        float b = ((color[2])       & 0xff)/255.0f;
        setBrushColor(r, g, b);
    }


    /**
     *  Sets the line width to the default.
     *  The default is the finest line width.
     */
    public void setDefaultLineWidth() throws IOException {
        append(0.0f);
        append(" w\n");
    }


    /**
     *  The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
     *  It is specified by a dash array and a dash phase.
     *  The elements of the dash array are positive numbers that specify the lengths of
     *  alternating dashes and gaps.
     *  The dash phase specifies the distance into the dash pattern at which to start the dash.
     *  The elements of both the dash array and the dash phase are expressed in user space units.
     *  <pre>
     *  Examples of line dash patterns:
     *
     *      "[Array] Phase"     Appearance          Description
     *      _______________     _________________   ____________________________________
     *
     *      "[] 0"              -----------------   Solid line
     *      "[3] 0"             ---   ---   ---     3 units on, 3 units off, ...
     *      "[2] 1"             -  --  --  --  --   1 on, 2 off, 2 on, 2 off, ...
     *      "[2 1] 0"           -- -- -- -- -- --   2 on, 1 off, 2 on, 1 off, ...
     *      "[3 5] 6"             ---     ---       2 off, 3 on, 5 off, 3 on, 5 off, ...
     *      "[2 3] 11"          -   --   --   --    1 on, 3 off, 2 on, 3 off, 2 on, ...
     *  </pre>
     *
     *  @param pattern the line dash pattern.
     */
    public void setLinePattern(String pattern) throws IOException {
        if (!pattern.equals(linePattern)) {
            linePattern = pattern;
            append(linePattern);
            append(" d\n");
        }
    }


    /**
     *  Sets the default line dash pattern - solid line.
     */
    public void setDefaultLinePattern() throws IOException {
        append("[] 0");
        append(" d\n");
    }


    /**
     *  Sets the pen width that will be used to draw lines and splines on this page.
     *
     *  @param width the pen width.
     */
    public void setPenWidth(double width) throws IOException {
        setPenWidth((float) width);
    }


    /**
     *  Sets the pen width that will be used to draw lines and splines on this page.
     *
     *  @param width the pen width.
     */
    public void setPenWidth(float width) throws IOException {
        if (pen_width != width) {
            pen_width = width;
            append(pen_width);
            append(" w\n");
        }
    }


    /**
     *  Sets the current line cap style.
     *
     *  @param style the cap style of the current line. Supported values: Cap.BUTT, Cap.ROUND and Cap.PROJECTING_SQUARE
     */
    public void setLineCapStyle(int style) throws IOException {
        if (line_cap_style != style) {
            line_cap_style = style;
            append(line_cap_style);
            append(" J\n");
        }
    }


    /**
     *  Sets the line join style.
     *
     *  @param style the line join style code. Supported values: Join.MITER, Join.ROUND and Join.BEVEL
     */
    public void setLineJoinStyle(int style) throws IOException {
        if (line_join_style != style) {
            line_join_style = style;
            append(line_join_style);
            append(" j\n");
        }
    }


    /**
     *  Moves the pen to the point with coordinates (x, y) on the page.
     *
     *  @param x the x coordinate of new pen position.
     *  @param y the y coordinate of new pen position.
     */
    public void moveTo(double x, double y) throws IOException {
        moveTo((float) x, (float) y);
    }


    /**
     *  Moves the pen to the point with coordinates (x, y) on the page.
     *
     *  @param x the x coordinate of new pen position.
     *  @param y the y coordinate of new pen position.
     */
    public void moveTo(float x, float y) throws IOException {
        append(x);
        append(' ');
        append(height - y);
        append(" m\n");
    }


    /**
     *  Draws a line from the current pen position to the point with coordinates (x, y),
     *  using the current pen width and stroke color.
     *  Make sure you call strokePath(), closePath() or fillPath() after the last call to this method.
     */
    public void lineTo(double x, double y) throws IOException {
        lineTo((float) x, (float) y);
    }


    /**
     *  Draws a line from the current pen position to the point with coordinates (x, y),
     *  using the current pen width and stroke color.
     *  Make sure you call strokePath(), closePath() or fillPath() after the last call to this method.
     */
    public void lineTo(float x, float y) throws IOException {
        append(x);
        append(' ');
        append(height - y);
        append(" l\n");
    }


    /**
     *  Draws the path using the current pen color.
     */
    public void strokePath() throws IOException {
        append("S\n");
    }


    /**
     *  Closes the path and draws it using the current pen color.
     */
    public void closePath() throws IOException {
        append("s\n");
    }


    /**
     *  Closes and fills the path with the current brush color.
     */
    public void fillPath() throws IOException {
        append("f\n");
    }


    /**
     *  Draws the outline of the specified rectangle on the page.
     *  The left and right edges of the rectangle are at x and x + w.
     *  The top and bottom edges are at y and y + h.
     *  The rectangle is drawn using the current pen color.
     *
     *  @param x the x coordinate of the rectangle to be drawn.
     *  @param y the y coordinate of the rectangle to be drawn.
     *  @param w the width of the rectangle to be drawn.
     *  @param h the height of the rectangle to be drawn.
     */
    public void drawRect(double x, double y, double w, double h)
            throws IOException {
        drawRect((float) x, (float) y, (float) w, (float) h);
    }


    /**
     *  Draws the outline of the specified rectangle on the page.
     *  The left and right edges of the rectangle are at x and x + w.
     *  The top and bottom edges are at y and y + h.
     *  The rectangle is drawn using the current pen color.
     *
     *  @param x the x coordinate of the rectangle to be drawn.
     *  @param y the y coordinate of the rectangle to be drawn.
     *  @param w the width of the rectangle to be drawn.
     *  @param h the height of the rectangle to be drawn.
     */
    public void drawRect(float x, float y, float w, float h)
            throws IOException {
        moveTo(x, y);
        lineTo(x+w, y);
        lineTo(x+w, y+h);
        lineTo(x, y+h);
        closePath();
    }


    /**
     *  Fills the specified rectangle on the page.
     *  The left and right edges of the rectangle are at x and x + w.
     *  The top and bottom edges are at y and y + h.
     *  The rectangle is drawn using the current pen color.
     *
     *  @param x the x coordinate of the rectangle to be drawn.
     *  @param y the y coordinate of the rectangle to be drawn.
     *  @param w the width of the rectangle to be drawn.
     *  @param h the height of the rectangle to be drawn.
     */
    public void fillRect(double x, double y, double w, double h)
            throws IOException {
        fillRect((float) x, (float) y, (float) w, (float) h);
    }


    /**
     *  Fills the specified rectangle on the page.
     *  The left and right edges of the rectangle are at x and x + w.
     *  The top and bottom edges are at y and y + h.
     *  The rectangle is drawn using the current pen color.
     *
     *  @param x the x coordinate of the rectangle to be drawn.
     *  @param y the y coordinate of the rectangle to be drawn.
     *  @param w the width of the rectangle to be drawn.
     *  @param h the height of the rectangle to be drawn.
     */
    public void fillRect(float x, float y, float w, float h)
            throws IOException {
        moveTo(x, y);
        lineTo(x+w, y);
        lineTo(x+w, y+h);
        lineTo(x, y+h);
        fillPath();
    }


    /**
     *  Draws or fills the specified path using the current pen or brush.
     *
     *  @param path the path.
     *  @param operation specifies 'stroke' or 'fill' operation.
     */
    public void drawPath(
            List<Point> path, char operation) throws Exception {
        if (path.size() < 2) {
            throw new Exception(
                    "The Path object must contain at least 2 points");
        }
        Point point = path.get(0);
        moveTo(point.x, point.y);
        boolean curve = false;
        for (int i = 1; i < path.size(); i++) {
            point = path.get(i);
            if (point.isControlPoint) {
                curve = true;
                append(point);
            }
            else {
                if (curve) {
                    curve = false;
                    append(point);
                    append("c\n");
                }
                else {
                    lineTo(point.x, point.y);
                }
            }
        }

        append(operation);
        append('\n');
    }


    /**
     * Strokes a bezier curve and draws it using the current pen.
     * @deprecated  As of v4.00 replaced by {@link #drawPath(List, char)}
     *
     * @param list the list of points that define the bezier curve.
     */
    @Deprecated
    public void drawBezierCurve(List<Point> list) throws IOException {
        drawBezierCurve(list, Operation.STROKE);
    }
    

    /**
     * Draws a bezier curve and fills it using the current brush.
     * @deprecated  As of v4.00 replaced by {@link #drawPath(List, char)}
     *
     * @param list the list of points that define the bezier curve.
     * @param operation must be Operation.STROKE or Operation.FILL.
     */
    @Deprecated
    public void drawBezierCurve(
            List<Point> list, char operation) throws IOException {
        Point point = list.get(0);
        moveTo(point.x, point.y);
        for (int i = 1; i < list.size(); i++) {
            point = list.get(i);
            append(point);
            if (i % 3 == 0) {
                append("c\n");
            }
        }

        append(operation);
        append('\n');
    }


    /**
     *  Draws a circle on the page.
     *
     *  The outline of the circle is drawn using the current pen color.
     *
     *  @param x the x coordinate of the center of the circle to be drawn.
     *  @param y the y coordinate of the center of the circle to be drawn.
     *  @param r the radius of the circle to be drawn.
     */
    public void drawCircle(
            double x,
            double y,
            double r) throws Exception {
        drawEllipse((float) x, (float) y, (float) r, (float) r, Operation.STROKE);
    }


    /**
     *  Draws a circle on the page.
     *
     *  The outline of the circle is drawn using the current pen color.
     *
     *  @param x the x coordinate of the center of the circle to be drawn.
     *  @param y the y coordinate of the center of the circle to be drawn.
     *  @param r the radius of the circle to be drawn.
     */
    public void drawCircle(
            float x,
            float y,
            float r) throws Exception {
        drawEllipse(x, y, r, r, Operation.STROKE);
    }


    /**
     *  Draws the specified circle on the page and fills it with the current brush color.
     *
     *  @param x the x coordinate of the center of the circle to be drawn.
     *  @param y the y coordinate of the center of the circle to be drawn.
     *  @param r the radius of the circle to be drawn.
     *  @param operation must be Operation.STROKE, Operation.CLOSE or Operation.FILL.
     */
    public void drawCircle(
            double x,
            double y,
            double r,
            char operation) throws Exception {
        drawEllipse((float) x, (float) y, (float) r, (float) r, operation);
    }


    /**
     *  Draws an ellipse on the page using the current pen color.
     *
     *  @param x the x coordinate of the center of the ellipse to be drawn.
     *  @param y the y coordinate of the center of the ellipse to be drawn.
     *  @param r1 the horizontal radius of the ellipse to be drawn.
     *  @param r2 the vertical radius of the ellipse to be drawn.
     */
    public void drawEllipse(
            double x,
            double y,
            double r1,
            double r2) throws Exception {
        drawEllipse((float) x, (float) y, (float) r1, (float) r2, Operation.STROKE);
    }


    /**
     *  Draws an ellipse on the page using the current pen color.
     *
     *  @param x the x coordinate of the center of the ellipse to be drawn.
     *  @param y the y coordinate of the center of the ellipse to be drawn.
     *  @param r1 the horizontal radius of the ellipse to be drawn.
     *  @param r2 the vertical radius of the ellipse to be drawn.
     */
    public void drawEllipse(
            float x,
            float y,
            float r1,
            float r2) throws Exception {
        drawEllipse(x, y, r1, r2, Operation.STROKE);
    }


    /**
     *  Fills an ellipse on the page using the current pen color.
     *
     *  @param x the x coordinate of the center of the ellipse to be drawn.
     *  @param y the y coordinate of the center of the ellipse to be drawn.
     *  @param r1 the horizontal radius of the ellipse to be drawn.
     *  @param r2 the vertical radius of the ellipse to be drawn.
     */
    public void fillEllipse(
            double x,
            double y,
            double r1,
            double r2) throws Exception {
        drawEllipse((float) x, (float) y, (float) r1, (float) r2, Operation.FILL);
    }


    /**
     *  Fills an ellipse on the page using the current pen color.
     *
     *  @param x the x coordinate of the center of the ellipse to be drawn.
     *  @param y the y coordinate of the center of the ellipse to be drawn.
     *  @param r1 the horizontal radius of the ellipse to be drawn.
     *  @param r2 the vertical radius of the ellipse to be drawn.
     */
    public void fillEllipse(
            float x,
            float y,
            float r1,
            float r2) throws Exception {
        drawEllipse(x, y, r1, r2, Operation.FILL);
    }


    /**
     *  Draws an ellipse on the page and fills it using the current brush color.
     *
     *  @param x the x coordinate of the center of the ellipse to be drawn.
     *  @param y the y coordinate of the center of the ellipse to be drawn.
     *  @param r1 the horizontal radius of the ellipse to be drawn.
     *  @param r2 the vertical radius of the ellipse to be drawn.
     *  @param operation the operation.
     */
    private void drawEllipse(
            float x,
            float y,
            float r1,
            float r2,
            char operation) throws Exception {
        // The best 4-spline magic number
        float m4 = 0.551784f;

        List<Point> list = new ArrayList<Point>();

        // Starting point
        list.add(new Point(x, y - r2));

        list.add(new Point(x + m4*r1, y - r2, Point.CONTROL_POINT));
        list.add(new Point(x + r1, y - m4*r2, Point.CONTROL_POINT));
        list.add(new Point(x + r1, y));

        list.add(new Point(x + r1, y + m4*r2, Point.CONTROL_POINT));
        list.add(new Point(x + m4*r1, y + r2, Point.CONTROL_POINT));
        list.add(new Point(x, y + r2));

        list.add(new Point(x - m4*r1, y + r2, Point.CONTROL_POINT));
        list.add(new Point(x - r1, y + m4*r2, Point.CONTROL_POINT));
        list.add(new Point(x - r1, y));

        list.add(new Point(x - r1, y - m4*r2, Point.CONTROL_POINT));
        list.add(new Point(x - m4*r1, y - r2, Point.CONTROL_POINT));
        list.add(new Point(x, y - r2));

        drawPath(list, operation);
    }

    
    /**
     *  Draws a point on the page using the current pen color.
     *
     *  @param p the point.
     */
    public void drawPoint(Point p) throws Exception {
        if (p.shape != Point.INVISIBLE)  {
            List<Point> list;
            if (p.shape == Point.CIRCLE) {
                if (p.fillShape) {
                    drawCircle(p.x, p.y, p.r, 'f');
                }
                else {
                    drawCircle(p.x, p.y, p.r, 'S');
                }
            }
            else if (p.shape == Point.DIAMOND) {
                list = new ArrayList<Point>();
                list.add(new Point(p.x, p.y - p.r));
                list.add(new Point(p.x + p.r, p.y));
                list.add(new Point(p.x, p.y + p.r));
                list.add(new Point(p.x - p.r, p.y));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
            else if (p.shape == Point.BOX) {
                list = new ArrayList<Point>();
                list.add(new Point(p.x - p.r, p.y - p.r));
                list.add(new Point(p.x + p.r, p.y - p.r));
                list.add(new Point(p.x + p.r, p.y + p.r));
                list.add(new Point(p.x - p.r, p.y + p.r));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
            else if (p.shape == Point.PLUS) {
                drawLine(p.x - p.r, p.y, p.x + p.r, p.y);
                drawLine(p.x, p.y - p.r, p.x, p.y + p.r);
            }
            else if (p.shape == Point.UP_ARROW) {
                list = new ArrayList<Point>();
                list.add(new Point(p.x, p.y - p.r));
                list.add(new Point(p.x + p.r, p.y + p.r));
                list.add(new Point(p.x - p.r, p.y + p.r));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
            else if (p.shape == Point.DOWN_ARROW) {
                list = new ArrayList<Point>();
                list.add(new Point(p.x - p.r, p.y - p.r));
                list.add(new Point(p.x + p.r, p.y - p.r));
                list.add(new Point(p.x, p.y + p.r));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
            else if (p.shape == Point.LEFT_ARROW) {
                list = new ArrayList<Point>();
                list.add(new Point(p.x + p.r, p.y + p.r));
                list.add(new Point(p.x - p.r, p.y));
                list.add(new Point(p.x + p.r, p.y - p.r));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
            else if (p.shape == Point.RIGHT_ARROW) {
                list = new ArrayList<Point>();
                list.add(new Point(p.x - p.r, p.y - p.r));
                list.add(new Point(p.x + p.r, p.y));
                list.add(new Point(p.x - p.r, p.y + p.r));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
            else if (p.shape == Point.H_DASH) {
                drawLine(p.x - p.r, p.y, p.x + p.r, p.y);
            }
            else if (p.shape == Point.V_DASH) {
                drawLine(p.x, p.y - p.r, p.x, p.y + p.r);
            }
            else if (p.shape == Point.X_MARK) {
                drawLine(p.x - p.r, p.y - p.r, p.x + p.r, p.y + p.r);
                drawLine(p.x - p.r, p.y + p.r, p.x + p.r, p.y - p.r);
            }
            else if (p.shape == Point.MULTIPLY) {
                drawLine(p.x - p.r, p.y - p.r, p.x + p.r, p.y + p.r);
                drawLine(p.x - p.r, p.y + p.r, p.x + p.r, p.y - p.r);
                drawLine(p.x - p.r, p.y, p.x + p.r, p.y);
                drawLine(p.x, p.y - p.r, p.x, p.y + p.r);
            }
            else if (p.shape == Point.STAR) {
                double angle = Math.PI / 10;
                double sin18 = Math.sin(angle);
                double cos18 = Math.cos(angle);
                double a = p.r * cos18;
                double b = p.r * sin18;
                double c = 2 * a * sin18;
                double d = 2 * a * cos18 - p.r;
                list = new ArrayList<Point>();
                list.add(new Point(p.x, p.y - p.r));
                list.add(new Point(p.x + c, p.y + d));
                list.add(new Point(p.x - a, p.y - b));
                list.add(new Point(p.x + a, p.y - b));
                list.add(new Point(p.x - c, p.y + d));
                if (p.fillShape) {
                    drawPath(list, 'f');
                }
                else {
                    drawPath(list, 's');
                }
            }
        }
    }


    /**
     *  Sets the text rendering mode.
     *
     *  @param mode the rendering mode.
     */
    public void setTextRenderingMode(int mode) throws Exception {
        if (mode >= 0 && mode <= 7) {
            this.renderingMode = mode;
        }
        else {
            throw new Exception("Invalid text rendering mode: " + mode);
        }
    }


    /**
     *  Sets the text direction.
     *
     *  @param degrees the angle.
     */
    public void setTextDirection(int degrees) throws Exception {
        if (degrees > 360) degrees %= 360;
        if (degrees == 0) {
            tm = new float[] { 1f,  0f,  0f,  1f};
        }
        else if (degrees == 90) {
            tm = new float[] { 0f,  1f, -1f,  0f};
        }
        else if (degrees == 180) {
            tm = new float[] {-1f,  0f,  0f, -1f};
        }
        else if (degrees == 270) {
            tm = new float[] { 0f, -1f,  1f,  0f};
        }
        else if (degrees == 360) {
            tm = new float[] { 1f,  0f,  0f,  1f};
        }
        else {
            float sinOfAngle = (float) Math.sin(degrees * (Math.PI / 180));
            float cosOfAngle = (float) Math.cos(degrees * (Math.PI / 180));
            tm = new float[] {cosOfAngle, sinOfAngle, -sinOfAngle, cosOfAngle};
        }
    }


    /**
     *  Draws a bezier curve starting from the current point.
     *  <strong>Please note:</strong> You must call the fillPath, closePath or strokePath method after the last bezierCurveTo call.
     *  <p><i>Author:</i> <strong>Pieter Libin</strong>, pieter@emweb.be</p>
     *
     *  @param p1 first control point
     *  @param p2 second control point
     *  @param p3 end point
     */
    public void bezierCurveTo(Point p1, Point p2, Point p3) throws IOException {
    	append(p1);
    	append(p2);
    	append(p3);
    	append("c\n");
    }


    /**
     *  Sets the start of text block.
     *  Please see Example_32. This method must have matching call to setTextEnd().
     */
    public void setTextStart() throws IOException {
        append("BT\n");
    }


    /**
     *  Sets the text location.
     *  Please see Example_32.
     *
     *  @param x the x coordinate of new text location.
     *  @param y the y coordinate of new text location.
     */
    public void setTextLocation(float x, float y) throws IOException {
        append(x);
        append(' ');
        append(height - y);
        append(" Td\n");
    }


    /**
     *  Sets the text leading.
     *  Please see Example_32.
     *
     *  @param leading the leading.
     */
    public void setTextLeading(float leading) throws IOException {
        append(leading);
        append(" TL\n");
    }


    public void setTextFont(Font font) throws IOException {
        append("/F");
        append(font.objNumber);
        append(' ');
        append(font.size);
        append(" Tf\n");
    }


    /**
     *  Prints a line of text and moves to the next line.
     *  Please see Example_32.
     */
    public void println(String str) throws IOException {
        print(str);
        println();
    }


    /**
     *  Prints a line of text.
     *  Please see Example_32.
     */
    public void print(String str) throws IOException {
        append('(');
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            if (ch == '(' || ch == ')' || ch == '\\') {
                append('\\');
                append((byte) ch);
            }
            else if (ch == '\t') {
                append(' ');
                append(' ');
                append(' ');
                append(' ');
            }
            else {
                append((byte) ch);
            }
        }
        append(") Tj\n");
    }


    /**
     *  Move to the next line.
     *  Please see Example_32.
     */
    public void println() throws IOException {
        append("T*\n");
    }


    /**
     *  Sets the end of text block.
     *  Please see Example_32.
     */
    public void setTextEnd() throws IOException {
        append("ET\n");
    }


    private void append(Point point) throws IOException {
        append(point.x);
        append(' ');
        append(height - point.y);
        append(' ');
    }


    protected void append(String str) throws IOException {
        for (int i = 0; i < str.length(); i++) {
            buf.write((byte) str.charAt(i));
        }
    }


    protected void append(int num) throws IOException {
        append(String.valueOf(num));
    }


    protected void append(float val) throws IOException {
        if (val == (int) val) {
            append((int) val);
        }
        else {
            append(Float.toString(val).replace(',', '.'));
        }
    }


    protected void append(double val) throws IOException {
        if (val == (int) val) {
            append((int) val);
        }
        else {
            append(String.valueOf(val).replace(',', '.'));
        }
    }


    protected void append(char ch) throws IOException {
        buf.write((byte) ch);
    }


    protected void append(byte b) throws IOException {
        buf.write(b);
    }


    /**
     *  Appends the specified array of bytes to the page.
     */
    public void append(byte[] buffer) throws IOException {
        buf.write(buffer);
    }

}   // End of Page.java
