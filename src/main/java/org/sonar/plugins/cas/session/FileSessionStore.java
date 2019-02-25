package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public final class FileSessionStore implements CasSessionStore {
    private static final Logger LOG = LoggerFactory.getLogger(FileSessionStore.class);
    private final String sessionStorePath;

    /**
     * This map contains the CAS granting ticket and the issued JWT. This map is only hit during back-channel logout.
     */
    private GrantingTicketFileHandler ticketToJwt;
    /**
     * This map provides the CAS plugin with information about a JWT's validity. This collection is hit on every Sonar
     * request and must be super-fast.
     */
    private JwtTokenFileHandler jwtIdToJwt;

    /**
     * default visibility constructor for testing
     */
    FileSessionStore(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
        this.ticketToJwt = new GrantingTicketFileHandler(sessionStorePath);
        this.jwtIdToJwt = new JwtTokenFileHandler(sessionStorePath);
    }

    public void prepareForWork() {
        try {
            createSessionDirectory();
        } catch (IOException e) {
            throw new CasInitException(e);
        }
    }

    private void createSessionDirectory() throws IOException {
        Path sessionStoreDir = Paths.get(sessionStorePath);
        LOG.info("Creating CAS session store with path {}", sessionStoreDir.toString());

        Files.createDirectories(sessionStoreDir);
    }

    public void store(String ticket, SimpleJwt jwt) {
        LOG.debug("store ticket {} to token {}", ticket, jwt.getJwtId());
        try {
            ticketToJwt.store(ticket, jwt);
            jwtIdToJwt.store(jwt.getJwtId(), jwt);
        } catch (IOException e) {
            LOG.error("Could not store JWT " + jwt.getJwtId() + "to storage path.", e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }
    }

    public boolean isJwtStored(SimpleJwt jwt) {
        boolean stored = jwtIdToJwt.isJwtStored(jwt.getJwtId());
        LOG.debug("check if JWT {} is stored: {}", jwt.getJwtId(), stored);

        return stored;
    }

    public SimpleJwt getJwtById(SimpleJwt jwt) {
        LOG.debug("get token {}", jwt.getJwtId());

        SimpleJwt result;
        try {
            result = jwtIdToJwt.get(jwt.getJwtId());
        } catch (IOException e) {
            LOG.error("Could not return JWT file " + jwt.getJwtId(), e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }
        if (result == null) {
            result = SimpleJwt.getNullObject();
        }

        return result;
    }

    public String invalidateJwt(String grantingTicketId) {
        LOG.debug("invalidate token by ticket {}", grantingTicketId);

        SimpleJwt jwt;
        try {
            String jwtId = ticketToJwt.get(grantingTicketId);
            jwt = jwtIdToJwt.get(jwtId);
        } catch (IOException e) {
            LOG.error("Could not invalidate JWT with granting ticket " + grantingTicketId, e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }
        if (jwt == null) {
            return "no ticket found";
        }

        SimpleJwt invalidated = jwt.cloneAsInvalidated();

        try {
            jwtIdToJwt.replace(jwt.getJwtId(), invalidated);
        } catch (IOException e) {
            LOG.error("Could not invalidate JWT file " + jwt.getJwtId(), e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }

        LOG.debug("successfully invalidated token {} by ticket {}", jwt.getJwtId(), grantingTicketId);

        return invalidated.getJwtId();
    }

    @Override
    public void refreshJwt(SimpleJwt jwtWithLongerExpirationDate) {
        String jwtId = jwtWithLongerExpirationDate.getJwtId();
        LOG.debug("refresh token {}", jwtId);

        try {
            jwtIdToJwt.replace(jwtId, jwtWithLongerExpirationDate);
        } catch (IOException e) {
            LOG.error("Could not invalidate JWT file " + jwtId, e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }

        LOG.debug("successfully refreshed token {}", jwtId);
    }

    public void pruneExpiredEntries() {
        // TODO prune all the expired things
        LOG.debug("prune these tokens {}", Collections.emptyList());
    }

    private static class CasIOAuthenticationException extends RuntimeException {
        CasIOAuthenticationException(String message) {
            super(message);
        }
    }

    private class CasInitException extends RuntimeException {
        CasInitException(IOException e) {
            super(e);
        }
    }
}