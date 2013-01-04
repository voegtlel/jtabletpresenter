/**
 *  Table.java
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

import java.util.*;


/**
 *  Used to create table objects and draw them on a page.
 *
 *  Please see Example_08.
 */
public class Table {

    public static int DATA_HAS_0_HEADER_ROWS = 0;
    public static int DATA_HAS_1_HEADER_ROWS = 1;
    public static int DATA_HAS_2_HEADER_ROWS = 2;
    public static int DATA_HAS_3_HEADER_ROWS = 3;
    public static int DATA_HAS_4_HEADER_ROWS = 4;
    public static int DATA_HAS_5_HEADER_ROWS = 5;
    public static int DATA_HAS_6_HEADER_ROWS = 6;
    public static int DATA_HAS_7_HEADER_ROWS = 7;
    public static int DATA_HAS_8_HEADER_ROWS = 8;
    public static int DATA_HAS_9_HEADER_ROWS = 9;

    private int rendered = 0;
    private int numOfPages;

    private List<List<Cell>> tableData = null;
    private int numOfHeaderRows = 0;

    private float x1;
    private float y1;

    private float margin = 1f;
    private float bottom_margin = 30f;


    /**
     *  Create a table object.
     *
     */
    public Table() {
        tableData = new ArrayList<List<Cell>>();
    }


    /**
     *  Sets the position (x, y) of the top left corner of this table on the page.
     *
     *  @param x the x coordinate of the top left point of the table.
     *  @param y the y coordinate of the top left point of the table.
     */
    public void setPosition(double x, double y) {
        this.x1 = (float) x;
        this.y1 = (float) y;
    }


    /**
     *  Sets the position (x, y) of the top left corner of this table on the page.
     *
     *  @param x the x coordinate of the top left point of the table.
     *  @param y the y coordinate of the top left point of the table.
     */
    public void setPosition(float x, float y) {
        setLocation(x, y);
    }


    /**
     *  Sets the location (x, y) of the top left corner of this table on the page.
     *
     *  @param x the x coordinate of the top left point of the table.
     *  @param y the y coordinate of the top left point of the table.
     */
    public void setLocation(float x, float y) {
        this.x1 = x;
        this.y1 = y;
    }


    /**
     *  Sets the padding around the cells of this table.
     *
     *  @param margin the padding.
     */
    public void setCellPadding(double margin) {
        this.margin = (float) margin;
    }


    /**
     *  Sets the padding around the cells of this table.
     *
     *  @param margin the padding.
     */
    public void setCellPadding(float margin) {
        this.margin = margin;
    }


    /**
     *  Sets the margin around the cells of this table.
     *
     *  @param margin the margin.
     */
    public void setCellMargin(double margin) {
        this.margin = (float) margin;
    }


    /**
     *  Sets the margin around the cells of this table.
     *
     *  @param margin the margin.
     */
    public void setCellMargin(float margin) {
        this.margin = margin;
    }


    /**
     *  Sets the bottom margin for this table.
     *
     *  @param bottom_margin the margin.
     */
    public void setBottomMargin(double bottom_margin) {
        this.bottom_margin = (float) bottom_margin;
    }


    /**
     *  Sets the bottom margin for this table.
     *
     *  @param bottom_margin the margin.
     */
    public void setBottomMargin(float bottom_margin) {
        this.bottom_margin = bottom_margin;
    }


    /**
     *  Sets the table data.
     *
     *  The table data is a perfect grid of cells.
     *  All cell should be an unique object and you can not reuse blank cell objects.
     *  Even if one or more cells have colspan bigger than zero the number of cells in the row will not change.
     *
     *  @param tableData the table data.
     */
    public void setData(
            List<List<Cell>> tableData) throws Exception {
        this.tableData = tableData;
        this.numOfHeaderRows = 0;
        this.rendered = numOfHeaderRows;
    }


    /**
     *  Sets the table data and specifies the number of header rows in this data.
     *
     *  @param tableData the table data.
     *  @param numOfHeaderRows the number of header rows in this data.
     */
    public void setData(
            List<List<Cell>> tableData, int numOfHeaderRows) throws Exception {
        this.tableData = tableData;
        this.numOfHeaderRows = numOfHeaderRows;
        this.rendered = numOfHeaderRows;
    }


    /**
     *  Auto adjusts the widths of all columns so that they are just wide enough to hold the text without truncation.
     */
    public void autoAdjustColumnWidths() {
        // Find the maximum text width for each column
        float[] max_col_widths = new float[tableData.get(0).size()];
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = row.get(j);
                if (cell.getColSpan() == 1) {
                    if (cell.text != null) {
                        cell.setWidth(cell.font.stringWidth(cell.text));
                        if (max_col_widths[j] == 0.0 ||
                                cell.getWidth() > max_col_widths[j]) {
                            max_col_widths[j] = cell.width;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = row.get(j);
                cell.setWidth(max_col_widths[j] + 3 * margin);
            }
        }
    }


    /**
     *  Sets the alignment of the numbers to the right.
     */
    public void rightAlignNumbers() {
        for (int i = numOfHeaderRows; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = row.get(j);
                if (cell.text != null) {
                    String str = cell.text;
                    int len = str.length();
                    boolean isNumber = true;
                    int k = 0;
                    while (k < len) {
                        char ch = str.charAt(k++);
                        if (!Character.isDigit(ch)
                                && ch != '('
                                && ch != ')'
                                && ch != '-'
                                && ch != '.'
                                && ch != ','
                                && ch != '\'') {
                            isNumber = false;
                        }
                    }
                    if (isNumber) {
                        cell.setTextAlignment(Align.RIGHT);
                    }
                }
            }
        }
    }


    /**
     *  Removes the horizontal lines between the rows from index1 to index2.
     */
    public void removeLineBetweenRows(
            int index1, int index2) throws Exception {
        for (int j = index1; j < index2; j++) {
            List<Cell> row = tableData.get(j);
            for (int i = 0; i < row.size(); i++) {
                Cell cell = row.get(i);
                cell.setBorder(Border.BOTTOM, false);
            }
            row = tableData.get(j + 1);
            for (int i = 0; i < row.size(); i++) {
                Cell cell = row.get(i);
                cell.setBorder(Border.TOP, false);
            }
        }
    }


    /**
     *  Sets the text alignment in the specified column.
     *
     *  @param index the index of the specified column.
     *  @param alignment the specified alignment. Supported values: Align.LEFT, Align.RIGHT, Align.CENTER and Align.JUSTIFY.
     */
    public void setTextAlignInColumn(
            int index, int alignment) throws Exception {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            if (index < row.size()) {
                row.get(index).setTextAlignment(alignment);
            }
        }
    }


    /**
     *  Sets the color of the text in the specified column.
     *
     *  @param index the index of the specified column.
     *  @param color the color specified as an integer.
     */
    public void setTextColorInColumn(
            int index, int color) throws Exception {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            if (index < row.size()) {
                row.get(index).setBrushColor(color);
            }
        }
    }


    /**
     *  Sets the font and font size for the specified column.
     *  @deprecated  As of version 4.00, replaced by {@link #setFontInColumn(int, Font)}
     *
     *  @param index the index of the specified column.
     *  @param font the font.
     *  @param size the font size.
     */
    @Deprecated
    public void setTextFontInColumn(
            int index, Font font, double size) throws Exception {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            if (index < row.size()) {
                row.get(index).font = font;
                row.get(index).font.setSize(size);
            }
        }
    }


    /**
     *  Sets the font and font size for the specified column.
     *
     *  @param index the index of the specified column.
     *  @param font the font.
     */
    public void setFontInColumn(int index, Font font) throws Exception {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            if (index < row.size()) {
                row.get(index).font = font;
            }
        }
    }


    /**
     *  Sets the color of the text in the specified row.
     *
     *  @param index the index of the specified row.
     *  @param color the color specified as an integer.
     */
    public void setTextColorInRow(
            int index, int color) throws Exception {
        List<Cell> row = tableData.get(index);
        for (int i = 0; i < row.size(); i++) {
            row.get(i).setBrushColor(color);
        }
    }


    /**
     *  Sets the font and font size for the text in the specified row.
     *  @deprecated  As of version 4.00, replaced by {@link #setFontInRow(int, Font)}
     *
     *  @param index the index of the specified row.
     *  @param font the font.
     *  @param size the font size.
     */
    @Deprecated
    public void setTextFontInRow(
            int index, Font font, double size) throws Exception {
        List<Cell> row = tableData.get(index);
        for (int i = 0; i < row.size(); i++) {
            row.get(i).font = font;
            row.get(i).font.setSize(size);
        }
    }


    /**
     *  Sets the font and font size for the text in the specified row.
     *
     *  @param index the index of the specified row.
     *  @param font the font.
     */
    public void setFontInRow(int index, Font font) throws Exception {
        List<Cell> row = tableData.get(index);
        for (int i = 0; i < row.size(); i++) {
            row.get(i).font = font;
        }
    }


    /**
     *  Sets the width of the column with the specified index.
     *
     *  @param index the index of specified column.
     *  @param width the specified width.
     */
    public void setColumnWidth(
            int index, double width) throws Exception {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            if (index < row.size()) {
                row.get(index).setWidth(width);
            }
        }
    }


    /**
     *  Returns the cell at the specified row and column.
     *
     *  @param row the specified row.
     *  @param col the specified column.
     *
     *  @return the cell at the specified row and column.
     */
    public Cell getCellAt(
            int row, int col) throws Exception {
        if (row >= 0) {
            return tableData.get(row).get(col);
        }
        return tableData.get(tableData.size() + row).get(col);
    }


    /**
     *  Returns a list of cell for the specified row.
     *
     *  @param index the index of the specified row.
     *
     *  @return the list of cells.
     */
    public List<Cell> getRow(int index) throws Exception {
        return tableData.get(index);
    }


    /**
     *  Returns a list of cell for the specified column.
     *
     *  @param index the index of the specified column.
     *
     *  @return the list of cells.
     */
    public List<Cell> getColumn(int index) throws Exception {
        List<Cell> column = new ArrayList<Cell>();
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            if (index < row.size()) {
                column.add(row.get(index));
            }
        }
        return column;
    }


    /**
     *  Returns the total number of pages that are required to draw this table on.
     *
     *  @param page the type of pages we are drawing this table on.
     *
     *  @return the number of pages.
     */
    public int getNumberOfPages(Page page) throws Exception {
        numOfPages = 1;
        while (hasMoreData()) {
            drawOn(page, false);
        }
        resetRenderedPagesCount();
        return numOfPages;
    }


    /**
     *  Draws this table on the specified page.
     *
     *  @param page the page to draw this table on.
     *
     *  @return Point the point on the page where to draw the next component.
     */
    public Point drawOn(Page page) throws Exception {
        return drawOn(page, true);
    }


    /**
     *  Draws this table on the specified page.
     *
     *  @param page the page to draw this table on.
     *  @param draw if false - do not draw the table. Use to only find out where the table ends.
     *
     *  @return Point the point on the page where to draw the next component.
     */
    public Point drawOn(Page page, boolean draw) throws Exception {
        float x = x1;
        float y = y1;
        float cell_w;
        float cell_h = 0f;

        for (int i = 0; i < numOfHeaderRows; i++) {
            cell_h = 0f;
            List<Cell> dataRow = tableData.get(i);
            for (int j = 0; j < dataRow.size(); j++) {
                Cell cell = dataRow.get(j);
                float cellHeight = cell.font.body_height + 2 * margin;
                if (cellHeight > cell_h) {
                    cell_h = cellHeight;
                }
                cell_w = cell.width;
                int colspan = cell.getColSpan();
                for (int k = 1; k < colspan; k++) {
                    cell_w += dataRow.get(++j).width;
                }

                if (draw) {
                    page.setBrushColor(cell.getBrushColor());
                    cell.paint(page, x, y, cell_w, cell_h, margin);
                }

                x += cell_w;
            }
            x = x1;
            y += cell_h;
        }

        for (int i = rendered; i < tableData.size(); i++) {
            cell_h = 0f;
            List<Cell> dataRow = tableData.get(i);
            for (int j = 0; j < dataRow.size(); j++) {
                Cell cell = dataRow.get(j);
                float cellHeight = cell.font.body_height + 2 * margin;
                if (cellHeight > cell_h) {
                    cell_h = cellHeight;
                }
                cell_w = cell.width;
                int colspan = cell.getColSpan();
                for (int k = 1; k < colspan; k++) {
                    cell_w += dataRow.get(++j).getWidth();
                }

                if (draw) {
                    page.setBrushColor(cell.getBrushColor());
                    cell.paint(page, x, y, cell_w, cell_h, margin);
                }

                x += cell_w;
            }
            x = x1;
            y += cell_h;

            // Consider the height of the next row when checking if we must go to a new page
            if (i < (tableData.size() - 1)) {
                List<Cell> nextRow = tableData.get(i + 1);
                for (int j = 0; j < nextRow.size(); j++) {
                    Cell cell = nextRow.get(j);
                    float cellHeight = cell.font.body_height + 2 * margin;
                    if (cellHeight > cell_h) {
                        cell_h = cellHeight;
                    }
                }
            }

            if ((y + cell_h) > (page.height - bottom_margin)) {
                if (i == tableData.size() - 1) {
                    rendered = -1;
                }
                else {
                    rendered = i + 1;
                    numOfPages++;
                }
                return new Point(x, y);
            }
        }

        rendered = -1;

        return new Point(x, y);
    }


    /**
     *  Returns true if the table contains more data that needs to be drawn on a page.
     */
    public boolean hasMoreData() {
        return rendered != -1;
    }


    /**
     *  Returns the width of this table when drawn on a page.
     *
     *  @return the widht of this table.
     */
    public float getWidth() {
        float table_width = 0.0f;
        List<Cell> row = tableData.get(0);
        for (int i = 0; i < row.size(); i++) {
            table_width += row.get(i).getWidth();
        }
        return table_width;
    }


    /**
     *  Returns the number of data rows that have been rendered so far.
     *
     *  @return the number of data rows that have been rendered so far.
     */
    public int getRowsRendered() {
        return rendered == -1 ? rendered : rendered - numOfHeaderRows;
    }


    /**
     *  Wraps around the text in all cells so it fits the column width.
     *
     */
    public void wrapAroundCellText() {
        List<List<Cell>> tableData2 = new ArrayList<List<Cell>>();

        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            int maxNumVerCells = 1;
            for (int j = 0; j < row.size(); j++) {
                Cell cell = row.get(j);
                int numVerCells = cell.getNumVerCells(margin);
                if (numVerCells > maxNumVerCells) {
                    maxNumVerCells = numVerCells;
                }
            }

            for (int j = 0; j < maxNumVerCells; j++) {
                List<Cell> row2 = new ArrayList<Cell>();
                for (int k = 0; k < row.size(); k++) {
                    Cell cell = row.get(k);
                    Cell cell2 = new Cell(cell.getFont(), "");
                    if (j == 0) {
                        cell2.setText(cell.getText());
                    }
                    cell2.setPoint(cell.getPoint());
                    cell2.setCompositeTextLine(cell.getCompositeTextLine());

                    cell2.setWidth(cell.width);
                    cell2.setHeight(cell.height);
                    cell2.setLineWidth(cell.lineWidth);

                    cell2.setBgColor(cell.getBgColor());
                    cell2.setPenColor(cell.getPenColor());
                    cell2.setBrushColor(cell.getBrushColor());

                    cell2.setProperties(cell.getProperties());
                    row2.add(cell2);
                }
                tableData2.add(row2);
            }
        }

        for (int i = 0; i < tableData2.size(); i++) {
            List<Cell> row = tableData2.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = row.get(j);
                if (cell.text != null) {
                    String[] tokens = cell.getText()
                            .replaceAll("\t", " ")
                            .replaceAll("\r", " ")
                            .replaceAll("\n", " ")
                            .replaceAll("\r\n", " ").split(" +");
                    int n = 0;
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < tokens.length; k++) {
                        String token = tokens[k];
                        if (cell.font.stringWidth(sb.toString() + " " + token)
                                > (cell.getWidth() - margin)) {
                            tableData2.get(i + n).get(j).setText(sb.toString());
                            sb = new StringBuilder(token);
                            n++;
                        }
                        else {
                            if (k > 0) {
                                sb.append(" ");
                            }
                            sb.append(token);
                        }
                    }
                    tableData2.get(i + n).get(j).setText(sb.toString());
                }
            }
        }

        tableData = tableData2;
    }


    /**
     *  Sets all table cells borders to <strong>false</strong>.
     *
     */
    public void setNoCellBorders() {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = tableData.get(i).get(j);
                cell.setNoBorders();
            }
        }
    }


    /**
     *  Sets the color of the cell border lines.
     *
     *  @param color the color of the cell border lines.
     */
    public void setCellBordersColor(int color) {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = tableData.get(i).get(j);
                cell.setPenColor(color);
            }
        }
    }


    /**
     *  Sets the width of the cell border lines.
     *
     *  @param width the width of the border lines.
     */
    public void setCellBordersWidth(float width) {
        for (int i = 0; i < tableData.size(); i++) {
            List<Cell> row = tableData.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell cell = tableData.get(i).get(j);
                cell.setLineWidth(width);
            }
        }
    }


    /**
     * Resets the rendered pages count. Call this method if you have to draw this table more than one time.
     */
    public void resetRenderedPagesCount() {
        this.rendered = numOfHeaderRows;
    }

}   // End of Table.java
