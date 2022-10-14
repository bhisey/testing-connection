package com.example.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import java.util.ArrayList;

import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.MetricRegistry;

public class TestingConnections {
    private CqlSession session;

    static String keyspace = "keyspace1";
    static String table = "names";

    public void connect() {
        CqlSessionBuilder builder = CqlSession.builder();
        session = builder.build();
        Metadata metadata = session.getMetadata();
        System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
    }

    public CqlSession getSession() {
        return this.session;
    }

    public void getData(String keyspace, String table) {
        ResultSet results = session.execute("select * from " + keyspace + "." + table);
        ArrayList<String> first_names = new ArrayList<String>();
        results.forEach(row -> first_names.add(row.getString("first")));
        first_names.forEach(first_name -> System.out.println(first_name));
    }


    public void close() {
            session.close();
    }

    public void registerJMX() {
        MetricRegistry registry = session.getMetrics()
                .orElseThrow(() -> new IllegalStateException("Metrics are disabled"))
                .getRegistry();

        JmxReporter reporter =
                JmxReporter.forRegistry(registry)
                        .inDomain("com.datastax.oss.driver")
                        .build();
        reporter.start();
    }

    public static void main(String[] args) {
            System.out.printf("Connecting to client");
            TestingConnections client = new TestingConnections();
            client.connect();
            client.registerJMX();
            client.getData(keyspace, table);
    }
}