package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.Color2dRenderer;
import com.zergatul.cheatutils.render.MainFrameBuffer;

import java.util.Arrays;

public class TableElement implements Element {

    private final int rowCount;
    private final int colCount;
    private final Element[][] cells;
    private int borderWidth;
    private int[] colWidths;
    private int[] rowHeights;
    private int measuredWidth, measuredHeight;
    private int x, y;

    public TableElement(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;

        cells = new Element[rowCount][];
        for (int r = 0; r < rowCount; r++) {
            cells[r] = new Element[colCount];
        }
    }

    @Override
    public void measure(RenderingContext context) {
        for (Element[] row : cells) {
            for (Element cell : row) {
                if (cell != null) {
                    cell.measure(context);
                }
            }
        }

        colWidths = new int[colCount];
        rowHeights = new int[rowCount];

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Element element = cells[r][c];
                if (element == null) {
                    continue;
                }
                if (element.getMeasuredWidth() > colWidths[c]) {
                    colWidths[c] = element.getMeasuredWidth();
                }
                if (element.getMeasuredHeight() > rowHeights[r]) {
                    rowHeights[r] = element.getMeasuredHeight();
                }
            }
        }

        measuredWidth = Arrays.stream(colWidths).sum() + borderWidth * (colCount + 1);
        measuredHeight = Arrays.stream(rowHeights).sum() + borderWidth * (rowCount + 1);
    }

    @Override
    public int getMeasuredWidth() {
        return measuredWidth;
    }

    @Override
    public int getMeasuredHeight() {
        return measuredHeight;
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;

        int yOffset = y + borderWidth;
        for (int r = 0; r < rowCount; r++) {
            int xOffset = x + borderWidth;
            for (int c = 0; c < colCount; c++) {
                Element element = cells[r][c];
                if (element != null) {
                    element.layout(
                            xOffset + (colWidths[c] - element.getMeasuredWidth()) / 2,
                            yOffset + (rowHeights[r] - element.getMeasuredHeight()) / 2,
                            colWidths[c], rowHeights[r]);
                }
                xOffset += colWidths[c] + borderWidth;
            }
            yOffset += rowHeights[r] + borderWidth;
        }
    }

    @Override
    public void render(RenderingContext context) {
//        if (borderWidth > 0) {
//            MainFrameBuffer.bind();
//            Color2dRenderer renderer = RenderUtilities.instance.getColor2dRenderer();
//            renderer.begin();
//
//            int yCursor = y;
//            for (int i = 0; i <= rowCount; i++) {
//                int x0 = x;
//                int x1 = x + measuredWidth;
//                int y0 = yCursor;
//                int y1 = yCursor + borderWidth;
//                renderer.quad(
//                        x0, y0,
//                        x1, y0,
//                        x1, y1,
//                        x0, y1,
//                        1, 1, 1, 1);
//                if (i < rowCount) {
//                    yCursor += rowHeights[i] + borderWidth;
//                }
//            }
//
//            int xCursor = x;
//            for (int i = 0; i <= colCount; i++) {
//                int x0 = xCursor;
//                int x1 = xCursor + borderWidth;
//                int y0 = y;
//                int y1 = y + measuredHeight;
//                renderer.quad(
//                        x0, y0,
//                        x1, y0,
//                        x1, y1,
//                        x0, y1,
//                        1, 1, 1, 1);
//                if (i < colCount) {
//                    xCursor += colWidths[i] + borderWidth;
//                }
//            }
//
//            renderer.end(context.getMatrix());
//        }

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Element element = cells[r][c];
                if (element != null) {
                    element.render(context);
                }
            }
        }
    }

    public void setContent(int row, int col, Element element) {
        cells[row][col] = element;
    }

    public void setBorderWidth(int width) {
        this.borderWidth = width;
    }
}