/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
package test.css.controls.functional;

import org.junit.Test;
import client.test.Keywords;
import client.test.Smoke;
import org.junit.BeforeClass;
import org.junit.Before;
import test.javaclient.shared.TestBase;
import static test.css.controls.ControlPage.Accordions;
import test.javaclient.shared.screenshots.ScreenshotUtils;

/**
 * Generated test
 */
public class AccordionsCssTest extends TestBase {

    {
        ScreenshotUtils.setComparatorDistance(0.003f);
    }

    @BeforeClass
    public static void runUI() {
        test.css.controls.ControlsCSSApp.main(null);
    }

    @Before
    public void createPage () {
        ((test.css.controls.ControlsCSSApp)getApplication()).open(Accordions);
    }

    /**
     * test  Accordion with css: -fx-background-color
     */
    @Test
    public void Accordions_BACKGROUND_COLOR() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-COLOR", true);
    }

    /**
     * test  Accordion with css: -fx-background-image
     */
    @Test
    public void Accordions_BACKGROUND_IMAGE() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-IMAGE", true);
    }

    /**
     * test  Accordion with css: -fx-background-inset
     */
    @Test
    public void Accordions_BACKGROUND_INSET() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-INSET", true);
    }

    /**
     * test  Accordion with css: -fx-background-position
     */
    @Test
    public void Accordions_BACKGROUND_POSITION() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-POSITION", true);
    }

    /**
     * test  Accordion with css: -fx-background-repeat-round
     */
    @Test
    public void Accordions_BACKGROUND_REPEAT_ROUND() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-REPEAT-ROUND", true);
    }

    /**
     * test  Accordion with css: -fx-background-repeat-space
     */
    @Test
    public void Accordions_BACKGROUND_REPEAT_SPACE() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-REPEAT-SPACE", true);
    }

    /**
     * test  Accordion with css: -fx-background-repeat-x-y
     */
    @Test
    public void Accordions_BACKGROUND_REPEAT_X_Y() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-REPEAT-X-Y", true);
    }

    /**
     * test  Accordion with css: -fx-background-size
     */
    @Test
    public void Accordions_BACKGROUND_SIZE() throws Exception {
       testAdditionalAction(Accordions.name(), "BACKGROUND-SIZE", true);
    }

    /**
     * test  Accordion with css: -fx-blend-mode
     */
    @Test
    public void Accordions_BLEND_MODE() throws Exception {
       testAdditionalAction(Accordions.name(), "BLEND-MODE", true);
    }

    /**
     * test  Accordion with css: -fx-border-color
     */
    @Test
    public void Accordions_BORDER_COLOR() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-COLOR", true);
    }

    /**
     * test  Accordion with css: -fx-border-inset
     */
    @Test
    public void Accordions_BORDER_INSET() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-INSET", true);
    }

    /**
     * test  Accordion with css: -fx-border-style-dashed
     */
    @Test
    public void Accordions_BORDER_STYLE_DASHED() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-STYLE-DASHED", true);
    }

    /**
     * test  Accordion with css: -fx-border-style-dotted
     */
    @Test
    public void Accordions_BORDER_STYLE_DOTTED() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-STYLE-DOTTED", true);
    }

    /**
     * test  Accordion with css: -fx-border-width
     */
    @Test
    public void Accordions_BORDER_WIDTH() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-WIDTH", true);
    }

    /**
     * test  Accordion with css: -fx-border-width-dashed
     */
    @Test
    public void Accordions_BORDER_WIDTH_dashed() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-WIDTH-dashed", true);
    }

    /**
     * test  Accordion with css: -fx-border-width-dotted
     */
    @Test
    public void Accordions_BORDER_WIDTH_dotted() throws Exception {
       testAdditionalAction(Accordions.name(), "BORDER-WIDTH-dotted", true);
    }

    /**
     * test  Accordion with css: -fx-drop-shadow
     */
    @Test
    public void Accordions_DROP_SHADOW() throws Exception {
       testAdditionalAction(Accordions.name(), "DROP_SHADOW", true);
    }

    /**
     * test  Accordion with css: -fx-image-border
     */
    @Test
    public void Accordions_IMAGE_BORDER() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER", true);
    }

    /**
     * test  Accordion with css: -fx-image-border-insets
     */
    @Test
    public void Accordions_IMAGE_BORDER_INSETS() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER-INSETS", true);
    }

    /**
     * test  Accordion with css: -fx-image-border-no-repeat
     */
    @Test
    public void Accordions_IMAGE_BORDER_NO_REPEAT() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER-NO-REPEAT", true);
    }

    /**
     * test  Accordion with css: -fx-image-border-repeat-x
     */
    @Test
    public void Accordions_IMAGE_BORDER_REPEAT_X() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER-REPEAT-X", true);
    }

    /**
     * test  Accordion with css: -fx-image-border-repeat-y
     */
    @Test
    public void Accordions_IMAGE_BORDER_REPEAT_Y() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER-REPEAT-Y", true);
    }

    /**
     * test  Accordion with css: -fx-image-border-round
     */
    @Test
    public void Accordions_IMAGE_BORDER_ROUND() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER-ROUND", true);
    }

    /**
     * test  Accordion with css: -fx-image-border-space
     */
    @Test
    public void Accordions_IMAGE_BORDER_SPACE() throws Exception {
       testAdditionalAction(Accordions.name(), "IMAGE-BORDER-SPACE", true);
    }

    /**
     * test  Accordion with css: -fx-inner-shadow
     */
    @Test
    public void Accordions_INNER_SHADOW() throws Exception {
       testAdditionalAction(Accordions.name(), "INNER_SHADOW", true);
    }

    /**
     * test  Accordion with css: -fx-opacity
     */
    @Test
    public void Accordions_OPACITY() throws Exception {
       testAdditionalAction(Accordions.name(), "OPACITY", true);
    }

    /**
     * test  Accordion with css: -fx-padding
     */
    @Test
    public void Accordions_PADDING() throws Exception {
       testAdditionalAction(Accordions.name(), "PADDING", true);
    }

    /**
     * test  Accordion with css: -fx-position-scale-shape
     */
    @Test
    public void Accordions_POSITION_SCALE_SHAPE() throws Exception {
       testAdditionalAction(Accordions.name(), "POSITION-SCALE-SHAPE", true);
    }

    /**
     * test  Accordion with css: -fx-rotate
     */
    @Test
    public void Accordions_ROTATE() throws Exception {
       testAdditionalAction(Accordions.name(), "ROTATE", true);
    }

    /**
     * test  Accordion with css: -fx-scale-shape
     */
    @Test
    public void Accordions_SCALE_SHAPE() throws Exception {
       testAdditionalAction(Accordions.name(), "SCALE-SHAPE", true);
    }

    /**
     * test  Accordion with css: -fx-scale-x
     */
    @Test
    public void Accordions_SCALE_X() throws Exception {
       testAdditionalAction(Accordions.name(), "SCALE-X", true);
    }

    /**
     * test  Accordion with css: -fx-scale-y
     */
    @Test
    public void Accordions_SCALE_Y() throws Exception {
       testAdditionalAction(Accordions.name(), "SCALE-Y", true);
    }

    /**
     * test  Accordion with css: -fx-shape
     */
    @Test
    public void Accordions_SHAPE() throws Exception {
       testAdditionalAction(Accordions.name(), "SHAPE", true);
    }

    /**
     * test  Accordion with css: -fx-snap-to-pixel
     */
    @Test
    public void Accordions_SNAP_TO_PIXEL() throws Exception {
       testAdditionalAction(Accordions.name(), "SNAP-TO-PIXEL", true);
    }

    /**
     * test  Accordion with css: -fx-translate-x
     */
    @Test
    public void Accordions_TRANSLATE_X() throws Exception {
       testAdditionalAction(Accordions.name(), "TRANSLATE-X", true);
    }

    /**
     * test  Accordion with css: -fx-translate-y
     */
    @Test
    public void Accordions_TRANSLATE_Y() throws Exception {
       testAdditionalAction(Accordions.name(), "TRANSLATE-Y", true);
    }



    public String getName() {
        return "ControlCss";
    }
}
