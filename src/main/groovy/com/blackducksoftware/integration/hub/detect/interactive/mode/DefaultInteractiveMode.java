/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.detect.interactive.mode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detect.help.DetectOptionManager;
import com.blackducksoftware.integration.hub.detect.hub.HubServiceWrapper;
import com.blackducksoftware.integration.log.SilentLogger;

@Component
public class DefaultInteractiveMode extends InteractiveMode {
    @Autowired
    private HubServiceWrapper hubServiceWrapper;

    @Autowired
    private DetectOptionManager detectOptionManager;

    @Override
    public void interact() {
        printWelcome();

        final Boolean connectToHub = askYesOrNo("Would you like to connect to a Hub Instance?");
        if (connectToHub == true) {
            boolean connected = false;
            boolean skipConnectionTest = false;
            while (!connected && !skipConnectionTest) {
                setPropertyFromQuestion("hubUrl", "What is the hub instance url?");

                print("You can now configure the hub with either an API token -OR- a username and password. The API token must already exist on the hub, but it is the preferred approach to configure your connection.");
                final Boolean useApiToken = askYesOrNo("Would you like to use an existing API token?");
                if (useApiToken) {
                    setPropertyFromQuestion("hubApiToken", "What is the hub API token?");
                } else {
                    setPropertyFromQuestion("hubUsername", "What is the hub username?");

                    final Boolean setHubPassword = askYesOrNoWithMessage("Would you like to set the hub password?",
                            "WARNING: If you choose to save the settings, this password will be stored in plain text. You can set this password as an environment variable BLACKDUCK_HUB_PASSWORD.");
                    if (setHubPassword) {
                        setPropertyFromSecretQuestion("hubPassword", "What is the hub password?");
                    }
                }

                final Boolean useProxy = askYesOrNo("Would you like to configure a proxy for the hub?");
                if (useProxy) {
                    setPropertyFromQuestion("hubProxyHost", "What is the hub proxy host?");
                    setPropertyFromQuestion("hubProxyPort", "What is the hub proxy port?");
                    setPropertyFromQuestion("hubProxyUsername", "What is the hub proxy username?");
                    final Boolean setHubPassword = askYesOrNoWithMessage("Would you like to set the hub proxy password?",
                            "WARNING: If you choose to save the settings, this password will be stored in plain text. You can set this password as an environment variable BLACKDUCK_HUB_PROXY_PASSWORD.");
                    if (setHubPassword) {
                        setPropertyFromSecretQuestion("hubProxyPassword", "What is the hub proxy password?");
                    }
                    final Boolean useNtlmProxy = askYesOrNo("Do you use a ntlm proxy?");
                    if (useNtlmProxy) {
                        setPropertyFromQuestion("hubProxyNtlmDomain", "What is the ntlm proxy domain?");
                        setPropertyFromQuestion("hubProxyNtlmWorkstation", "What is the ntlm proxy workstation?");
                    }
                }

                final Boolean trustCert = askYesOrNo("Would you like to automatically trust the hub certificate?");
                if (trustCert) {
                    setProperty("hubTrustCertificate", "true");
                }

                final Boolean testHub = askYesOrNo("Would you like to test the hub connection now?");
                if (testHub) {
                    try {
                        detectOptionManager.applyInteractiveOptions(getInteractiveOptions());
                        connected = hubServiceWrapper.testHubConnection(new SilentLogger());
                    } catch (final Exception e) {
                        println("Failed to test hub connection.");
                        println(e.toString());
                        println("");
                    }

                    if (!connected) {
                        println("Failed to connect to the hub.");
                        println("");
                        skipConnectionTest = !askYesOrNo("Would you like to retry entering the hub information?");
                    }
                } else {
                    skipConnectionTest = true;
                }
            }

            final Boolean customDetails = askYesOrNo("Would you like to provide a project name and version to use on the hub?");
            if (customDetails) {
                setPropertyFromQuestion("projectName", "What is the hub project name?");
                setPropertyFromQuestion("projectVersionName", "What is the hub project version?");
            }
        } else {
            setProperty("hubOfflineMode", "true");
        }

        final Boolean scan = askYesOrNo("Would you like run a CLI scan?");
        if (!scan) {
            setProperty("hubSignatureScannerDisabled", "true");
        } else if (scan && connectToHub) {
            final Boolean upload = askYesOrNo("Would you like to upload CLI scan results to the hub?");
            if (!upload) {
                setProperty("hubSignatureScannerDryRun", "true");
            }
        }

        if (scan) {
            final Boolean customScanner = askYesOrNo("Would you like to provide a custom scanner?");
            if (customScanner) {
                final Boolean downloadCustomScanner = askYesOrNo("Would you like to download the custom scanner?");
                if (downloadCustomScanner) {
                    setPropertyFromQuestion("hubSignatureScannerHostUrl", "What is the scanner host url?");
                } else {
                    setPropertyFromQuestion("hubSignatureScannerOfflineLocalPath", "What is the location of your offline scanner?");
                }
            }
        }

        performStandardOutflow();
    }

}
