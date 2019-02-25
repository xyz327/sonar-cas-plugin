package org.sonar.plugins.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerStartHandler;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.IgnoreCert;
import org.sonar.plugins.cas.util.SonarCasProperties;

@ServerSide
public class DevelopmentServerStartHandler implements ServerStartHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DevelopmentServerStartHandler.class);

    private Configuration configuration;

    public DevelopmentServerStartHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onServerStart(Server server) {
        if (SonarCasProperties.DISABLE_CERT_VALIDATION.getBoolean(configuration, false)) {
            LOG.warn("SSL certificate check is disabled. Please DISABLE SSL disabling on a production machine for security reasons.");
            IgnoreCert.disableSslVerification();
        }
    }
}
