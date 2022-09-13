package com.tubitak.cdcdemo.service;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tubitak.cdcdemo.model.entity.DDLChange;
import com.tubitak.cdcdemo.model.entity.DMLChange;
import com.tubitak.cdcdemo.model.repository.DDLChangeRepository;
import com.tubitak.cdcdemo.model.repository.DMLChangeRepository;

import jakarta.mail.MessagingException;

@Component
public class KafkaListeners {

  @Autowired
  DDLChangeRepository ddlChangeRepository;

  @Autowired
  DMLChangeRepository dmlChangeRepository;

  @Autowired
  private EmailService emailService;

  @Value("${cdc.notification.email.list}")
  String[] recipients;

  @KafkaListener(topics = "${spring.kafka.topics}", groupId = "${spring.kafka.consumer.group-id}")

  public void listen(String message)
      throws UnsupportedEncodingException, MessagingException, JsonProcessingException {

    JSONObject cdcLog = new JSONObject(message);

    String cdcInfo = "";

    JSONObject payload = cdcLog.getJSONObject("payload");
    JSONObject source = payload.getJSONObject("source");

    Long ms = source.getLong("ts_ms");
    DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
    df.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
    Date date = new Date(ms);

    String schema = (String) source.get("schema");
    String table = (String) source.get("table");
    String operation = (String) payload.get("op");

    if (table.equals("ddl_history")) {
      JSONObject after = payload.getJSONObject("after");
      String eventType = (String) after.get("event_type");
      String tableName = (String) after.get("table_name");

      ddlChangeRepository
          .save(new DDLChange(tableName, eventType, df.format(date)));
      cdcInfo += "<table style='background-color:#f2f2f2; margin:0px auto; text-align:center; border: 1px solid #000000; width:90%;'>"
          +
          "<tr style='border: 1px solid #000000;'>" +
          "<th colspan='3' style='border: 1px solid #000000; background-color:#ADD8E6;'>DDL - LOG</th>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<th style='border: 1px solid #000000;'> Object Name</th>" +
          "<th style='border: 1px solid #000000;'> Operation </th>" +
          "<th style='border: 1px solid #000000;'> Date </th>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<td style='border: 1px solid #000000;'>" + tableName + "</td>" +
          "<td style='border: 1px solid #000000;'>" + eventType + "</td>" +
          "<td style='border: 1px solid #000000;'>" + df.format(date) + "</td>" +
          "</tr>" +
          "</table>";
    } else {
      Map<String, String> op = Map.ofEntries(
          Map.entry("c", "INSERT"),
          Map.entry("u", "UPDATE"),
          Map.entry("d", "DELETE"));

      String after = null, before = null;
      switch (operation) {
        case "c":
          after = generateContent(payload, "after");
          dmlChangeRepository.save(
              new DMLChange(schema + "." + table, op.get(operation),
                  df.format(date),
                  null,
                  String.valueOf(payload.get("after"))));
          break;
        case "d":
          before = generateContent(payload, "before");
          dmlChangeRepository
              .save(
                  new DMLChange(schema + "." + table, op.get(operation),
                      df.format(date),
                      String.valueOf(payload.get("before")), null));
          break;
        case "u":
          after = generateContent(payload, "after");
          before = generateContent(payload, "before");
          dmlChangeRepository
              .save(
                  new DMLChange(schema + "." + table, op.get(operation),
                      df.format(date),
                      String.valueOf(payload.get("before")), String.valueOf(payload.get("after"))));
          break;
        default:
          break;
      }

      cdcInfo += "<table style='background-color:#f2f2f2; margin:0px auto; text-align:center; border: 1px solid #000000; width:90%; table-layout:fixed;'>"
          +
          "<tr style='border: 1px solid #000000;'>" +
          "<th colspan='2' style='border: 1px solid #000000; background-color:#ADD8E6;'>DML - LOG</th>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<th style='border: 1px solid #000000;'> Table Name </th>" +
          "<td style='border: 1px solid #000000;'>" + schema + "." + table + "</td>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<th style='border: 1px solid #000000;'> Operation </th>" +
          "<td style='border: 1px solid #000000;'>" + op.get(operation) + "</td>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<th style='border: 1px solid #000000;'> Date </th>" +
          "<td style='border: 1px solid #000000;'>" + df.format(date) + "</td>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<th style='border: 1px solid #000000;'> Before </th>" +
          "<td style='border: 1px solid #000000;'>" + before + "</td>" +
          "</tr>" +
          "<tr style='border: 1px solid #000000;'>" +
          "<th style='border: 1px solid #000000;'> After </th>" +
          "<td style='border: 1px solid #000000;'>" + after + "</td>" +
          "</tr>" +
          "</table>";
    }
    emailService.sendEmail(recipients, cdcInfo);
  }

  public String generateContent(JSONObject payload, String key) {

    JSONObject obj = payload.getJSONObject(key);
    String content = "<table style='border-collapse: collapse; margin: 10px auto; background-color:#ffffcc; width:90%; table-layout:fixed; border: 1px solid #000000;'>";
    Iterator<String> its = obj.keys();
    while (its.hasNext()) {
      String it = its.next();
      content += "<tr>" + "<th style='border: 1px solid #000000;'>" + it + "</th>"
          + "<td style='border: 1px solid #000000;'>" + obj.get(it) + "</td>" + "</tr>";
    }
    content += "</table>";
    return content;
  }
}