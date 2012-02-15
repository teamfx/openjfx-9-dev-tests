/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.jemmy.fx.control;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jemmy.interfaces.TreeItem;
import org.jemmy.interfaces.EditableCellOwner.EditableCell;
import org.jemmy.fx.SceneDock;
import org.jemmy.interfaces.Parent;
import javafx.scene.Node;
import org.jemmy.control.Wrap;
import org.jemmy.lookup.EqualsLookup;
import org.jemmy.lookup.ByToStringLookup;
import org.jemmy.TimeoutExpiredException;
import org.jemmy.fx.AppExecutor;
import org.jemmy.interfaces.CellOwner.Cell;
import org.jemmy.interfaces.EditableCellOwner;
import org.jemmy.lookup.LookupCriteria;
import org.jemmy.resources.StringComparePolicy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shura
 */
public class TreeTest {

    public TreeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        AppExecutor.executeNoBlock(TreeApp.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    TreeViewDock tree;

    @Before
    public void setUp() {
        tree = new TreeViewDock(new SceneDock().asParent());
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void lookup() {
        new ScrollBarDock(tree.wrap().as(Parent.class, Node.class)).asScroll().caret().to(0);
        assertTrue(3
                < tree.asItemParent().
                lookup(new ByToStringLookup<Object>("01", StringComparePolicy.SUBSTRING)).size());
        assertEquals(1,
                tree.asItemParent().
                lookup(new ByToStringLookup<Object>("01", StringComparePolicy.EXACT)).size());
        assertEquals(1,
                tree.asItemParent().
                lookup(new EqualsLookup("010")).size());
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("02")).asTreeItem().collapse();
        try {
            tree.asItemParent().
                    lookup(new EqualsLookup("022")).wrap();
            fail("An axception should happen");
        } catch (TimeoutExpiredException e) {
        } catch (Exception e) {
            fail(TimeoutExpiredException.class.getName() + " + should happen");
        }
    }

    //@Test
    public void tree() {
        tree.asTree().selector().select(new ByToStringLookup("0"));
        assertEquals("000", tree.asTree().selector().select(new ByToStringLookup("00"), new ByToStringLookup("000")).getControl());
        assertEquals("011", tree.asTree().selector().select(new ByToStringLookup("01"), new ByToStringLookup("011")).getControl());
        assertEquals("001", tree.asTree().selector().select(new ByToStringLookup("00"), new ByToStringLookup("001")).getControl());
        Wrap<?> item010 = tree.asTree().selector().select(new ByToStringLookup("01"), new ByToStringLookup("010"));
        assertEquals("010", item010.getControl());
        assertEquals("023", tree.asTree().selector().select(new ByToStringLookup("02"), new ByToStringLookup("023")).getControl());
    }

    //@Test
    public void collapseAll() {
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("0")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("02")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("00")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("01")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("00")).asTreeItem().collapse();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("01")).asTreeItem().collapse();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("02")).asTreeItem().collapse();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("0")).asTreeItem().collapse();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("0")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("00")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("01")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("02")).asTreeItem().expand();
    }

    //@Test
    public void edit() {
        tree.asItemParent().setEditor(new TextFieldCellEditor());
        tree.asTree().selector().select(new ByToStringLookup("00"), new ByToStringLookup("00c"));
        EditableCell c00c = new TreeItemDock(tree.asItemParent(), new EqualsLookup("00c")).asEditableCell();
        c00c.edit("11");
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("00")).asTreeItem().collapse();
    }

    //@Test
    public void multiple() {
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("0")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("02")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("00")).asTreeItem().expand();
        new TreeItemDock(tree.asItemParent(), new EqualsLookup("01")).asTreeItem().expand();
        tree.asItemParent().select(new EqualsLookup("021"), new EqualsLookup("001"), new EqualsLookup("011"));
        assertEquals(3, tree.wrap().getControl().getSelectionModel().getSelectedItems().size());
    }

    //@Test
    public void wrap() {
        Parent<String> p = tree.wrap().as(Parent.class, String.class);
        p.lookup().size();
        p.lookup(new EqualsLookup("01")).as(TreeItem.class).collapse();
        p.lookup(new EqualsLookup("02")).as(TreeItem.class).expand();
    }
    
    @Test
    public void itemWrap() {
        EditableCellOwner<javafx.scene.control.TreeItem> p = tree.wrap().as(EditableCellOwner.class, javafx.scene.control.TreeItem.class);
        p.lookup().size();
        p.setEditor(new TextFieldCellEditor());
        
        p.lookup(new LookupCriteria<javafx.scene.control.TreeItem>() {

            public boolean check(javafx.scene.control.TreeItem control) {
                return control.getValue().equals("00");
            }
        }).as(EditableCell.class).edit("lala");
    }
    
}