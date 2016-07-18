package com.seveniu.web;

import com.seveniu.common.json.Json;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by seveniu on 6/9/16.
 * SpiderMonitorApi
 */
@Controller
@RequestMapping("/api/spider-monitor")
public class SpiderMonitorApi {

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = "text/json;charset=UTF-8")
    @ResponseBody
    public String getAll() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 20000);
        return Json.toJson(map);
    }
}
