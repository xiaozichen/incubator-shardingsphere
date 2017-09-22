/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.yaml.sharding;

import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Sharding datasource for yaml.
 *
 * @author zhangliang
 */
public class YamlShardingDataSource extends ShardingDataSource {
    
    public YamlShardingDataSource(final File yamlFile) throws IOException, SQLException {
        super(unmarshal(yamlFile).build(Collections.<String, DataSource>emptyMap()), unmarshal(yamlFile).getProps());
    }
    
    public YamlShardingDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws IOException, SQLException {
        super(unmarshal(yamlFile).build(dataSourceMap), unmarshal(yamlFile).getProps());
    }
    
    public YamlShardingDataSource(final byte[] yamlByteArray) throws IOException, SQLException {
        super(unmarshal(yamlByteArray).build(Collections.<String, DataSource>emptyMap()), unmarshal(yamlByteArray).getProps());
    }
    
    public YamlShardingDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlByteArray) throws IOException, SQLException {
        super(unmarshal(yamlByteArray).build(dataSourceMap), unmarshal(yamlByteArray).getProps());
    }
    
    private static YamlShardingConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlShardingConfiguration.class)).loadAs(inputStreamReader, YamlShardingConfiguration.class);
        }
    }
    
    private static YamlShardingConfiguration unmarshal(final byte[] yamlByteArray) throws IOException {
        return new Yaml(new Constructor(YamlShardingConfiguration.class)).loadAs(new ByteArrayInputStream(yamlByteArray), YamlShardingConfiguration.class);
    }
}
