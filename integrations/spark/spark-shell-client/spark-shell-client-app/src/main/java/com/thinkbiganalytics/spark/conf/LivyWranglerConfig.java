//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.thinkbiganalytics.spark.conf;

/*-
 * #%L
 * kylo-spark-shell-client-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClientBuilder;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.SparkShellOptions;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.datavalidator.DataValidator;
import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.service.*;
import com.thinkbiganalytics.spark.shell.CatalogDataSetProviderFactory;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.ResourcePropertySource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan(
        basePackages = {"com.thinkbiganalytics.spark",
                "com.thinkbiganalytics.kylo.catalog" /* needed for CatalogDataSetProviderFactory*/},
        excludeFilters = {@Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {SparkContext.class}
        ), @Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {SQLContext.class}
        ),@Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {EmbeddedServletContainerFactory.class}
        ),@Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {ResourceConfig.class}
        )}
)
@PropertySource(value = {"classpath:sparkDefaults.properties", "classpath:spark.properties", "classpath:sparkDevOverride.properties"}, ignoreResourceNotFound = true)
public class LivyWranglerConfig {
    private static SparkContext sparkContext;
    private static SQLContext sqlContext;

    public LivyWranglerConfig() {

    }

    public static void setSparkContext(SparkContext sparkContext) {
        LivyWranglerConfig.sparkContext = sparkContext;
    }

    public static void setSqlContext(SQLContext sqlContext) {
        LivyWranglerConfig.sqlContext = sqlContext;
    }

    /**
     * Property placeholder configurer needed to process @Value annotations
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Primary
    public SparkContext sparkContext() {
        return sparkContext;
    }

    @Bean
    @Primary
    public SQLContext sqlContext() {
        return sqlContext;
    }

/*
    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainer(final ServerProperties serverProperties, @Qualifier("sparkShellPort") final int serverPort) {
        serverProperties.setPort(serverPort);
        return new TomcatEmbeddedServletContainerFactory();
    }*/

    /**
     * Gets the Hadoop File System.
     */
    @Bean
    @Primary
    public FileSystem fileSystem() throws IOException {
        return FileSystem.get(new org.apache.hadoop.conf.Configuration());
    }

    /**
     * Creates a service to stop this app after a period of inactivity.
     */
    /*
    @Bean
    @Primary
    public IdleMonitorService idleMonitorService(final SparkShellOptions parameters) {
        final IdleMonitorService idleMonitorService = new IdleMonitorService(parameters.getIdleTimeout(), TimeUnit.SECONDS);
        idleMonitorService.startAsync();
        return idleMonitorService;
    }
    */

    /**
     * Gets the resource configuration for setting up Jersey.
     *
     * @return the Jersey configuration
     */
    /*
    @Bean
    @Primary
    public ResourceConfig jerseyConfig(final TransformService transformService, final FileSystem fileSystem, final SparkLocatorService sparkLocatorService) {
        final ResourceConfig config = new ResourceConfig(ApiListingResource.class, SwaggerSerializers.class);
        config.packages("com.thinkbiganalytics.spark.rest");
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(fileSystem).to(FileSystem.class);
                bind(transformService).to(TransformService.class);
                bind(sparkLocatorService).to(SparkLocatorService.class);
            }
        });

        return config;
    } */

    /**
     * Creates the job tracker service.
     */
    @Bean
    @Primary
    public JobTrackerService jobTrackerService(@Nonnull final SparkScriptEngine sparkScriptEngine, @Nonnull final SparkListenerService sparkListenerService) {
        final JobTrackerService jobTrackerService = new JobTrackerService(sparkScriptEngine.getClassLoader());
        sparkListenerService.addSparkListener(jobTrackerService);
        return jobTrackerService;
    }

    /**
     * Gets the command-line options.
     *
     * @param applicationArguments the Spring Application arguments
     * @return the Spark Shell options
     */
    @Bean
    @Primary
    public SparkShellOptions parameters(/*@Nonnull final ApplicationArguments applicationArguments*/) {
        final SparkShellOptions parameters = new SparkShellOptions();
        //new JCommander(parameters, applicationArguments.getSourceArgs());
        return parameters;
    }

    /**
     * Gets the application runner that communicates with Kylo services.
     *
     * @param parameters the command-line options
     * @param serverPort the Spark Shell port number
     * @return an remote client runner
     */
    /*
    @Bean
    @Primary
    public RemoteClientRunner remoteClientRunner(@Nonnull final SparkShellOptions parameters, @Qualifier("sparkShellPort") final int serverPort) {
        return new RemoteClientRunner(parameters, serverPort);
    }
*/

    /**
     * Determines the Spark Shell port number.
     *
     * @param serverPort Spring server port
     * @param parameters command-line options
     * @return the Spark Shell port number
     */
    /*
    @Bean(name = "sparkShellPort")
    @Primary
    public int serverPort(@Nonnull @Value("${server.port}") final String serverPort, @Nonnull final SparkShellOptions parameters) {
        // First check for command-line options
        if (parameters.getPortMin() != SparkShellOptions.NO_PORT || parameters.getPortMax() != SparkShellOptions.NO_PORT) {
            Preconditions.checkArgument(parameters.getPortMin() != SparkShellOptions.NO_PORT, "port-min is required when port-max is specified");
            Preconditions.checkArgument(parameters.getPortMax() != SparkShellOptions.NO_PORT, "port-max is required when port-min is specified");
            Preconditions.checkArgument(parameters.getPortMin() <= parameters.getPortMax(), "port-min must be less than or equal to port-max");

            // Look for an open port
            int port = parameters.getPortMin();
            boolean valid = false;

            do {
                try {
                    new ServerSocket(port).close();
                    valid = true;
                } catch (final IOException e) {
                    ++port;
                }
            } while (!valid && port <= parameters.getPortMax());

            // Return if valid
            if (valid) {
                return port;
            } else {
                throw new IllegalStateException("No open ports available: " + parameters.getPortMin() + "-" + parameters.getPortMax());
            }
        }

        // Second use Spring server port
        return Integer.parseInt(serverPort);
    }
    */

    /**
     * Creates the Spark configuration.
     *
     * @return the Spark configuration
     */
    @Bean
    @Primary
    public SparkConf sparkConf(final Environment env/*, @Qualifier("sparkShellPort") final int serverPort*/) {
        final SparkConf conf = new SparkConf().setAppName("SparkShellServer");
                //.set("spark.ui.port", Integer.toString(serverPort + 1));

        final Iterable<Map.Entry<String, Object>> properties = FluentIterable.from(Collections.singleton(env))
                .filter(AbstractEnvironment.class)
                .transformAndConcat(new Function<AbstractEnvironment, Iterable<?>>() {
                    @Nullable
                    @Override
                    public Iterable<?> apply(@Nullable final AbstractEnvironment input) {
                        return (input != null) ? input.getPropertySources() : null;
                    }
                })
                .filter(ResourcePropertySource.class)
                .transform(new Function<ResourcePropertySource, Map<String, Object>>() {
                    @Nullable
                    @Override
                    public Map<String, Object> apply(@Nullable final ResourcePropertySource input) {
                        return (input != null) ? input.getSource() : null;
                    }
                })
                .transformAndConcat(new Function<Map<String, Object>, Iterable<Map.Entry<String, Object>>>() {
                    @Nullable
                    @Override
                    public Iterable<Map.Entry<String, Object>> apply(@Nullable final Map<String, Object> input) {
                        return (input != null) ? input.entrySet() : null;
                    }
                })
                .filter(new Predicate<Map.Entry<String, Object>>() {
                    @Override
                    public boolean apply(@Nullable final Map.Entry<String, Object> input) {
                        return (input != null && input.getKey().startsWith("spark."));
                    }
                });
        for (final Map.Entry<String, Object> entry : properties) {
            conf.set(entry.getKey(), entry.getValue().toString());
        }

        return conf;
    }




    /**
     * Gets the Spark context.
     */
    /*
    @Bean
    public SparkContext sparkContext(@Nonnull final SparkScriptEngine engine) {
        return engine.getSparkContext();
    }
    */

    /**
     * Creates a Spark locator service.
     */
    @Bean
    public SparkLocatorService sparkLocatorService(final SparkContext sc, @Value("${spark.shell.datasources.exclude}") final String excludedDataSources,
                                                   @Value("${spark.shell.datasources.include}") final String includedDataSources) {
        final SparkLocatorService service = new SparkLocatorService();
        if (excludedDataSources != null && !excludedDataSources.isEmpty()) {
            final List<String> dataSources = Arrays.asList(excludedDataSources.split(","));
            service.excludeDataSources(dataSources);
        }
        if (includedDataSources != null && !includedDataSources.isEmpty()) {
            final List<String> dataSources = Arrays.asList(includedDataSources.split(","));
            service.includeDataSources(dataSources);
        }
        return service;
    }

    /**
     * Gets the Spark SQL context.
     *
     * @param engine the Spark script engine
     * @return the Spark SQL context
     */
    /*
    @Bean
    public SQLContext sqlContext(final SparkScriptEngine engine, @Nonnull final List<JdbcDialect> jdbcDialects) {
        // Register JDBC dialects
        for (final JdbcDialect dialect : jdbcDialects) {
            JdbcDialects.registerDialect(dialect);
        }

        // Create SQL Context
        return engine.getSQLContext();
    }
*/
    /**
     * Gets the transform service.
     *
     * @param transformScriptClass      the transform script class
     * @param engine                    the Spark script engine
     * @param sparkContextService       the Spark context service
     * @param tracker                   the transform job tracker
     * @param datasourceProviderFactory the data source provider factory
     * @param profiler                  the profiler
     * @return the transform service
     */
    /* TJH: pre merge with master
    @Bean
    public TransformService transformService(final Class<? extends TransformScript> transformScriptClass, final SparkScriptEngine engine, final SparkContextService sparkContextService,
                                             final JobTrackerService tracker, final DatasourceProviderFactory datasourceProviderFactory, final Profiler profiler, final DataValidator validator,
                                             final FileSystem fileSystem, final DataSetConverterService converterService) {
        final TransformService service = new TransformService(transformScriptClass, engine, sparkContextService, tracker, converterService);
        service.setDatasourceProviderFactory(datasourceProviderFactory);
        service.setFileSystem(fileSystem);
        service.setProfiler(profiler);
        service.setValidator(validator);
        return service;
    }
    */

    /**
     * Gets the transform service.
     *
     * @param transformScriptClass      the transform script class
     * @param engine                    the Spark script engine
     * @param sparkContextService       the Spark context service
     * @param tracker                   the transform job tracker
     * @param datasourceProviderFactory the data source provider factory
     * @param profiler                  the profiler
     * @return the transform service
     */
    @Bean
    public TransformService transformService(final Class<? extends TransformScript> transformScriptClass, final SparkScriptEngine engine, final SparkContextService sparkContextService,
                                             final JobTrackerService tracker, final DatasourceProviderFactory datasourceProviderFactory, final Profiler profiler, final DataValidator validator,
                                             final FileSystem fileSystem, final DataSetConverterService converterService, final KyloCatalogClientBuilder kyloCatalogClientBuilder, final
                                             CatalogDataSetProviderFactory catalogDataSetProviderFactory) {
        final TransformService service = new TransformService(transformScriptClass, engine, sparkContextService, tracker, converterService, kyloCatalogClientBuilder);
        service.setDatasourceProviderFactory(datasourceProviderFactory);
        service.setFileSystem(fileSystem);
        service.setProfiler(profiler);
        service.setValidator(validator);
        service.setCatalogDataSetProviderFactory(catalogDataSetProviderFactory);
        return service;
    }

    @Bean(name = "downloadsDatasourceExcludes")
    public List<String> getDownloadsDatasourceExcludes(@Value("${spark.shell.datasources.exclude.downloads}") String excludesStr) throws IOException {

        FileUtils.write(new File("/tmp/exDownloads.txt"), excludesStr);

        List<String> excludes = Lists.newArrayList();
        if (StringUtils.isNotEmpty(excludesStr)) {
            excludes.addAll(Arrays.asList(excludesStr.split(",")));
        }
        return excludes;
    }

    @Bean(name = "tablesDatasourceExcludes")
    public List<String> getTablesDatasourceExcludes(@Value("${spark.shell.datasources.exclude.tables}") String excludesStr) throws IOException {
        FileUtils.write(new File("/tmp/exTables.txt"), excludesStr);

        List<String> excludes = Lists.newArrayList();
        if (StringUtils.isNotEmpty(excludesStr)) {
            excludes.addAll(Arrays.asList(excludesStr.split(",")));
        }
        return excludes;
    }

    @Bean
    public SparkUtilityService sparkUtilityService() {
        return new SparkUtilityService();
    }
}
