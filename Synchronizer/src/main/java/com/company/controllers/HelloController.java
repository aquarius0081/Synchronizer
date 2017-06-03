package com.company.controllers;

import com.company.DBUtil;
import com.company.Main;
import com.company.SyncAction;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Rinat on 27.05.2017.
 */
@Controller
public class HelloController {

  /**
   * Instance of {@link Logger} class for {@link Main}
   */
  private static final Logger logger = Logger.getLogger(HelloController.class);

  private static List<SyncAction> actions = Arrays.asList(new SyncAction(0, "export"), new SyncAction(1, "sync"));

  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public String index(Model model) {
    model.addAttribute("actions", actions);
    return "index";
  }

  @RequestMapping(value = "/performAction", method = RequestMethod.POST)
  public @ResponseBody
  String performAction(int id, String fileName) {
    DBUtil.connectToDB();
    if (actions.get(id).getName().equalsIgnoreCase("export")) {
      try {
        logger.info("Start export process from DB to XML file.");
        Main.exportToXml(fileName);
        final String successMessage = "Export process from DB to XML file completed successfully.";
        logger.info(successMessage);
        System.out.println(successMessage);
        return "success";
      } catch (Exception e) {
        logger.fatal("Export process from DB to XML file failed!");
        System.out
            .println("Export process from DB to XML file failed! Please see log for details.");
        return "fail";
      }
    } else if (actions.get(id).getName().equalsIgnoreCase("sync")) {
      try {
        logger.info("Start synchronization process from XML file to DB.");
        Main.syncFromXml(fileName);
        final String successMessage = "Synchronization process from XML file to DB completed successfully.";
        logger.info(successMessage);
        System.out.println(successMessage);
        return "success";
      } catch (Exception e) {
        logger.fatal("Synchronization process from XML file to DB failed!");
        System.out.println(
            "Synchronization process from XML file to DB failed! Please see log for details.");
        return "fail";
      }
    }
    return "fail";
  }
}
