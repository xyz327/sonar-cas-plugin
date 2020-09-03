/*
 * Sonar CAS Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cas;

import com.google.common.collect.ImmutableSet;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.util.*;

/**
 * Parse the config, provide attribute configuration and util methods for extracting the attribute values.
 *
 * @author Jan Boerner, TRIOLOGY GmbH
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
@ServerSide
public class CasAttributeSettings {

    private final CasSettings casSettings;

    /**
     * called with injection by SonarQube during server initialization
     */
    public CasAttributeSettings(CasSettings casSettings) {
        this.casSettings = casSettings;
    }

    Set<String> getGroups(Map<String, Object> attributes) {
        Set<String> groups = new HashSet<>();
        for (String key : getRoleAttributes()) {
            Collection<String> roles = getCollectionAttribute(attributes, key);
            if (roles != null) {
                groups.addAll(roles);
            }
        }
        return groups;
    }

    private List<String> getRoleAttributes() {
        final String str = casSettings.getRolesAttribute();
        if (!str.isEmpty()) {
            return Arrays.asList(str.split(","));
        }
        return Collections.emptyList();
    }

    private Collection<String> getCollectionAttribute(Map<String, Object> attributes, String key) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        Object attribute = attributes.get(key);
        if (attribute instanceof Collection) {
            for (Object item : (Collection) attribute) {
                builder.add(item.toString());
            }
        } else if (attribute != null) {
            builder.add(attribute.toString());
        }

        return builder.build();
    }

    String getEmail(Map<String, Object> attributes) {
        String mailAttribute = casSettings.getEmailAttribute();
        return getStringAttribute(attributes, mailAttribute);
    }



    String getDisplayName(Map<String, Object> attributes) {
        return getStringAttribute(attributes, casSettings.getFullNameAttribute());
    }


    private String getStringAttribute(Map<String, Object> attributes, String key) {
        return (String) attributes.get(key);
    }
}
