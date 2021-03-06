/*
 * Copyright © 2017 Daniel Boulet
 * All rights reserved.
 */

package com.obtuse.ui.tableutils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.lang.reflect.Modifier;

/**
 A customizable table cell editor.
 */

public abstract class CustomizableCellEditor extends DefaultCellEditor {

    final Class[] argTypes = new Class[]{ String.class };

    java.lang.reflect.Constructor constructor;

    Object value;

    public CustomizableCellEditor() {

        super( new JTextField() );

        // Daniel Boulet danny somewhere in matilda.com 2017/11/24:
        //
        // I don't really know why the line
        //
        //    getComponent().setName( "Table.editor" );
        //
        // is here.
        //
        // In my defence, the whole cell editor stuff bears an amazingly strong resemblance to black magic
        // at times and I can recall getting a little desperate back when I was figuring out how to make this
        // CustomizableCellEditor class work.
        //
        // Also, the JTable.GenericEditor class does this (again, don't really know why).

        getComponent().setName( "Table.editor" );

    }

    /**
     Provide an opportunity to do input validation.
     <p/>This method is invoked when the human presses the return key or the tab key or some other equivalent
     key to indicate that they are done editing the cell.
     This method should <em>do what needs to be done</em> to validate the input and then either return quietly
     if it is happy with the input or throw any unchecked exception with a message saying what made the validation method grumpy.

     @param currentValue the contents in the edit box when the user presses the return key or something equivalent.
     @throws Exception any unchecked exception describing the problem if the input is found to be invalid.
     */

    public abstract void validate( String currentValue ) throws Exception;

    public boolean stopCellEditing() {

        String s = (String)super.getCellEditorValue();

        // Here we are dealing with the case where a user
        // has deleted the string value in a cell, possibly
        // after a failed validation. Return null, so that
        // they have the option to replace the value with
        // null or use escape to restore the original.
        // For Strings, return "" for backward compatibility.
        try {

            // Invoke the validate method in our implementation class.
            // They will throw an unchecked exception if they are unimpressed with the current value of the edit box.
            // Otherwise, they will return quietly.

            validate( s );

            // If the edit box is empty then stop the editing session.
            // Backward compatibility demands that the value of the editing session be an empty string if the cell's {@code class} is
            // {@link String} or {@code null} otherwise.

            if ( "".equals( s ) ) {

                if ( constructor.getDeclaringClass() == String.class ) {

                    value = "";

                }

                return super.stopCellEditing();

            }

            // We are about to invoke the constructor from the cell's {@code class} which takes a single {@link String} parameter.
            // Before we do that, verify that we are allowed to call the constructor for the cell's {@code class} (we don't want to expose
            // any package-private constructors in our package to software that isn't from our package).

            checkAccess( constructor.getModifiers() );

            // Create a result value for the edit session which is of the correct {@code class} for the cell.

            //noinspection RedundantArrayCreation
            value = constructor.newInstance( new Object[]{ s } );

        } catch ( Exception e ) {

            ( (JComponent)getComponent() ).setBorder( new LineBorder( Color.red ) );

            return false;

        }

        return super.stopCellEditing();

    }

    /**
     * Utility method that throws SecurityException if SecurityManager is set
     * and modifiers are not public.
     * <p>'Borrowed' from the old {@code sun.swing.SwingUtilities2} class.</p>
     * @param modifiers a set of modifiers
     */

    public static void checkAccess(int modifiers) {

        if (System.getSecurityManager() != null
            && !Modifier.isPublic( modifiers)) {
            throw new SecurityException("Resource is not accessible");

        }
    }

    /**
     * Checks package access on the given classname.
     * This method is typically called when the Class instance is not
     * available and the caller attempts to load a class on behalf
     * the true caller (application).
     * <p>'Borrowed' from the old {@code sun.reflect.misc.ReflectUtil} class.</p>
     */
    public static void checkPackageAccess(String name) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            String cname = name.replace('/', '.');
            if (cname.startsWith("[")) {
                int b = cname.lastIndexOf('[') + 2;
                if (b > 1 && b < cname.length()) {
                    cname = cname.substring(b);
                }
            }
            int i = cname.lastIndexOf('.');
            if (i != -1) {
                s.checkPackageAccess(cname.substring(0, i));
            }
        }
    }

    public Component getTableCellEditorComponent(
            final JTable table, final Object value,
            final boolean isSelected,
            final int row, final int column
    ) {

        this.value = null;
        ( (JComponent)getComponent() ).setBorder( new LineBorder( Color.black ) );

        try {

            Class<?> type = table.getColumnClass( column );

            // Since our obligation is to produce a value which is
            // assignable for the required type it is OK to use the
            // String constructor for columns which are declared
            // to contain Objects. A String is an Object.

            if ( type == Object.class ) {

                type = String.class;

            }

            // This was re-worked to avoid using the sun.reflect.misc.ReflectUtil} class
            // which went behind an invisible shield with the advent of Java 9 (maybe Java 10).
            // The old line read
            //
            // ReflectUtil.checkPackageAccess( type );
            //
            // It probably needs debuggin'.

            checkPackageAccess( type.getCanonicalName() );
            checkAccess( type.getModifiers() );
            constructor = type.getConstructor( argTypes );

        } catch ( Exception e ) {

            return null;

        }

        return super.getTableCellEditorComponent( table, value, isSelected, row, column );

    }

    public Object getCellEditorValue() {

        return value;

    }

    public String toString() {

        return "CustomizableCellEditor()";

    }

}