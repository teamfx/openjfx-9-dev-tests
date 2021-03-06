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

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.jemmy.action.FutureAction;
import org.jemmy.control.Wrap;
import org.jemmy.control.Wrapper;
import org.jemmy.lookup.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class WebNodeParent<ITEM extends org.w3c.dom.Node> extends AbstractParent<ITEM>
        implements ControlList {

    public static final String SELECTED_ITEM_PROP_NAME = "selectedItem";
    protected Wrapper wrapper;
    protected Class<ITEM> itemClass;
    protected Scene scene;
    protected WebViewWrap<? extends WebView> view;
    protected WebNodeWrap<? extends org.w3c.dom.Node> node;

    public WebNodeParent(WebViewWrap<? extends WebView> webViewOp, Class<ITEM> itemClass) {
        this(webViewOp, null, itemClass);
    }

    public WebNodeParent(WebViewWrap<? extends WebView> webViewOp, WebNodeWrap<? extends org.w3c.dom.Node> webNodeOp, Class<ITEM> itemClass) {
        this.view = webViewOp;
        this.wrapper = new ItemWrapper<ITEM>(webViewOp);
        this.itemClass = itemClass;
        this.node = webNodeOp;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    @Override
    public <ST extends ITEM> Lookup<ST> lookup(Class<ST> controlClass, LookupCriteria<ST> criteria) {
        return new PlainLookup<>(view.getEnvironment(),
                this, wrapper, controlClass, criteria);
    }

    @Override
    public Lookup<ITEM> lookup(LookupCriteria<ITEM> criteria) {
        return this.lookup(itemClass, criteria);
    }

    @Override
    public Class<ITEM> getType() {
        return itemClass;
    }

    protected static class ItemWrapper<ITEM extends org.w3c.dom.Node> implements Wrapper {

        WebViewWrap<? extends WebView> webViewOp;

        public ItemWrapper(WebViewWrap<? extends WebView> webViewOp) {
            this.webViewOp = webViewOp;
        }

        @Override
        public <T> Wrap<? extends T> wrap(Class<T> controlClass, T control) {
            if (controlClass.isAssignableFrom(org.w3c.dom.Node.class)) {
                return (Wrap<? extends T>) new WebNodeWrap(webViewOp, (ITEM) control);
            }
            return null;
        }

    }

    @Override
    public List<ITEM> getControls() {

        view.getEnvironment().getWaiter(WebViewWrap.PAGE_LOADING_TIMEOUT).ensureValue(Worker.State.SUCCEEDED,
                () -> new FutureAction<>(view.getEnvironment(), () -> view.getControl().getEngine().getLoadWorker().getState()).get());

        return new FutureAction<>(view.getEnvironment(), () -> {
            List<ITEM> list = new ArrayList<>();

            NodeList childNodes;
            if (node == null) {
                childNodes = view.getControl().getEngine().getDocument().getChildNodes();
            } else {
                childNodes = node.getControl().getChildNodes();
            }
            addToList(list, childNodes);
            return list;
        }).get();
    }

    private void addToList(List<ITEM> list, NodeList children) {
        for (int i = 0; i < children.getLength(); i++) {
            final Node item = children.item(i);
            list.add((ITEM) item);
            addToList(list, item.getChildNodes());
        }
    }

}
