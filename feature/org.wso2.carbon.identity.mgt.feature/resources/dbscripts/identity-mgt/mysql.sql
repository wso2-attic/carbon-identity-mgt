--
-- Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

/* NOTE: Use VARCHAR(255) instead of VARCHAR(256) if the length needed is less than 256. Because 256 will require
 * two bytes to store the VARCHAR character length.
 */

CREATE TABLE IF NOT EXISTS IDM_ENTITY
(
  ID                INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
  USER_UUID         VARCHAR(64)                        NOT NULL,
  DOMAIN            VARCHAR(64)                        NOT NULL,
  CONNECTOR_TYPE    CHAR(1) DEFAULT 'I'                NOT NULL,
  CONNECTOR_ID      VARCHAR(64)                        NOT NULL,
  CONNECTOR_USER_ID VARCHAR(64)                        NOT NULL
) ENGINE INNODB;

CREATE UNIQUE INDEX IDM_ENTITY_INDEX_1
  ON IDM_ENTITY (USER_UUID, DOMAIN, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID);

CREATE INDEX IDM_ENTITY_INDEX_2 ON IDM_ENTITY (CONNECTOR_ID, CONNECTOR_USER_ID);

CREATE INDEX IDM_ENTITY_INDEX_3 ON IDM_ENTITY(ENTITY_UUID);

CREATE TABLE IDM_USER_GROUP_MAPPING
(
  ID                  INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
  USER_UUID           VARCHAR(64)                        NOT NULL,
  GROUP_UUID          VARCHAR(64)                        NOT NULL
)
