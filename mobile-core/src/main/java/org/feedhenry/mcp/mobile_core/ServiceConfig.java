package org.feedhenry.mcp.mobile_core;

import java.util.Map;
import java.util.Optional;

/**
 * Created by summers on 9/26/17.
 */

interface ServiceConfig {

    Map<String, String> getConfigFor(String serviceName);

}
