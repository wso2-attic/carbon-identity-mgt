/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.store.test.unit;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.exception.CarbonIdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.impl.internal.config.domain.DomainConfigFile;
import org.wso2.carbon.identity.mgt.impl.util.FileUtil;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit Tests for FIleUtils
 */
@PrepareForTest(FileUtil.class)
public class FileUtilTests {


    @Test
    public void testReadConfigFile() throws CarbonIdentityMgtConfigException {
        Path path = Paths.get(File.separator, "conf", "domain-config.yaml");
        URL url = this.getClass().getResource(path.toString());
        DomainConfigFile domainConfigFile = FileUtil.readConfigFile(url.getPath(), DomainConfigFile.class);
        Assert.assertNotNull(domainConfigFile, "domain-config.yaml is not read.");
    }

    @Test
    public void testReadConfigFiles() {
        Path path = Paths.get(File.separator, "conf");
        URL url = this.getClass().getResource(path.toString());
        path = Paths.get(url.getPath());
        boolean exceptionThrown = false;
        try {
            List<DomainConfigFile> domainConfigFile = FileUtil.readConfigFiles(path, DomainConfigFile.class,
                    "*random.yaml");
            Assert.assertTrue(domainConfigFile.size() == 0, "Result list should be empty when no files found.");
        } catch (CarbonIdentityMgtConfigException e) {
            exceptionThrown = true;
            Assert.assertTrue(exceptionThrown, "File Read failed when no files found.");
        }
    }
}
