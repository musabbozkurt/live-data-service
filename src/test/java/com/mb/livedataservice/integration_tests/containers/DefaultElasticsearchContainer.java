package com.mb.livedataservice.integration_tests.containers;

import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class DefaultElasticsearchContainer extends ElasticsearchContainer {

    private static final String ELASTIC_SEARCH_DOCKER = "elasticsearch:8.17.1";
    private static final String CLUSTER_NAME = "cluster.name";
    private static final String ELASTIC_SEARCH = "elasticsearch";
    private static final String DISCOVERY_TYPE = "discovery.type";
    private static final String DISCOVERY_TYPE_SINGLE_NODE = "single-node";
    private static final String XPACK_SECURITY_ENABLED = "xpack.security.enabled";

    public DefaultElasticsearchContainer() {
        super(DockerImageName.parse(ELASTIC_SEARCH_DOCKER).asCompatibleSubstituteFor("docker.elastic.co/elasticsearch"));
        addFixedExposedPort(9202, 9202);
        addEnv(DISCOVERY_TYPE, DISCOVERY_TYPE_SINGLE_NODE);
        addEnv(XPACK_SECURITY_ENABLED, Boolean.FALSE.toString());
        addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
    }
}
