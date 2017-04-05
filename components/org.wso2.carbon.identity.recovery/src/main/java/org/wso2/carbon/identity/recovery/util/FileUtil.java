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
package org.wso2.carbon.identity.recovery.util;

import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * File util to write read yaml configurations
 */
public class FileUtil {
    private FileUtil() {
    }

    public static <T> T readConfigFile(Path file, Class<T> classType) throws IdentityRecoveryException {

        try (InputStreamReader inputStreamReader =
                     new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            CustomClassLoaderConstructor constructor =
                    new CustomClassLoaderConstructor(FileUtil.class.getClassLoader());
            Yaml yaml = new Yaml(constructor);
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml.loadAs(inputStreamReader, classType);
        } catch (IOException e) {
            throw new IdentityRecoveryException(
                    String.format("Error in reading file %s", file.toString()), e);
        }
    }

    public static <T> void writeConfigFiles(Path file, Object data)
            throws IdentityRecoveryException {

        if (Files.exists(file, new LinkOption[0])) {
            try {
                CustomClassLoaderConstructor constructor =
                        new CustomClassLoaderConstructor(FileUtil.class.getClassLoader());
                Yaml yaml = new Yaml(constructor);
                yaml.setBeanAccess(BeanAccess.FIELD);
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file.toFile()),
                                                            StandardCharsets.UTF_8)) {
                    yaml.dump(data, writer);
                }
            } catch (IOException e) {
                throw new IdentityRecoveryException(
                        String.format("Error in reading file %s", new Object[] { file.toString() }), e);
            }
        } else {
            throw new IdentityRecoveryException(
                    String.format("Configuration file %s is not available.", new Object[] { file.toString() }));
        }
    }
}
