/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Render table cells with left, right or center alignment.
 */

@SuppressWarnings("UnusedDeclaration")
public class SimpleTableCellRenderer extends DefaultTableCellRenderer {

    public enum Alignment {

        LEFT {
            public int getSwingConstant() {

                return SwingConstants.LEFT;

            }

        },

        CENTER {
            public int getSwingConstant() {

                return SwingConstants.CENTER;

            }

        },

        RIGHT {
            public int getSwingConstant() {

                return SwingConstants.RIGHT;

            }

        };

        public abstract int getSwingConstant();

    }

    /**
     * A JTable cell rendered that renders its value left justified.
     * <p/> The JTable cell is rendered using a {@link javax.swing.table.DefaultTableCellRenderer} and the result is then left-justified.
     */

    public static final SimpleTableCellRenderer LEFT_JUSTIFIED = new SimpleTableCellRenderer( Alignment.LEFT );

    /**
     * A JTable cell rendered that renders its value centered.
     * <p/> The JTable cell is rendered using a {@link javax.swing.table.DefaultTableCellRenderer} and the result is then centered.
     */

    public static final SimpleTableCellRenderer CENTERED = new SimpleTableCellRenderer( Alignment.CENTER );

    /**
     * A JTable cell rendered that renders its value right justified.
     * <p/> The JTable cell is rendered using a {@link javax.swing.table.DefaultTableCellRenderer} and the result is then right-justified.
     */

    public static final SimpleTableCellRenderer RIGHT_JUSTIFIED = new SimpleTableCellRenderer( Alignment.RIGHT );

    private final Alignment _alignment;

    private SimpleTableCellRenderer( final Alignment alignment ) {

        super();

        _alignment = alignment;

    }

    /**
     * Render a table cell using {@link javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent}
     * and then left-justify, center or right-justify the result depending on how this {@link com.obtuse.ui.SimpleTableCellRenderer} was defined.
     * <p/>See {@link javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent} for parameter and return value description.
     */

    public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column
    ) {

        Component renderedCell = super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
        );

        JLabel label = (JLabel)renderedCell;
        label.setHorizontalAlignment( _alignment.getSwingConstant() );

        return label;

    }

}
