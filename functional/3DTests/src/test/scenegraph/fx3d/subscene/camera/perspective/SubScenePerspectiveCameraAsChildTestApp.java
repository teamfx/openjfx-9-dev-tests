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
package test.scenegraph.fx3d.subscene.camera.perspective;

import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import test.javaclient.shared.Utils;
import test.scenegraph.fx3d.interfaces.camera.PerspectiveCameraAsChildTestingFace;
import test.scenegraph.fx3d.subscene.camera.fixedeye.SubScenePerspectiveCameraFixedEyeAsChildTestApp;

/**
 *
 * @author Andrew Glushchenko
 */
public class SubScenePerspectiveCameraAsChildTestApp extends SubScenePerspectiveCameraFixedEyeAsChildTestApp implements PerspectiveCameraAsChildTestingFace {

    @Override
    public void cameraConstruct() {
        for (int i = 0; i < scenesCount; i++) {
            Group parent = (Group) cameras[i].getParent();
            parent.getChildren().remove(cameras[i]);
            cameras[i] = new PerspectiveCamera();
            subScenes[i].setCamera(cameras[i]);
            parent.getChildren().add(cameras[i]);
        }
    }

    @Override
    protected Camera addCamera(SubScene scene) {
        PerspectiveCamera camera = new PerspectiveCamera(false);
        scene.setCamera(camera);
        return camera;
    }

    public static void main(String[] args) {
        Utils.launch(SubScenePerspectiveCameraAsChildTestApp.class, args);
    }
}
