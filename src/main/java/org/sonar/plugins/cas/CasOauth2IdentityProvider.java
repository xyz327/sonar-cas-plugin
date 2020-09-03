package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * The {@link CasOauth2IdentityProvider} is responsible for the browser based cas sso authentication. The authentication
 * workflow for an unauthenticated user is as follows:
 *
 * <ol>
 * <li>the {@link ForceCasLoginFilter} redirects the user to /sessions/new</li>
 * <li>the {@link AuthenticationFilter} redirects the user to the CAS Server</li>
 * <li>the user authenticates to the CAS Server</li>
 * <li>the CAS Server redirects back to /sessions/init/cas</li>
 *
 * <li>the {@link CasOauth2IdentityProvider} is called by sonarqube (InitFilter) and creates the user from the assertions and
 * redirects the user to the root of sonarqube.
 * During this phase:
 * <ol>
 * <li>the generated JWT token is fetched from the response</li>
 * <li>the CAS granting ticket is stored along the JWT for later backchannel logout</li>
 * <li>the JWT is stored for black/whitelisting of each incoming </li>
 * </ol>
 * </li>
 * <li>the user logs out at some point (by Javascript injection to the backchannel single logout URL {@link CasSonarSignOutInjectorFilter})</li>
 * <li>CAS requests a logout with a Service Ticket which contains the original Service Ticket
 * <ul>
 * <li>the stored JWT is invalidated and stored back.</li>
 * </ul>
 * </li>
 * <li>The user with an existing JWT cannot re-use existing JWT</li>
 * </ol>
 */
@ServerSide
public class CasOauth2IdentityProvider implements OAuth2IdentityProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CasOauth2IdentityProvider.class);

    private final Configuration configuration;
    private final CasAttributeSettings casAttributeSettings;
    private final CasSettings casSettings;
    private final TicketValidatorFactory ticketValidatorFactory;

    /**
     * called with injection by SonarQube during server initialization
     */
    public CasOauth2IdentityProvider(Configuration configuration,
    CasAttributeSettings casAttributeSettings,
    CasSettings casSettings,TicketValidatorFactory ticketValidatorFactory) {
        this.configuration = configuration;
        this.casAttributeSettings = casAttributeSettings;
        this.casSettings = casSettings;
        this.ticketValidatorFactory = ticketValidatorFactory;
    }

    private String getCasLoginUrl() {
        return casSettings.getCasServerLoginUrl();
    }

    private String getSonarServiceUrl(InitContext context) {
        return context.getCallbackUrl();
    }

    @Override
    public void init(InitContext context) {
        String loginRedirectUrl = getCasLoginUrl() + "?service=" + getSonarServiceUrl(context);
        context.redirectTo(loginRedirectUrl);
    }

    @Override
    public void callback(CallbackContext context) {
        try {
            String grantingTicket = getServiceTicketParameter(context.getRequest());
            if(StringUtils.isBlank(grantingTicket)){
                return;
            }
            TicketValidator validator = ticketValidatorFactory.create();
            Assertion assertion = validator.validate(grantingTicket, context.getCallbackUrl());

            UserIdentity userIdentity = createUserIdentity(assertion);

            LOG.debug("Received assertion. Authenticating with user {}", userIdentity.getName());
            context.authenticate(userIdentity);

            context.redirectToRequestedPage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserIdentity createUserIdentity(Assertion assertion) {
        AttributePrincipal principal = assertion.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        LOG.debug("user role {}", casAttributeSettings.getGroups(attributes));
        LOG.debug("user info:{}", attributes);

        UserIdentity.Builder builder = UserIdentity.builder()
                .setLogin(principal.getName())
                .setProviderLogin(principal.getName());

        String displayName = casAttributeSettings.getDisplayName(attributes);
        if (!Strings.isNullOrEmpty(displayName)) {
            builder = builder.setName(displayName);
        }

        String email = casAttributeSettings.getEmail(attributes);
        if (!Strings.isNullOrEmpty(email)) {
            builder = builder.setEmail(email);
        }

        Set<String> groups = casAttributeSettings.getGroups(attributes);
        if (!groups.isEmpty()) {
            builder = builder.setGroups(groups);
        }

        return builder.build();
    }

    private String getServiceTicketParameter(HttpServletRequest request) {
        String ticket = request.getParameter("ticket");
        return StringUtils.defaultIfEmpty(ticket, "");
    }

    @Override
    public String getKey() {
        return "cas";
    }

    @Override
    public String getName() {
        return "CAS";
    }

    @Override
    public Display getDisplay() {
        return Display.builder()
                .setBackgroundColor("#143E51")
                .setIconPath("/static/casplugin/cas_logo.png")
                .build();
    }

    @Override
    public boolean isEnabled() {
        return casSettings.isEnabled();
    }

    @Override
    public boolean allowsUsersToSignUp() {
        return true;
    }


}
