/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.bomtool.npm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.detect.bomtool.NpmBomTool;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.model.NpmPackageJson;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class NpmPackageFolder {
    private final String path;
    private final File nodeModulesDirectory;
    public String scopedPackageName;
    private NpmPackageJson packageJson = null;

    public NpmPackageFolder(final String path) {
        this.path = path;
        nodeModulesDirectory = new File(path, NpmBomTool.NODE_MODULES);
    }

    public NpmPackageFolder(final File projectDirectory) {
        this.path = projectDirectory.getPath();
        this.nodeModulesDirectory = new File(projectDirectory, NpmBomTool.NODE_MODULES);
    }

    public NpmPackageFolder getParentNpmProject() {
        File nodeModulesParent = nodeModulesDirectory.getParentFile();

        while (nodeModulesParent != null) {
            if (NpmBomTool.NODE_MODULES.equals(nodeModulesParent.getName())) {
                return new NpmPackageFolder(nodeModulesParent.getParentFile());
            }

            nodeModulesParent = nodeModulesParent.getParentFile();
        }

        return null;
    }

    public NpmPackageFolder getChildNpmProjectFromNodeModules(final String npmProjectName) {
        final File projectFolder = new File(nodeModulesDirectory, npmProjectName);
        if (projectFolder.exists()) {
            return new NpmPackageFolder(projectFolder);
        }

        return null;
    }

    public List<NpmPackageFolder> getChildNpmProjectsFromNodeModules() {

        final List<NpmPackageFolder> packages = new ArrayList<>();
        if (nodeModulesDirectory.exists()) {
            final File[] list = nodeModulesDirectory.listFiles();
            for (final File file : list) {
                final NpmPackageFolder folder = new NpmPackageFolder(file);
                if (folder.packageJsonExists()) {
                    packages.add(new NpmPackageFolder(file));
                } else {
                    if (file.getName().startsWith("@")) {
                        final File[] psuedo = file.listFiles();
                        for (final File psuedoFile : psuedo) {
                            for (final File realFile : psuedoFile.listFiles()) {
                                final NpmPackageFolder folderInner = new NpmPackageFolder(realFile);
                                if (folderInner.packageJsonExists()) {
                                    final NpmPackageFolder found = new NpmPackageFolder(realFile);
                                    found.scopedPackageName = file.getName();
                                    packages.add(found);
                                }
                            }
                        }
                    }
                }
            }
        }

        return packages;
    }

    public boolean packageJsonExists() {
        final File packageJsonFile = new File(path, NpmBomTool.PACKAGE_JSON);
        return packageJsonFile.exists();
    }

    public NpmPackageJson getPackageJson(final Gson gson) {
        if (packageJson == null) {
            try {
                final File packageJsonFile = new File(path, NpmBomTool.PACKAGE_JSON);
                final FileReader fileReader = new FileReader(packageJsonFile);
                final JsonReader jsonReader = new JsonReader(fileReader);
                final NpmPackageJson newPackageJson = gson.fromJson(jsonReader, NpmPackageJson.class);
                packageJson = newPackageJson;
            } catch (final FileNotFoundException e) {
                packageJson = new NpmPackageJson();
            }
        }

        return packageJson;
    }

    public String getPath() {
        return path;
    }

    public File getNodeModulesDirectory() {
        return nodeModulesDirectory;
    }
}
