package org.sonar.plugins.cas;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

@ServerSide
public final class CasRestClientFactory {
    private final CasSettings casSettings;
    private CasRestClient impl;
    private Configuration configuration;

    /** This constructor is used with Dependency Injection during SonarQube start-up time*/
    @SuppressWarnings("unused")
    public CasRestClientFactory(CasSettings casSettings) {
        this.casSettings = casSettings;
    }

    CasRestClientFactory(Configuration configuration, CasRestClient impl) {
        this.configuration = configuration;
        this.impl = impl;
        casSettings = null;
    }

    CasRestClient create() {
        if(impl != null) {
            return impl;
        }

        String casServerUrlPrefix = getCasServerUrlPrefix();
        String serviceUrl = getServiceUrl();
        return new CasRestClient(casServerUrlPrefix, serviceUrl);
    }

    private String getCasServerUrlPrefix() {
        return casSettings.getCasServerUrl();
    }

    private String getServiceUrl() {
        return casSettings.getSonarServerUrl();
    }
}
