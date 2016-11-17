package com.betabreakers;

import com.xebialabs.restito.server.StubServer;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.Condition.*;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static StubServer server;

    public static void main(String[] args) throws Exception
    {
        logger.info("Starting main method");
        logger.info("Starting new build");
        String directory = System.getProperty("user.dir");
        StandardJMeterEngine meter = new StandardJMeterEngine();

        setup();

        logger.info("dir is {}", directory);

        logger.info("Loading JMeter properties");
        JMeterUtils.loadJMeterProperties(directory + "/src/main/resources/bin/jmeter.properties");

        logger.info("Setting JMeter base directory");
        JMeterUtils.setJMeterHome(directory + "/src/main/resources");

        logger.info("Initializing locale");
        JMeterUtils.initLocale();

        logger.info("Loading SaveService properties");
        SaveService.loadProperties();

        logger.info("Locating Test Plan");
        File in = new File(directory + "/src/main/resources/TestPlan.jmx");
        HashTree testPlanTree = SaveService.loadTree(in);

        logger.info("Configuring Test Plan");
        meter.configure(testPlanTree);

        logger.info("Running Test Plan");
        meter.run();

        userQuery();
        while (meter.isActive());


        logger.info("Init teardown");
        teardown();
    }

    public static void setup() throws IOException
    {
        logger.info("Creating stub server");
        server = new StubServer(8888).run();
//        Process p = new ProcessBuilder("java -jar CMDrunner.jar --tool Reporter --generate-png test.png" +
//                " --input-jtl results.jtl --plugin-type ResponseTimesOverTime --width 800 --height 600").start();
    }

    @After
    public static void teardown()
    {
        logger.info("Destroying stub server");
        server.stop();
    }

    @Test
    public static void userQuery()
    {
        logger.info("Testing login verification");
        whenHttp(server).match(endsWithUri("/loginrequest")).then(status(HttpStatus.ACCEPTED_202));

        logger.info("Testing /preview page");
        whenHttp(server).match(endsWithUri("/preview")).then(status(HttpStatus.ACCEPTED_202), stringContent("Successfully accessed preview content"));

        logger.info("Testing /demo page");
        whenHttp(server).match(endsWithUri("/demo")).then(status(HttpStatus.ACCEPTED_202), stringContent("Successfully accessed demo content"));
    }

}