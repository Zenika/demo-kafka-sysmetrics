package com.zenika.kafka.demo;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class to de/serialize {@link Metrics} to and from Json string.
 *
 * This class is not threadsafe.
 */
public class JsonReaderWriter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonReaderWriter.class);

    public final JSONParser parser = new JSONParser();

    public String toJson(Metrics m) {

        JSONObject json = new JSONObject();
        json.put("time", m.time);
        json.put("cpuPercUser", m.cpuPercUser);
        json.put("cpuPercSys", m.cpuPercSys);
        json.put("memTotal", m.memTotal);
        json.put("memUsed", m.memUsed);
        json.put("memActual", m.memActualUsed);
        json.put("memFree", m.memFree);
        return json.toJSONString();
    }

    public Metrics fromJson(String s) {
        try {
            JSONObject json = (JSONObject) parser.parse(s);
            Metrics metrics = new Metrics(
                    (Long) json.get("time"),
                    (Double) json.get("cpuPercUser"),
                    (Double) json.get("cpuPercSys"),
                    (Double) json.get("memTotal"),
                    (Double) json.get("memActual"),
                    (Double) json.get("memUsed"),
                    (Double) json.get("memFree")
            );
            return metrics;
        } catch (Exception e) {
            LOG.error("Error occurred while parsing json {}", s, e);
            throw new RuntimeException(e);
        }
    }
}
