package com.obtuse.ui.layout.play;

import com.obtuse.ui.MyActionListener;
import com.obtuse.ui.layout.LinearOrientation;
import com.obtuse.ui.layout.linear.LinearContainer;
import com.obtuse.ui.layout.linear.LinearLayoutUtil;
import com.obtuse.util.BasicProgramConfigInfo;
import com.obtuse.util.Logger;
import com.obtuse.util.ObtuseUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 An alternative to {@link FlowLayout} that (hopefully) works better.
 <p>By "works better", I primarily mean that this (from scratch) implementation
 handles the resizing of the container as the required space to display the
 contents of the container changes.</p>
 */

public class ObtuseFlowLayoutManager implements LayoutManager2 {

    private class ComponentPlan {

        @NotNull final Component _component;
        @NotNull final Rectangle _bounds;

        private ComponentPlan( @NotNull Component component, @NotNull Rectangle bounds ) {
            super();

            _component = component;
            _bounds = bounds;

        }

        @NotNull
        private Component getComponent() {

            return _component;

        }

        @NotNull
        private Rectangle getBounds() {

            return _bounds;

        }

        public String toString() {

            return "ComponentPlan( bounds=" + ObtuseUtil.fBounds( getBounds() ) + ", component=" + getComponent() + " )";

        }

    }

    private static boolean s_vLog = true;

    private int _vGap;
    private int _hGap;
    private int _minWidth = 0, _minHeight = 0;
    private int _preferredWidth = 0, _preferredHeight = 0;
    private boolean _sizeUnknown = true;
    private java.util.List<ComponentPlan> _plannedComponentBounds;

    public ObtuseFlowLayoutManager() {

        this( 3, 0 );
    }

    public ObtuseFlowLayoutManager( int vGap, int hGap ) {
        super();

        _vGap = vGap;
        _hGap = hGap;

    }

    /* Required by LayoutManager. */
    public void addLayoutComponent( String name, Component comp ) {

        vLog(
                "ObtuseFlowLayoutManager.addLayoutComponent( " +
                ObtuseUtil.enquoteToJavaString( name ) + ", " +
                comp +
                " )"
        );

    }

    /* Required by LayoutManager. */
    public void removeLayoutComponent( Component comp ) {

        vLog(
                "ObtuseFlowLayoutManager.addLayoutComponent( " +
                comp +
                " )"
        );

    }

    private void planLayout( Container parent, int assumedWidth ) {

        int componentCount = parent.getComponentCount();
//        Dimension cPrefSize = null;
        int parentWidth = parent.getWidth();
        int pHeight = parent.getHeight();

        _preferredWidth = 0;
        _preferredHeight = 0;
        _minWidth = 0;
        _minHeight = 0;
        _plannedComponentBounds = new ArrayList<>();

        Insets insets = parent.getInsets();
        final int rowStart = insets.left;
        final int rowEnd = parentWidth - ( insets.left + insets.right );
        final int colStart = insets.top;

        int x = rowStart;
        int y = colStart;

        int rowHeight = 0;
        int nRows = 0;

//        boolean rowStarted = false;

        _preferredWidth = rowStart;
        vLog( "starting layout:  insets=" + ObtuseUtil.fInsets( insets ) + ", rowStart=" + rowStart + ", rowEnd=" + rowEnd + ", colStart=" + colStart );
        for ( int i = 0; i < componentCount; i++ ) {

            if ( i == 0 ) {

                nRows = 1;

            }
            Component c = parent.getComponent( i );

            if ( c.isVisible() ) {

                Dimension cPrefSize = c.getPreferredSize();

                vLog( "new component's WxH is " + c.getWidth() + "x" + c.getHeight() + ", cPrefSize=" + ObtuseUtil.fDim( cPrefSize ) );

                vLog( "preferred component size is " + ObtuseUtil.fDim( cPrefSize ) );

                // If we've already dealt with at least one component then x
                // is the righthand edge of the component that we just dealt with.
                // If this is the first component then it, by definition, fits on the current line
                // since it won't fit any better on the next line if it doesn't appear to fit on this line.

                boolean startingNewRow = i == 0;

                vLog( "i=" + i + ", x=" + x + ", _hGap=" + _hGap );
                vLog( "x + _hGap + cPrefSize.width= " + ( x + _hGap + cPrefSize.width ) + ", rowEnd=" + rowEnd );

                if ( !startingNewRow && x + _hGap + cPrefSize.width > rowEnd ) {

                    y += rowHeight;
                    rowHeight = 0;
                    x = insets.left;
                    startingNewRow = true;
                    vLog( "starting a new row:  x=" + x + ", y=" + y + ", rowHeight=" + rowHeight );
                    nRows += 1;

                }

                rowHeight = Math.max( rowHeight, cPrefSize.height );
                int newX = startingNewRow ? rowStart : ( x + _hGap );
                ComponentPlan componentPlan = new ComponentPlan(
                        c,
                        new Rectangle(
                                newX,
                                y,
                                cPrefSize.width,
                                cPrefSize.height
                        )
                );
                _plannedComponentBounds.add(
                        componentPlan
                );

                x = newX + cPrefSize.width;
                _preferredWidth = Math.max( _preferredWidth, x );
                _preferredHeight = Math.max( _preferredHeight, y + cPrefSize.height );

                vLog( "rowHeight=" + rowHeight + ", newX=" + newX + ", _pCB[" + i + "]=" + ObtuseUtil.fBounds( componentPlan.getBounds() ) + ", x=" + x + ", _pW=" + _preferredWidth + ", _pH=" + _preferredHeight );

//                rowStarted = true;
//
//                if ( i > 0 ) {
//                    _preferredWidth += cPrefSize.width / 2;
//                    _preferredHeight += _vGap;
//                } else {
//                    _preferredWidth = cPrefSize.width;
//                }
//                _preferredHeight += cPrefSize.height;
//
//                _minWidth = Math.max(
//                        c.getMinimumSize().width,
//                        _minWidth
//                );
//                _minHeight = _preferredHeight;
            }

        }

        _preferredWidth += insets.right;
        _preferredHeight += insets.bottom;

        vLog( "done:  _pW=" + _preferredWidth + ", _pH=" + _preferredHeight + ", nRows=" + nRows );

    }


    /* Required by LayoutManager. */
    public Dimension preferredLayoutSize( Container parent ) {

        vLog(
                "ObtuseFlowLayoutManager.preferredLayoutSize( " +
                "pw=" + _preferredWidth + ", " +
                "ph=" + _preferredHeight + ", " +
                parent +
                " )"
        );

        planLayout( parent, 100000 );

        Dimension dim = new Dimension( _preferredWidth, _preferredHeight );

//        Dimension dim = new Dimension( 50, 50 );
        vLog( "preferredLayoutSize=" + ObtuseUtil.fDim( dim ) );

        return dim;

//        Dimension dim = new Dimension( 0, 0 );
//        int nComps = parent.getComponentCount();
//
//        planLayout( parent );
//
//        //Always add the container's insets!
//        Insets insets = parent.getInsets();
//        dim.width = _preferredWidth
//                    + insets.left + insets.right;
//        dim.height = _preferredHeight
//                     + insets.top + insets.bottom;
//
//        _sizeUnknown = false;
//
//        return dim;
    }

    /* Required by LayoutManager. */
    public Dimension minimumLayoutSize( Container parent ) {

        vLog(
                "ObtuseFlowLayoutManager.minimumLayoutSize( " +
                "pw=" + _preferredWidth + ", " +
                "ph=" + _preferredHeight + ", " +
                parent +
                " )"
        );

        planLayout( parent, 100000 );

        Dimension dim = new Dimension( _preferredWidth, _preferredHeight );

//        Dimension dim = new Dimension( 25, 25 );

        vLog( "minimumLayoutSize=" + ObtuseUtil.fDim( dim ) );

        return dim;

//        Dimension dim = new Dimension( 0, 0 );
//        int nComps = parent.getComponentCount();
//
//        //Always add the container's insets!
//        Insets insets = parent.getInsets();
//        dim.width = _minWidth
//                    + insets.left + insets.right;
//        dim.height = _minHeight
//                     + insets.top + insets.bottom;
//
//        _sizeUnknown = false;
//
//        return dim;
    }

    /* Required by LayoutManager. */
    /*
     * This is called when the panel is first displayed,
     * and every time its size changes.
     * Note: You CAN'T assume preferredLayoutSize or
     * minimumLayoutSize will be called -- in the case
     * of applets, at least, they probably won't be.
     */
    public void layoutContainer( Container parent ) {

        vLog(
                "ObtuseFlowLayoutManager.layoutContainer( " +
                parent +
                " )"
        );

        Dimension pPrefSize = parent.getPreferredSize();
        planLayout( parent, pPrefSize.width );

        int ix = 0;
        for ( ComponentPlan componentPlan : _plannedComponentBounds ) {

            Component c = componentPlan.getComponent();
            Rectangle bounds = componentPlan.getBounds();

            vLog( "component #" + ix + " is being placed at " + ObtuseUtil.fBounds( bounds ) );

            c.setBounds( bounds );

            ix += 1;

        }

//        Insets insets = parent.getInsets();
//        int maxWidth = parent.getWidth()
//                       - ( insets.left + insets.right );
//        int maxHeight = parent.getHeight()
//                        - ( insets.top + insets.bottom );
//        int nComps = parent.getComponentCount();
//        int previousWidth = 0, previousHeight = 0;
//        int x = 0, y = insets.top;
//        int rowh = 0, start = 0;
//        int xFudge = 0, yFudge = 0;
//        boolean oneColumn = false;
//
//        // Go through the components' sizes, if neither
//        // preferredLayoutSize nor minimumLayoutSize has
//        // been called.
//        if ( _sizeUnknown ) {
//            planLayout( parent );
//        }
//
//        if ( maxWidth <= _minWidth ) {
//            oneColumn = true;
//        }
//
//        if ( maxWidth != _preferredWidth ) {
//            xFudge = ( maxWidth - _preferredWidth ) / ( nComps - 1 );
//        }
//
//        if ( maxHeight > _preferredHeight ) {
//            yFudge = ( maxHeight - _preferredHeight ) / ( nComps - 1 );
//        }
//
//        for ( int i = 0; i < nComps; i++ ) {
//            Component c = parent.getComponent( i );
//            if ( c.isVisible() ) {
//                Dimension d = c.getPreferredSize();
//
//                // increase x and y, if appropriate
//                if ( i > 0 ) {
//                    if ( !oneColumn ) {
//                        x += previousWidth / 2 + xFudge;
//                    }
//                    y += previousHeight + _vGap + yFudge;
//                }
//
//                // If x is too large,
//                if ( ( !oneColumn ) &&
//                     ( x + d.width ) >
//                     ( parent.getWidth() - insets.right ) ) {
//                    // reduce x to a reasonable number.
//                    x = parent.getWidth()
//                        - insets.bottom - d.width;
//                }
//
//                // If y is too large,
//                if ( ( y + d.height )
//                     > ( parent.getHeight() - insets.bottom ) ) {
//                    // do nothing.
//                    // Another choice would be to do what we do to x.
//                }
//
//                // Set the component's size and position.
//                c.setBounds( x, y, d.width, d.height );
//
//                previousWidth = d.width;
//                previousHeight = d.height;
//            }
//        }
    }

    public String toString() {

        return getClass().getName() + "( vgap=" + _vGap + " )";
    }

    @Override
    public void addLayoutComponent( final Component comp, final Object constraints ) {

        vLog(
                "ObtuseFlowLayoutManager.addLayoutComponent( " +
                comp + ", " +
                constraints +
                " )"
        );

    }

    @Override
    public Dimension maximumLayoutSize( final Container target ) {

        vLog(
                "ObtuseFlowLayoutManager.maximumLayoutSize( " +
                target +
                " )"
        );

//        planLayout( target );

        Dimension dim = new Dimension( 32767, 32767 );

//        Dimension dim = new Dimension( 100, 100 );

        vLog( "maximumLayoutSize=" + ObtuseUtil.fDim( dim ) );

        return dim;

    }

    @Override
    public float getLayoutAlignmentX( final Container target ) {

        vLog(
                "ObtuseFlowLayoutManager.getLayoutAlignmentX( " +
                target +
                " )"
        );


        return 0;
    }

    @Override
    public float getLayoutAlignmentY( final Container target ) {

        vLog(
                "ObtuseFlowLayoutManager.getLayoutAlignmentY( " +
                target +
                " )"
        );

        return 0;
    }

    @Override
    public void invalidateLayout( final Container target ) {

        vLog(
                "ObtuseFlowLayoutManager.invalidateLayout( " +
                target +
                " )"
        );

    }

    public static void setVlog( boolean vLog ) {

        s_vLog = vLog;

    }

    private static void vLog( @NotNull final String msg ) {

        if ( s_vLog ) {

            Logger.logMsg( msg );

        }

    }

    public static void main( String[] args ) {

        BasicProgramConfigInfo.init(
                "Obtuse",
                "com.obtuse.ui.layout.play",
                "ObtuseFlowLayoutManager"
        );

        JFrame jf = new JFrame( "Testing ObtuseFlowLayoutManager" );

        JPanel jpd = new JPanel();
//        jpd.setLayout( new FlowLayout() );
        jpd.setLayout( new ObtuseFlowLayoutManager( 0, 30 ) );

        JButton add = new JButton( "Add A Button" );
        add.addActionListener(
                new MyActionListener() {

                    @Override
                    protected void myActionPerformed( final ActionEvent actionEvent ) {

                        jpd.add( new JButton( "button" + jpd.getComponentCount() ) );
                        jpd.revalidate();

                    }

                }
        );
        jpd.add( add );
        jpd.add( new JButton( "button 1" ) );
        jpd.add( new JButton( "button 2" ) );
        jpd.add( new JButton( "button 3" ) );
        jpd.add( new JButton( "button 4" ) );
        jpd.setBorder( BorderFactory.createLineBorder( Color.GREEN, 4 ) );

        LinearContainer lc = LinearLayoutUtil.createPanel3(
                "ObtuseFlowLayoutManager test",
                LinearOrientation.VERTICAL
        );
        lc.add( jpd );
        LinearLayoutUtil.SpaceSponge sponge = new LinearLayoutUtil.SpaceSponge();
        sponge.setBackground( Color.GRAY );
        lc.add( sponge );
        jf.setContentPane( lc.getAsJPanel() );
        jf.pack();
        jf.setVisible( true );

    }

}